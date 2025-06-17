import { useEffect } from 'react';
import styled, { useTheme } from 'styled-components';

export default function MapPanel() {
  const theme = useTheme();
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

        const bounds = new window.kakao.maps.LatLngBounds();
        bounds.extend(new window.kakao.maps.LatLng(36.461, 127.275)); // 북서쪽
        bounds.extend(new window.kakao.maps.LatLng(36.281, 127.493)); // 남동쪽
        map.setBounds(bounds);

        const geocoder = new window.kakao.maps.services.Geocoder();

        const data = [
          { address: '대전광역시 서구 둔산로 100', amount: 123000 },
          { address: '대전광역시 유성구 대학로 291', amount: 54000 },
          { address: '대전광역시 중구 중앙로 120', amount: 87000 },
        ];

        data.forEach(({ address, amount }) => {
          geocoder.addressSearch(address, (result, status) => {
            if (status === window.kakao.maps.services.Status.OK) {
              const coords = new window.kakao.maps.LatLng(result[0].y, result[0].x);

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
                content: content,
                yAnchor: 1,
              });

              overlay.setMap(map);
            }
          });
        });
      });
    };

    loadScript().then(() => {
      window.kakao.maps.load(() => {
        initMap();
      });
    });
  }, []);

  return <MapBox id="map" />;
}

const MapBox = styled.div`
  width: 100%;
  height: 100%;
  border-radius: 8px;
`;
