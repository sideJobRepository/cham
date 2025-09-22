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

export default function MapPanel() {
  const theme = useTheme();
  const { mapData } = useSearchMapState();
  const setCenterAddr = useSetRecoilState(mapCenterAddrState);
  const [mapReady, setMapReady] = useState(false);

  const searchCondition = useRecoilValue(mapSearchFilterState);

  const user = useRecoilValue(userState);

  //지도 로딩
  const firstGeocodeRef = useRef(true);

  //삭제키
  const [deleteText, setDeleteText] = useState('');

  const fileInputRef = useRef(null);

  const mapSearch = useMapSearch();

  //모달
  const [open, setOpen] = useState(false);
  const [detailParams, setDetailParams] = useState(null);

  const handleSearch = () => {
    const rawAmount = searchCondition.numberOfVisits?.replace(/,/g, '');
    const params = {
      cardOwnerPositionId: searchCondition.selectedRole?.value,
      cardUseName: searchCondition.cardUseName,
      numberOfVisits: parseInt(rawAmount, 10),
      addrName: searchCondition.addrName,
      startDate: searchCondition.startDate?.toISOString().split('T')[0],
      endDate: searchCondition.endDate?.toISOString().split('T')[0],
      sortOrder: searchCondition.sortOrder,
      addrDetail: '',
    };

    mapSearch(params);
  };

  const handleExcelUploadClick = () => {
    fileInputRef.current?.click(); // 엑셀 업로드 버튼 클릭 시 input 클릭
  };

  const handleExcelFileChange = async e => {
    const file = e.target.files[0];
    if (!file) return;

    const fileName = file.name.toLowerCase();
    if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
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
    } catch (error) {
      console.log('error', error);
      const errorMessage = error.response.data.message;
      toast.error(errorMessage);
    } finally {
      e.target.value = '';
    }
  };

  // 최초 지도 생성
  useEffect(() => {
    const loadScript = () => {
      return new Promise(resolve => {
        const script = document.createElement('script');
        const mapKey = import.meta.env.VITE_KAKAO_MAP_ID;

        script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${mapKey}&autoload=false&libraries=services`;
        script.async = true;
        document.head.appendChild(script);
        script.onload = resolve;
      });
    };

    const initMap = () => {
      window.kakao.maps.load(() => {
        const container = document.getElementById('map');
        const map = new window.kakao.maps.Map(container, {
          center: new window.kakao.maps.LatLng(36.3504, 127.3845),
          level: 9,
        });

        //확대 축소 버튼
        const zoomControl = new window.kakao.maps.ZoomControl();
        map.addControl(zoomControl, window.kakao.maps.ControlPosition.RIGHT);

        window.mapInstance = map;

        const bounds = new window.kakao.maps.LatLngBounds();
        bounds.extend(new window.kakao.maps.LatLng(36.461, 127.275));
        bounds.extend(new window.kakao.maps.LatLng(36.281, 127.493));
        map.setBounds(bounds);

        setMapReady(true); // 지도 준비 완료 표시
      });
    };

    loadScript().then(() => {
      window.kakao.maps.load(() => {
        initMap();
      });
    });
  }, []);

  useEffect(() => {
    // 지도 데이터 준비
    if (!window.kakao?.maps || !window.mapInstance || !mapReady) return;
    if (!mapData) return;

    // 검색결과 없을 때
    if (Object.keys(mapData).length === 0) {
      if (globalThis._overlays) {
        globalThis._overlays.forEach(e => e.overlay.setMap(null));
        globalThis._overlays.clear?.();
      }
      if (window.customOverlays) {
        window.customOverlays.forEach(o => o.setMap(null));
        window.customOverlays = [];
      }
      setCenterAddr([]);
      return;
    }

    const map = window.mapInstance;

    // 전역 캐시/락 (styled-components의 Map 충돌 피하려고 globalThis.Map 사용)
    if (!globalThis._geoCache) globalThis._geoCache = new globalThis.Map(); // addr -> {lat,lng} | null
    if (globalThis._geocodingBusy == null) globalThis._geocodingBusy = false;

    const doOnce = async () => {
      // 저장소 준비
      if (!globalThis._geoCache) globalThis._geoCache = new Map(); // addr -> {lat,lng}|null
      if (!globalThis._overlays) globalThis._overlays = new Map(); // addr -> {overlay,lat,lng,raw,isVisible}

      const points = Object.values(mapData || {})
        .map(item => ({
          address: item.addrDetail,
          amount: item.totalSum,
          raw: item,
          visits: item.visits?.length ?? 0,
        }))
        .filter(p => !!p.address);

      //검색결과 반영
      const addrSet = new Set(points.map(p => p.address));
      if (globalThis._overlays) {
        globalThis._overlays.forEach((entry, addr) => {
          if (!addrSet.has(addr)) {
            entry.overlay.setMap(null);
            globalThis._overlays.delete(addr);
          }
        });
      }

      const bounds = map.getBounds();
      const sw = bounds.getSouthWest();
      const ne = bounds.getNorthEast();
      const inViewByLatLng = (lat, lng) =>
        lat >= sw.getLat() && lat <= ne.getLat() && lng >= sw.getLng() && lng <= ne.getLng();

      // 1) 캐시에 없는 주소만 추리되, "뷰포트 안일 가능성 높은 것" 우선
      const need = points.filter(p => !globalThis._geoCache.has(p.address)).map(p => p.address);

      // 뷰포트 우선 정렬(대충 주소 문자열 해시로 섞임 방지 + 앞쪽 N개 먼저)
      const MAX_FIRST_BATCH = 40;
      const first = need.slice(0, MAX_FIRST_BATCH);
      const rest = need.slice(MAX_FIRST_BATCH);
      const ordered = [...first, ...rest];

      // 2) 동시성 제한 큐
      const geocoder = new window.kakao.maps.services.Geocoder();
      const CONCURRENCY = 40; // 동시 실행 수
      const BATCH_DELAY = 50; // 배치 간 휴식(ms)
      const MAX_RETRY = 2; // 실패시 재시도 횟수
      const BASE_DELAY = 120; // 재시도 backoff base(ms)

      const sleep = ms => new Promise(r => setTimeout(r, ms));

      const geocodeOne = async addr => {
        // 이미 누가 캐시했다면 스킵
        if (globalThis._geoCache.has(addr)) return;

        for (let attempt = 0; attempt <= MAX_RETRY; attempt++) {
          const coords = await new Promise(resolve => {
            geocoder.addressSearch(addr, (result, status) => {
              if (status === window.kakao.maps.services.Status.OK && result?.[0]) {
                resolve({
                  lat: parseFloat(result[0].y),
                  lng: parseFloat(result[0].x),
                });
              } else {
                resolve(null);
              }
            });
          });

          if (coords) {
            globalThis._geoCache.set(addr, coords);
            return;
          }

          // 실패 → backoff 후 재시도
          if (attempt < MAX_RETRY) {
            await sleep(BASE_DELAY * (attempt + 1)); // 120ms, 240ms...
          } else {
            globalThis._geoCache.set(addr, null); // 최종 실패도 기록해서 재폭주 방지
          }
        }
      };

      const runQueue = async addresses => {
        // 주소가 하나도 없으면(전부 캐시됨) 바로 종료: 로딩/토스트 X
        if (!addresses || addresses.length === 0) return;

        const isFirst = firstGeocodeRef.current;
        let toastId;

        // 최초에만 로딩 UI
        if (isFirst) {
          toastId = toast.loading('지도 위치를 불러오는 중 입니다.');
        }

        for (let i = 0; i < addresses.length; i += CONCURRENCY) {
          const slice = addresses.slice(i, i + CONCURRENCY);
          await Promise.all(slice.map(geocodeOne));
          if (i + CONCURRENCY < addresses.length) {
            await sleep(BATCH_DELAY);
          }
        }

        if (isFirst) {
          if (toastId) {
            toast.update(toastId, {
              render: '지도 위치를 불러왔습니다.',
              type: 'success',
              isLoading: false,
              autoClose: 1000,
            });
          }
          firstGeocodeRef.current = false; // 이후부터는 토스트 안함
        }
      };

      // 3) 실행
      await runQueue(ordered);

      // 4) 오버레이: 재사용 + 토글만
      const visibleItems = [];
      for (const { address, amount, visits, raw } of points) {
        const cached = globalThis._geoCache.get(address);
        if (!cached?.lat || !cached?.lng) continue;

        let entry = globalThis._overlays.get(address);
        if (!entry) {
          // 최초 1회 생성
          const div = document.createElement('div');
          div.style.cssText = `
      display:flex;align-items:center;justify-content:center;
      background:${theme.colors.primary};color:white;padding:5px 12px;
      border-radius:20px;font-weight:bold;font-size:${theme.sizes.medium};
      white-space:nowrap;box-shadow:0 2px 6px rgba(0,0,0,.3);height:30px;cursor:pointer;
      will-change: transform; transform: translateZ(0);
    `;
          div.innerHTML = `${(raw.totalSum ?? amount)?.toLocaleString()}원&nbsp;&nbsp;&nbsp;<i class="fa fa-walking"></i>&nbsp;${raw.visits ?? visits}`;

          div.addEventListener('click', () => {
            const params = {
              cardOwnerPositionId: searchCondition.selectedRole?.value,
              cardUseName: searchCondition.cardUseName,
              numberOfVisits: searchCondition.numberOfVisits,
              addrName: searchCondition.addrName,
              startDate: searchCondition.startDate?.toISOString().split('T')[0],
              endDate: searchCondition.endDate?.toISOString().split('T')[0],
              sortOrder: searchCondition.sortOrder,
              addrDetail: raw.addrDetail,
              detail: true,
            };
            setDetailParams(params);
            setOpen(true);
          });

          const overlay = new window.kakao.maps.CustomOverlay({
            position: new window.kakao.maps.LatLng(cached.lat, cached.lng),
            content: div,
            yAnchor: 1,
          });

          entry = { overlay, lat: cached.lat, lng: cached.lng, raw, isVisible: false };
          globalThis._overlays.set(address, entry);
        } else {
          // 좌표 최신화
          if (entry.lat !== cached.lat || entry.lng !== cached.lng) {
            entry.lat = cached.lat;
            entry.lng = cached.lng;
            entry.overlay.setPosition(new window.kakao.maps.LatLng(cached.lat, cached.lng));
          }
        }

        const show = inViewByLatLng(entry.lat, entry.lng);
        if (show && !entry.isVisible) {
          entry.overlay.setMap(map);
          entry.isVisible = true;
        } else if (!show && entry.isVisible) {
          entry.overlay.setMap(null);
          entry.isVisible = false;
        }

        if (show) visibleItems.push(raw);
      }

      setCenterAddr(visibleItems);
    };

    // 최초 1회 실행
    doOnce();

    // idle 때도 실행(락으로 동시성 제어)
    const idleHandler = () => doOnce();
    window.kakao.maps.event.addListener(map, 'idle', idleHandler);

    return () => {
      window.kakao.maps.event.removeListener(map, 'idle', idleHandler);
    };
  }, [mapData, mapReady, setCenterAddr, theme, searchCondition]);

  return (
    <>
      <MapBox>
        {user?.role === 'ADMIN' && (
          <ExcelSection>
            <ExcelButton onClick={handleExcelUploadClick}>
              <AiOutlineUpload />
              추가
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
                          const errorMessage = e.response.data.message;
                          toast.error(errorMessage);
                        }
                      },
                    },
                    {
                      label: '취소',
                      onClick: () => {},
                    },
                  ],
                });
              }}
            >
              <AiOutlineDelete />
              삭제
            </ExcelButton>
          </ExcelSection>
        )}
        <MapContainer $hasHeader={user?.role === 'ADMIN'} id="map" />
      </MapBox>
      <Modal open={open} onClose={() => setOpen(false)} title="상세보기">
        <DetailPage initialParams={detailParams} />
      </Modal>
    </>
  );
}

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
  transform: translate(-50%, -0%);
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
    border-bottom: 2px solid ${({ theme }) => theme.colors.primary}; // 선택적으로 재지정
  }
`;
