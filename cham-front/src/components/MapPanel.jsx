import { useEffect, useRef, useState } from 'react';
import styled, { useTheme } from 'styled-components';
import { useSearchMapState } from '@/recoil/useAppState.js';
import { useRecoilValue, useSetRecoilState } from 'recoil';
import { mapCenterAddrState, mapSearchFilterState, userState } from '@/recoil/appState.js';
import { confirmAlert } from 'react-confirm-alert';
import api from '@/utils/axiosInstance.js';
import { toast } from 'react-toastify';
import { AiOutlineUpload, AiOutlineDelete } from 'react-icons/ai';
import { useMapSearch } from '@/recoil/fetchAppState.js';
import Modal from '@/components/modal/Modal.jsx';
import DetailPage from '@/pages/DetailPage.jsx';

/** ====== 줌 기준 (카카오: 값이 작을수록 더 확대됨) ====== */
const DETAIL_MAX_LEVEL = 5; // ≤5  : 상세(가맹점별)
const DONG_MAX_LEVEL_MIN = 6; // 6~8 : 동 집계
const DONG_MAX_LEVEL_MAX = 8;
const GU_LEVEL_MIN = 9; // 9~10: 구 집계
const GU_LEVEL_MAX = 10;
const SIDO_LEVEL_MIN = 11; // ≥11 : 시/도 집계

/** DB 좌표 파싱 (x=경도, y=위도) */
function toLatLng(x, y) {
  const lng = parseFloat(x);
  const lat = parseFloat(y);
  if (Number.isFinite(lat) && Number.isFinite(lng)) return { lat, lng };
  return null;
}

/** 현재 지도 bounds 내에 있는지 */
function inView(lat, lng, map) {
  const b = map.getBounds();
  const sw = b.getSouthWest();
  const ne = b.getNorthEast();
  return lat >= sw.getLat() && lat <= ne.getLat() && lng >= sw.getLng() && lng <= ne.getLng();
}

/** 카테고리 표시(두 번째 단계 우선, 없으면 첫 단계) */
function getCatLabel(categoryName) {
  if (!categoryName) return '기타';
  const parts = String(categoryName)
    .split('>')
    .map(s => s.trim())
    .filter(Boolean);
  if (parts.length >= 2) return parts[1];
  if (parts.length >= 1) return parts[0];
  return '기타';
}

export default function MapPanel() {
  const theme = useTheme();
  const { mapData } = useSearchMapState(); // { details: {...}, summaries: {depth0,depth1,depth2}}
  const setCenterAddr = useSetRecoilState(mapCenterAddrState);
  const [mapReady, setMapReady] = useState(false);

  const searchCondition = useRecoilValue(mapSearchFilterState);
  const user = useRecoilValue(userState);

  const [open, setOpen] = useState(false);
  const [detailParams, setDetailParams] = useState(null);

  const fileInputRef = useRef(null);
  const [deleteText, setDeleteText] = useState('');
  const mapSearch = useMapSearch();

  const handleSearch = () => {
    const params = {
      cardOwnerPositionId: searchCondition.selectedRole?.value,
      input: searchCondition.input,
      sortOrder: searchCondition.sortOrder,
      addrDetail: '',
    };
    mapSearch(params);
  };

  const handleExcelUploadClick = () => fileInputRef.current?.click();

  const handleExcelFileChange = async e => {
    const file = e.target.files[0];
    if (!file) return;
    const name = file.name.toLowerCase();
    if (!name.endsWith('.xlsx') && !name.endsWith('.xls')) {
      toast.error('엑셀 파일(.xlsx, .xls)만 업로드할 수 있습니다.');
      return;
    }
    const formData = new FormData();
    formData.append('multipartFile', file);
    try {
      await api.post('/cham/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      toast.success('엑셀 파일이 업로드되었습니다.');
      await handleSearch();
    } catch (err) {
      console.error(err);
      toast.error(err.response?.data?.message ?? '업로드 실패');
    } finally {
      e.target.value = '';
    }
  };

  /** 최초 지도 생성 */
  useEffect(() => {
    const loadScript = () =>
      new Promise(resolve => {
        const script = document.createElement('script');
        const mapKey = import.meta.env.VITE_KAKAO_MAP_ID;
        script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${mapKey}&autoload=false&libraries=services`;
        script.async = true;
        document.head.appendChild(script);
        script.onload = resolve;
      });

    const initMap = () => {
      window.kakao.maps.load(() => {
        const container = document.getElementById('map');
        const map = new window.kakao.maps.Map(container, {
          center: new window.kakao.maps.LatLng(36.3504, 127.3845),
          level: 10, // 구 집계로 시작
        });
        const zoomControl = new window.kakao.maps.ZoomControl();
        map.addControl(zoomControl, window.kakao.maps.ControlPosition.RIGHT);
        window.mapInstance = map;

        // 대전 근처 bounds
        const bounds = new window.kakao.maps.LatLngBounds();
        bounds.extend(new window.kakao.maps.LatLng(36.461, 127.275));
        bounds.extend(new window.kakao.maps.LatLng(36.281, 127.493));
        map.setBounds(bounds);

        setMapReady(true);
      });
    };

    loadScript().then(() => window.kakao.maps.load(initMap));
  }, []);

  /** (DB 데이터만 사용) */
  useEffect(() => {
    if (!window.kakao?.maps || !window.mapInstance || !mapReady) return;
    if (!mapData) return;

    const map = window.mapInstance;

    // 오버레이 캐시
    if (!globalThis._detailOverlays) globalThis._detailOverlays = new Map(); // addrDetail → CustomOverlay
    if (!globalThis._aggOverlays) globalThis._aggOverlays = new Map(); // key(path/depth) → CustomOverlay

    const hideAll = () => {
      globalThis._detailOverlays.forEach(o => o.setMap(null));
      globalThis._aggOverlays.forEach(o => o.setMap(null));
    };

    const draw = () => {
      const level = map.getLevel();

      const isDetail = level <= DETAIL_MAX_LEVEL;
      const isDong = level >= DONG_MAX_LEVEL_MIN && level <= DONG_MAX_LEVEL_MAX;
      const isGu = level >= GU_LEVEL_MIN && level <= GU_LEVEL_MAX;
      const isSido = level >= SIDO_LEVEL_MIN;

      // 항상 현재 화면에 보이는 '디테일' 객체들을 리스트로 넘긴다
      const visibleDetails = [];

      // ========== 상세 오버레이(마커) 갱신 ==========
      const details = Object.values(mapData?.details || {});
      details.forEach(item => {
        const coords = toLatLng(item.x, item.y);
        if (!coords) return;

        const key = item.addrDetail || `${coords.lat},${coords.lng}`;
        let overlay = globalThis._detailOverlays.get(key);

        // 오버레이 생성 (카테고리/금액/방문수 표기 + 클릭시 상세 모달)
        if (!overlay) {
          const div = document.createElement('div');
          div.style.cssText = `
            display:flex;align-items:center;justify-content:center;
            background:${theme.colors.primary};color:white;padding:5px 12px;
            border-radius:20px;font-weight:bold;font-size:${theme.sizes.medium};
            white-space:nowrap;box-shadow:0 2px 6px rgba(0,0,0,.3);height:30px;cursor:pointer;
          `;
          const catLabel = getCatLabel(item.categoryName);
          div.innerHTML =
            `${catLabel}&nbsp;${(item.totalSum ?? 0).toLocaleString()}원&nbsp;&nbsp;&nbsp;` +
            `<i class="fa fa-walking"></i>&nbsp;${item.visits ?? 0}`;

          div.addEventListener('click', () => {
            const params = {
              cardOwnerPositionId: searchCondition.selectedRole?.value,
              cardUseName: searchCondition.cardUseName,
              numberOfVisits: searchCondition.numberOfVisits,
              addrName: searchCondition.addrName,
              startDate: searchCondition.startDate?.toISOString?.().split('T')[0],
              endDate: searchCondition.endDate?.toISOString?.().split('T')[0],
              sortOrder: searchCondition.sortOrder,
              addrDetail: item.addrDetail,
              detail: true,
              catLabel: item.categoryName || '기타',
            };
            setDetailParams(params);
            setOpen(true);
          });

          overlay = new window.kakao.maps.CustomOverlay({
            position: new window.kakao.maps.LatLng(coords.lat, coords.lng),
            content: div,
            yAnchor: 1,
            zIndex: 1,
          });

          // hover 효과
          div.addEventListener('mouseenter', () => {
            div.style.transform = 'scale(1.25)';
            overlay.setZIndex(9999);
          });
          div.addEventListener('mouseleave', () => {
            div.style.transform = 'scale(1)';
            overlay.setZIndex(1);
          });

          globalThis._detailOverlays.set(key, overlay);
        } else {
          overlay.setPosition(new window.kakao.maps.LatLng(coords.lat, coords.lng));
        }

        // 상세 모드일 때만 지도에 보이게
        if (isDetail && inView(coords.lat, coords.lng, map)) {
          overlay.setMap(map);
        } else {
          overlay.setMap(null);
        }

        // 화면에 보이는 디테일은 리스트에 싹 담아 보냄 (모드 무관)
        if (inView(coords.lat, coords.lng, map)) {
          visibleDetails.push({
            ...item,
            categoryLabel: item.categoryName || '기타',
          });
        }
      });

      // ========== 집계(시/구/동) 오버레이 갱신 ==========
      globalThis._aggOverlays.forEach(o => o.setMap(null)); // 모드 바뀔 수 있으니 일단 숨김

      const pick = isDong
        ? mapData?.summaries?.depth2
        : isGu
          ? mapData?.summaries?.depth1
          : isSido
            ? mapData?.summaries?.depth0
            : [];

      if (!isDetail && Array.isArray(pick)) {
        pick.forEach(g => {
          const coords = toLatLng(g.x, g.y);
          if (!coords) return;
          if (!inView(coords.lat, coords.lng, map)) return;

          const key = `${g.depth}-${g.regionId}-${g.path}`;
          let overlay = globalThis._aggOverlays.get(key);

          if (!overlay) {
            const div = document.createElement('div');
            div.style.cssText = `
              display:flex;align-items:center;justify-content:center;text-align:center;
              background:${theme.colors.primary};color:white;padding:10px 14px;
              border-radius:24px;font-weight:bold;font-size:${theme.sizes.medium};
              white-space:nowrap;box-shadow:0 2px 6px rgba(0,0,0,.3);cursor:pointer;
            `;
            // 집계 뱃지 (지역명 + 방문수)
            div.innerHTML = `${g.path}&nbsp;&nbsp;<i class="fa fa-walking"></i>&nbsp;${g.count ?? 0}회`;

            // 클릭 시 한 칸 확대 + 그 위치로 센터 이동
            div.addEventListener('click', () => {
              const latlng = new window.kakao.maps.LatLng(coords.lat, coords.lng);
              map.setCenter(latlng);
              map.setLevel(Math.max(1, map.getLevel() - 1));
            });

            overlay = new window.kakao.maps.CustomOverlay({
              position: new window.kakao.maps.LatLng(coords.lat, coords.lng),
              content: div,
              yAnchor: 1,
              zIndex: 1,
            });

            // hover
            div.addEventListener('mouseenter', () => {
              div.style.transform = 'scale(1.25)';
              overlay.setZIndex(9999);
            });
            div.addEventListener('mouseleave', () => {
              div.style.transform = 'scale(1)';
              overlay.setZIndex(1);
            });

            globalThis._aggOverlays.set(key, overlay);
          } else {
            overlay.setPosition(new window.kakao.maps.LatLng(coords.lat, coords.lng));
          }

          overlay.setMap(map);
        });
      }

      // 항상 ‘현재 화면에 보이는 디테일들’을 리스트 상태로 전달 (모드 무관)
      setCenterAddr(visibleDetails);
    };

    draw();
    const idle = () => draw();
    window.kakao.maps.event.addListener(map, 'idle', idle);
    return () => window.kakao.maps.event.removeListener(map, 'idle', idle);
  }, [mapData, mapReady, setCenterAddr, theme, searchCondition]);

  return (
    <>
      <MapBox>
        {user?.role === 'ADMIN' && (
          <ExcelSection>
            <ExcelButton onClick={handleExcelUploadClick}>
              <AiOutlineUpload /> 추가
            </ExcelButton>
            <input
              type="file"
              ref={fileInputRef}
              accept=".xlsx,.xls"
              style={{ display: 'none' }}
              onChange={handleExcelFileChange}
            />
            <DeleteInput
              value={deleteText}
              type="text"
              onChange={e => setDeleteText(e.target.value)}
              placeholder="삭제키를 입력해주세요."
            />
            <ExcelButton
              onClick={() => {
                confirmAlert({
                  message: '해당 엑셀을 삭제하시겠습니까?',
                  buttons: [
                    {
                      label: '삭제',
                      onClick: async () => {
                        try {
                          await api.delete(`/cham/upload/${deleteText}`);
                          toast.success('엑셀 삭제가 완료되었습니다.');
                          await handleSearch();
                        } catch (e) {
                          console.error(e);
                          const msg = e.response?.data?.message ?? '삭제 실패';
                          toast.error(msg);
                        }
                      },
                    },
                    { label: '취소', onClick: () => {} },
                  ],
                });
              }}
            >
              <AiOutlineDelete /> 삭제
            </ExcelButton>
          </ExcelSection>
        )}
        <MapContainer id="map" />
      </MapBox>

      <Modal open={open} onClose={() => setOpen(false)} title="상세보기">
        <DetailPage initialParams={detailParams} />
      </Modal>
    </>
  );
}

/* ===== styled ===== */
const MapBox = styled.div`
  width: 100%;
  height: 100%;
  position: relative;
`;
const MapContainer = styled.div`
  width: 100%;
  height: 100%;
`;
const ExcelSection = styled.div`
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translate(-50%, 0);
  display: flex;
  background-color: #ffffff;
  border-radius: 999px;
  border: 2px solid ${({ theme }) => theme.colors.primary};
  justify-content: center;
  align-items: center;
  padding: 4px;
  gap: 8px;
  z-index: 2;
`;
const ExcelButton = styled.button`
  display: flex;
  align-items: center;
  background: ${({ color, theme }) => color || theme.colors.primary};
  border: none;
  color: white;
  font-weight: bold;
  font-size: ${({ theme }) => theme.sizes.medium};
  padding: 10px 16px;
  border-radius: 999px;
  cursor: pointer;
  white-space: nowrap;
  gap: 6px;
  svg {
    width: 20px;
    height: 20px;
  }
  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.sizes.small};
    padding: 6px 12px;
  }
`;
const DeleteInput = styled.input`
  border: none;
  background: unset;
  padding: 4px;
  &:focus {
    outline: none;
    border-bottom: 2px solid ${({ theme }) => theme.colors.primary};
  }
`;
