import { useEffect, useState } from 'react';
import styled, { useTheme } from 'styled-components';
import { useSearchMapState } from '@/recoil/useAppState.js';
import { useRecoilValue, useSetRecoilState } from 'recoil';
import {
  mapCenterAddrState,
  mapSearchFilterState,
  selectedCardDataState,
} from '@/recoil/appState.js';
import { useNavigate } from 'react-router-dom';

export default function MapPanel() {
  const theme = useTheme();
  const { mapData } = useSearchMapState();
  const setCenterAddr = useSetRecoilState(mapCenterAddrState);
  const [mapReady, setMapReady] = useState(false);

  const navigate = useNavigate();
  const setSelectedCard = useSetRecoilState(selectedCardDataState);
  const searchCondition = useRecoilValue(mapSearchFilterState);

  // 최초 지도 생성
  useEffect(() => {
    const loadScript = () => {
      return new Promise(resolve => {
        const script = document.createElement('script');
        script.src =
          'https://dapi.kakao.com/v2/maps/sdk.js?appkey=1519e43e7b03b773d886b2b894b783b1&autoload=false&libraries=services';
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
    //지도 준비
    if (!window.kakao?.maps || !window.mapInstance || !mapReady) {
      return;
    }

    //map 데이터 준비
    if (!mapData) {
      return;
    }

    //검색결과가 없을 경우
    if (Object.keys(mapData).length === 0) {
      setCenterAddr([]);
    }

    const map = window.mapInstance;
    const geocoder = new window.kakao.maps.services.Geocoder();

    //마커생성
    const renderAndSaveVisibleMarkers = () => {
      // 기존 마커 제거
      if (!window.customOverlays) window.customOverlays = [];
      window.customOverlays.forEach(o => o.setMap(null));
      window.customOverlays = [];

      const bounds = map.getBounds();
      const sw = bounds.getSouthWest();
      const ne = bounds.getNorthEast();

      const points = Object.values(mapData || {}).map(item => ({
        address: item.addrDetail,
        amount: item.totalSum,
        raw: item,
      }));

      const visibleItems = [];
      let completed = 0;

      points.forEach(({ address, amount, raw }) => {
        geocoder.addressSearch(address, (result, status) => {
          completed++;

          if (status === window.kakao.maps.services.Status.OK && result[0]) {
            const lat = parseFloat(result[0].y);
            const lng = parseFloat(result[0].x);
            const coords = new window.kakao.maps.LatLng(lat, lng);

            if (
              lat >= sw.getLat() &&
              lat <= ne.getLat() &&
              lng >= sw.getLng() &&
              lng <= ne.getLng()
            ) {
              visibleItems.push(raw);

              const div = document.createElement('div');
              div.style.cssText = `
            display: flex;
            align-items: center;
            justify-content: center;
            background: ${theme.colors.primary};
            color: white;
            padding: 5px 12px;
            border-radius: 20px;
            font-weight: bold;
            font-size: ${theme.sizes.medium};
            white-space: nowrap;
            box-shadow: 0 2px 6px rgba(0,0,0,0.3);
            height: 30px;
            cursor: pointer;
          `;
              div.textContent = `${amount.toLocaleString()}원`;

              // 클릭 시 상세 페이지로 이동
              div.addEventListener('click', () => {
                const query = new URLSearchParams({
                  cardOwnerPositionId: searchCondition.selectedRole?.value,
                  cardUseName: searchCondition.cardUseName,
                  numberOfVisits: searchCondition.numberOfVisits,
                  startDate: searchCondition.startDate?.toISOString().split('T')[0],
                  endDate: searchCondition.endDate?.toISOString().split('T')[0],
                  sortOrder: searchCondition.sortOrder,
                  addrDetail: raw.addrDetail,
                  detail: true,
                  // 필요한 필드 추가
                }).toString();
                window.open(`/detail?${query}`, '_blank');
              });

              const overlay = new window.kakao.maps.CustomOverlay({
                position: coords,
                content: div,
                yAnchor: 1,
              });
              overlay.setMap(map);
              window.customOverlays.push(overlay);
            }
          }

          if (completed === points.length) {
            setCenterAddr(visibleItems);
          }
        });
      });
    };

    renderAndSaveVisibleMarkers();

    const idleHandler = () => renderAndSaveVisibleMarkers();
    window.kakao.maps.event.addListener(map, 'idle', idleHandler);

    return () => {
      window.kakao.maps.event.removeListener(map, 'idle', idleHandler);
    };
  }, [mapData, mapReady, setCenterAddr, theme]);

  return <MapBox id="map" />;
}

const MapBox = styled.div`
  width: 100%;
  height: 100%;
  border-radius: 8px;
`;
