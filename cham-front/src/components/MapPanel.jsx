import { useEffect, useRef, useState } from 'react';
import styled, { useTheme } from 'styled-components';
import { useSearchMapState } from '@/recoil/useAppState.js';
import { useRecoilState, useRecoilValue, useSetRecoilState } from 'recoil';

import {
  mapCenterAddrState,
  mapSearchFilterState,
  selectedCardDataState,
  userState,
} from '@/recoil/appState.js';
import { useNavigate } from 'react-router-dom';
import { FaFileExcel } from 'react-icons/fa';
import { confirmAlert } from 'react-confirm-alert';
import api from '@/utils/axiosInstance.js';
import { toast } from 'react-toastify';
import { AiOutlineDelete } from 'react-icons/ai';
import { useMapSearch } from '@/recoil/fetchAppState.js';

export default function MapPanel() {
  const theme = useTheme();
  const { mapData } = useSearchMapState();
  const setCenterAddr = useSetRecoilState(mapCenterAddrState);
  const [mapReady, setMapReady] = useState(false);

  const searchCondition = useRecoilValue(mapSearchFilterState);

  const user = useRecoilValue(userState);

  //삭제키
  const [deleteText, setDeleteText] = useState('');

  const fileInputRef = useRef(null);

  const mapSearch = useMapSearch();

  const handleSearch = () => {
    const rawAmount = searchCondition.numberOfVisits?.replace(/,/g, '');
    const params = {
      cardOwnerPositionId: searchCondition.selectedRole?.value,
      cardUseName: searchCondition.cardUseName,
      numberOfVisits: parseInt(rawAmount, 10),
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
        replies: item.replies.length,
      }));

      const visibleItems = [];
      let completed = 0;

      points.forEach(({ address, amount, replies, raw }) => {
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
              div.innerHTML = `${amount.toLocaleString()}원&nbsp;&nbsp;&nbsp;<i class="fa fa-comment"></i>&nbsp;${replies}`;

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

              div.addEventListener('mouseenter', () => {
                div.style.opacity = '0.8';
              });
              div.addEventListener('mouseleave', () => {
                div.style.opacity = '1';
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

  return (
    <>
      <MapBox>
        {user?.role === 'ADMIN' && (
          <ExcelSection>
            <ExcelButton onClick={handleExcelUploadClick}>
              <FaFileExcel size={16} />
              엑셀 업로드
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
              placeholder="삭제키를 입력해주세요"
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
              <AiOutlineDelete size={20} />
              엑셀 삭제
            </ExcelButton>
          </ExcelSection>
        )}
        <Map $hasHeader={user?.role === 'ADMIN'} id="map" />
      </MapBox>
    </>
  );
}

const MapBox = styled.div`
  width: 100%;
  height: 100%;
`;

const Map = styled.div`
  width: 100%;
  height: ${({ $hasHeader }) => ($hasHeader ? 'calc(100% - 60px)' : '100%')};
  border-radius: 8px;
`;

const ExcelSection = styled.div`
  width: 100%;
  height: 60px;
  padding: 10px;
  display: flex;
  gap: 8px;
  justify-content: right;
  @media ${({ theme }) => theme.device.mobile} {
    padding: 0;
    height: 40px;
    width: 100%;
    margin-bottom: 20px;
    justify-content: center;
  }
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
  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.sizes.small};
    padding: 6px 12px;
  }
`;

const DeleteInput = styled.input`
  border: none;
  border-bottom: 2px solid ${({ theme }) => theme.colors.primary};
  padding: 4px;
  &:focus {
    outline: none;
    border-bottom: 2px solid ${({ theme }) => theme.colors.primary}; // 선택적으로 재지정
  }
`;
