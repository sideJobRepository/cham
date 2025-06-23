import styled, { useTheme } from 'styled-components';
import SearchBar from '../components/SearchBar';
import Sidebar from '../components/Sidebar';
import MapPanel from '../components/MapPanel';
import { useState, useEffect, useRef } from 'react';
import { FaFileExcel } from 'react-icons/fa';
import { useRecoilState, useRecoilValue } from 'recoil';
import { mapSearchFilterState, userState } from '@/recoil/appState.js';
import { AiOutlineDelete } from 'react-icons/ai';
import { toast } from 'react-toastify';
import { confirmAlert } from 'react-confirm-alert';
import api from '@/utils/axiosInstance.js';
import { useMapSearch } from '@/recoil/fetchAppState.js';

export default function MainPage() {
  const theme = useTheme();
  const [isMobile, setIsMobile] = useState(false);
  const user = useRecoilValue(userState);

  //삭제키
  const [deleteText, setDeleteText] = useState('');

  const fileInputRef = useRef(null);

  const mapSearch = useMapSearch();
  const [searchCondition, setSearchCondition] = useRecoilState(mapSearchFilterState);

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
      toast.error('엑셀 업로드 실패');
    } finally {
      e.target.value = '';
    }
  };

  useEffect(() => {
    const checkMobile = () => {
      const result = window.matchMedia(theme.device.mobile).matches;
      setIsMobile(result);
    };

    checkMobile();
    window.addEventListener('resize', checkMobile);

    return () => {
      window.removeEventListener('resize', checkMobile);
    };
  }, [theme.device.mobile]);

  return (
    <MainPageWrapper>
      <ContentSection>
        {isMobile ? (
          <>
            <ListSection>
              <SearchBar />
              <MapSection>
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
                      onChange={e => setDeleteText(e.target.value)}
                      type="text"
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
                                  await api.delete(`/cham/reply/${deleteText}`);
                                  toast.success('엑셀 삭제가 완료되었습니다.');
                                  await handleSearch();
                                } catch (e) {
                                  toast.error('엑셀 삭제가 실패했습니다.');
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
                <MapPanel />
              </MapSection>
              <ListContent>
                <Sidebar />
              </ListContent>
            </ListSection>
          </>
        ) : (
          <>
            <ListSection>
              <SearchBar />
              <ListContent>
                <Sidebar />
              </ListContent>
            </ListSection>
            <MapSection>
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
                                toast.error('엑셀 삭제가 실패했습니다.');
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
              <MapPanel />
            </MapSection>
          </>
        )}
      </ContentSection>
    </MainPageWrapper>
  );
}

const MainPageWrapper = styled.div`
  margin: 0 auto;
  height: 100%;
  padding: 20px 0;
`;

const ContentSection = styled.div`
  display: flex;
  height: 100%;
  flex: 1;
  overflow: hidden;
  @media screen and ${({ theme }) => theme.device.mobile} {
    flex-direction: column;
    height: auto;
  }
`;

const ListSection = styled.div`
  display: flex;
  flex-direction: column;
  width: 50%;
  height: 100%;
  padding: 10px;
  @media ${({ theme }) => theme.device.mobile} {
    width: 100%;
    height: auto;
  }
`;

const ListContent = styled.div`
  flex: 1;
  overflow-y: auto;
  margin-top: 20px;

  @media ${({ theme }) => theme.device.mobile} {
    overflow-y: visible;
  }
`;

const MapSection = styled.div`
  flex: 1;
  height: 90%;
  border-radius: 8px;
  padding: 0 12px;
  @media ${({ theme }) => theme.device.mobile} {
    flex: none;
    height: 400px;
    padding: 0;
    margin-top: 20px;
    overflow: hidden;
  }
`;

const ExcelSection = styled.div`
  width: 100%;
  height: 10%;
  padding: 10px;
  display: flex;
  gap: 8px;
  justify-content: right;
  @media ${({ theme }) => theme.device.mobile} {
    padding: 0;
    margin-bottom: 20px;
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
