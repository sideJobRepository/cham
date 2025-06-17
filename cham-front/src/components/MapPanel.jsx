import { useEffect } from 'react';
import styled from 'styled-components';

export default function MapPanel() {
  useEffect(() => {
    const loadScript = () => {
      return new Promise(resolve => {
        const script = document.createElement('script');
        script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=1519e43e7b03b773d886b2b894b783b1&autoload=false&libraries=services`;
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
        geocoder.addressSearch('대전광역시 서구 둔산로 100', (result, status) => {
          if (status === window.kakao.maps.services.Status.OK) {
            const coords = new window.kakao.maps.LatLng(result[0].y, result[0].x);
            const markerImage = new window.kakao.maps.MarkerImage(
              'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_red.png',
              new window.kakao.maps.Size(40, 45),
              { offset: new window.kakao.maps.Point(20, 45) }
            );

            new window.kakao.maps.Marker({
              map,
              position: coords,
              image: markerImage,
            });

            map.setCenter(coords); // 마커 기준 중심 이동
          }
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
