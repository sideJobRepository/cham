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

// === ADD: 상세표시 임계 줌 레벨 ===
const DETAIL_MAX_LEVEL = 6;

// === ADD: content 구성 함수 ===
function renderOverlayContent({ div, raw, amount, visits, map, theme }) {
  const level = map.getLevel();
  if (level <= DETAIL_MAX_LEVEL) {
    // 상세(금액 + 방문횟수)
    div.innerHTML =
      `<span>${(raw.totalSum ?? amount)?.toLocaleString()}원&nbsp;&nbsp;&nbsp;</span>` +
      `<i class="fa fa-walking"></i>&nbsp;${raw.visits ?? visits}`;
  } else {
    // 거점(간단)
    div.innerHTML = `<i class="fa fa-map-marker"></i>`;
  }
}

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
    fileInputRef.current?.click();
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

        const zoomControl = new window.kakao.maps.ZoomControl();
        map.addControl(zoomControl, window.kakao.maps.ControlPosition.RIGHT);

        window.mapInstance = map;

        const bounds = new window.kakao.maps.LatLngBounds();
        bounds.extend(new window.kakao.maps.LatLng(36.461, 127.275));
        bounds.extend(new window.kakao.maps.LatLng(36.281, 127.493));
        map.setBounds(bounds);

        setMapReady(true);
      });
    };

    loadScript().then(() => {
      window.kakao.maps.load(() => {
        initMap();
      });
    });
  }, []);

  useEffect(() => {
    if (!window.kakao?.maps || !window.mapInstance || !mapReady) return;
    if (!mapData) return;

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

    if (!globalThis._geoCache) globalThis._geoCache = new globalThis.Map();
    if (globalThis._geocodingBusy == null) globalThis._geocodingBusy = false;

    const doOnce = async () => {
      if (!globalThis._geoCache) globalThis._geoCache = new Map();
      if (!globalThis._overlays) globalThis._overlays = new Map();

      const points = Object.values(mapData || {})
        .map(item => ({
          address: item.addrDetail,
          amount: item.totalSum,
          raw: item,
          visits: item.visits?.length ?? 0,
        }))
        .filter(p => !!p.address);

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

      const need = points.filter(p => !globalThis._geoCache.has(p.address)).map(p => p.address);

      const MAX_FIRST_BATCH = 40;
      const first = need.slice(0, MAX_FIRST_BATCH);
      const rest = need.slice(MAX_FIRST_BATCH);
      const ordered = [...first, ...rest];

      const geocoder = new window.kakao.maps.services.Geocoder();
      const CONCURRENCY = 40;
      const BATCH_DELAY = 50;
      const MAX_RETRY = 2;
      const BASE_DELAY = 120;

      const sleep = ms => new Promise(r => setTimeout(r, ms));

      const geocodeOne = async addr => {
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

          if (attempt < MAX_RETRY) {
            await sleep(BASE_DELAY * (attempt + 1));
          } else {
            globalThis._geoCache.set(addr, null);
          }
        }
      };

      const runQueue = async addresses => {
        if (!addresses || addresses.length === 0) return;
        const isFirst = firstGeocodeRef.current;
        let toastId;

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
          firstGeocodeRef.current = false;
        }
      };

      await runQueue(ordered);

      const visibleItems = [];
      for (const { address, amount, visits, raw } of points) {
        const cached = globalThis._geoCache.get(address);
        if (!cached?.lat || !cached?.lng) continue;

        let entry = globalThis._overlays.get(address);
        if (!entry) {
          const div = document.createElement('div');
          div.style.cssText = `
      display:flex;align-items:center;justify-content:center;
      background:${theme.colors.primary};color:white;padding:10px;
      border-radius:20px;font-weight:bold;font-size:${theme.sizes.medium};
      white-space:nowrap;box-shadow:0 2px 6px rgba(0,0,0,.3);height:30px;cursor:pointer;
      will-change: transform; transform: translateZ(0);
    `;

          // === ADD: content 구성 ===
          renderOverlayContent({ div, raw, amount, visits, map, theme });

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

          // === ADD: div를 entry에 보관 ===
          entry = { overlay, lat: cached.lat, lng: cached.lng, raw, isVisible: false, el: div };
          globalThis._overlays.set(address, entry);
        } else {
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

        // === ADD: 보이는 항목은 매번 content 갱신 ===
        if (show && entry.el) {
          renderOverlayContent({ div: entry.el, raw: entry.raw, amount, visits, map, theme });
        }

        if (show) visibleItems.push(raw);
      }

      setCenterAddr(visibleItems);
    };

    doOnce();
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
    border-bottom: 2px solid ${({ theme }) => theme.colors.primary};
  }
`;
