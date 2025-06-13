import { useEffect } from 'react';
import styled from 'styled-components';

export default function MapPanel() {
  useEffect(() => {
    const scriptLoad = async () => {
      if (!window.kakao || !window.kakao.maps) return;

      window.kakao.maps.load(() => {
        const container = document.getElementById('map');
        const options = {
          center: new window.kakao.maps.LatLng(36.3504, 127.3845), // 대전
          level: 3,
        };

        new window.kakao.maps.Map(container, options);
      });
    };

    if (window.kakao && window.kakao.maps) {
      scriptLoad();
    } else {
      const interval = setInterval(() => {
        if (window.kakao && window.kakao.maps) {
          clearInterval(interval);
          scriptLoad();
        }
      }, 300);
    }
  }, []);

  return <MapBox id="map" />;
}

const MapBox = styled.div`
  width: 100%;
  height: 100%;
  border-radius: 8px;
`;
