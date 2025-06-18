import { useEffect, useState } from 'react';
import styled, { useTheme } from 'styled-components';
import { useSearchMapState } from '@/recoil/useAppState.js';
import { useSetRecoilState } from 'recoil';
import { mapCenterAddrState } from '@/recoil/appState.js';

export default function MapPanel() {
  const theme = useTheme();
  const { mapData } = useSearchMapState();
  const setCenterAddr = useSetRecoilState(mapCenterAddrState);
  const [mapReady, setMapReady] = useState(false);

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
          level: 6,
        });

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
    if (!mapData || Object.keys(mapData).length === 0) {
      return;
    }

    const map = window.mapInstance;
    const geocoder = new window.kakao.maps.services.Geocoder();

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

              const content = `
                <div style="
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
                ">
                  ${amount.toLocaleString()}원
                </div>
              `;

              const overlay = new window.kakao.maps.CustomOverlay({
                position: coords,
                content,
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
