import { useEffect, useRef, useState } from 'react';
import { useFetchUserList, useMapSearch } from '@/recoil/fetchAppState.js';
import styled from 'styled-components';
import { toast } from 'react-toastify';
import api from '@/utils/axiosInstance.js';
import { AiOutlineUpload, AiOutlineDelete } from 'react-icons/ai';
import { useRecoilValue } from 'recoil';
import { mapSearchFilterState } from '@/recoil/appState.js';
import { useUserListState } from '@/recoil/useAppState.js';
import Pagination from '@/components/Pagination.jsx';
import { showConfirmModal } from '@/components/ConfirmAlert.jsx';
import { BiUser } from 'react-icons/bi';
import { BiMap } from 'react-icons/bi';

export default function AdminModal() {
  const userListFetch = useFetchUserList();
  const userListData = useUserListState();

  const [checkedIds, setCheckedIds] = useState([]);

  const [page, setPage] = useState(0);
  const totalPages = userListData?.userData?.totalPages ?? 0;

  const [roleMap, setRoleMap] = useState({});

  const fileInputRef = useRef(null);
  const [deleteText, setDeleteText] = useState('');
  const mapSearch = useMapSearch();

  const searchCondition = useRecoilValue(mapSearchFilterState);

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

    const toastId = toast.loading('엑셀 파일을 업로드 하는 중입니다.');
    try {
      await api.post('/cham/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      toast.update(toastId, {
        render: '엑셀 파일 업로드가 완료 되었습니다.',
        type: 'success',
        isLoading: false,
        autoClose: 1000,
      });
      await handleSearch();
    } catch (err) {
      console.error(err);
      toast.update(toastId, {
        render: err.response?.data?.message ?? '엑셀 업로드 실패',
        type: 'error',
        isLoading: false,
        autoClose: 1000,
      });
    } finally {
      e.target.value = '';
    }
  };

  //권한 수정
  const handleRoleSubmit = async () => {
    showConfirmModal({
      title: <>권한 변경</>,
      message: <>권한을 변경하시겠습니까?</>,
      gb: true,
      firstText: '취소',
      secondText: '확인',
      onConfirm: async () => {
        const selected = checkedIds.map(id => ({
          memberRoleId: id,
          roleId: roleMap[id] ?? items.content.find(i => i.memberId === id)?.roleId,
        }));

        if (selected.length === 0) {
          toast.error('선택된 행이 없습니다.');
          return;
        }

        try {
          await api.put(`/cham/role`, selected, {});
          setCheckedIds([]);
          setRoleMap([]);
          await userListFetch(page);
          toast.success('권한 수정이 완료되었습니다.');
        } catch (e) {
          console.log('error', e);
          toast.error('권한 수정이 실패하였습니다.');
        }
      },
    });
  };

  const handleSearch = () => {
    const params = {
      cardOwnerPositionId: searchCondition.selectedRole?.value,
      input: searchCondition.input,
      sortOrder: searchCondition.sortOrder,
      addrDetail: '',
    };
    mapSearch(params);
  };

  useEffect(() => {
    //유저리스트 조회
    userListFetch(page);
  }, [page]);

  return (
    <AdminWrapper>
      <ExcelSection>
        <input
          type="file"
          ref={fileInputRef}
          accept=".xlsx,.xls"
          style={{ display: 'none' }}
          onChange={handleExcelFileChange}
        />
        <h2>
          <BiMap />
          맛집지도 업로드
        </h2>
        <ButtonBox>
          <DeleteInput
            value={deleteText}
            type="text"
            onChange={e => setDeleteText(e.target.value)}
            placeholder="삭제키를 입력해주세요."
          />
          <div>
            <ExcelButton color="#1A7D55" onClick={handleExcelUploadClick}>
              <AiOutlineUpload /> 추가
            </ExcelButton>
            <ExcelButton
              color="#FF5E57"
              onClick={() => {
                showConfirmModal({
                  title: <>엑셀 삭제</>,
                  message: <>해당 엑셀을 삭제하시겠습니까?</>,
                  gb: false,
                  firstText: '취소',
                  secondText: '삭제',
                  onConfirm: async () => {
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
                });
              }}
            >
              <AiOutlineDelete /> 삭제
            </ExcelButton>
          </div>
        </ButtonBox>
      </ExcelSection>
      <TableSection>
        <ButtonBox>
          <h2>
            <BiUser />
            회원관리
          </h2>
          <Button onClick={() => handleRoleSubmit()}>저장</Button>
        </ButtonBox>
        <TableScrollBox>
          <Table>
            <thead>
              <tr>
                <Th style={{ width: '50px' }}> </Th>
                <Th style={{ width: '200px' }}>이메일</Th>
                <Th style={{ width: '100px' }}>이름</Th>
                <Th style={{ width: '120px' }}>연락처</Th>
                <Th style={{ width: '150px' }}>권한</Th>
              </tr>
            </thead>
            <tbody>
              {userListData?.userData?.content.map(item => {
                return (
                  <tr key={item.memberEmail}>
                    <Td>
                      <input
                        type="checkbox"
                        checked={checkedIds.includes(item.memberId)}
                        onChange={e => {
                          const isChecked = e.target.checked;
                          setCheckedIds(prev =>
                            isChecked
                              ? [...prev, item.memberId]
                              : prev.filter(id => id !== item.memberId)
                          );
                        }}
                      />
                    </Td>
                    <Td color="#222">{item.memberEmail}</Td>
                    <Td color="#222">{item.memberName}</Td>
                    <Td color="#222">{item.memberPhoneNo}</Td>
                    <Td color="#222">
                      <div>
                        <label>
                          <input
                            type="radio"
                            name={`role-${item.memberId}`}
                            value="1"
                            checked={(roleMap[item.memberId] ?? item.roleId) === 1}
                            onChange={() => setRoleMap(prev => ({ ...prev, [item.memberId]: 1 }))}
                          />
                          관리자
                        </label>
                        <label style={{ marginLeft: '12px' }}>
                          <input
                            type="radio"
                            name={`role-${item.memberId}`}
                            value="2"
                            checked={(roleMap[item.memberId] ?? item.roleId) === 2}
                            onChange={() => setRoleMap(prev => ({ ...prev, [item.memberId]: 2 }))}
                          />
                          일반
                        </label>
                      </div>
                    </Td>
                  </tr>
                );
              })}
            </tbody>
          </Table>
        </TableScrollBox>
        <PaginationSetion>
          <Pagination current={page} totalPages={totalPages} onChange={p => setPage(p)} />
        </PaginationSetion>
      </TableSection>
    </AdminWrapper>
  );
}
const AdminWrapper = styled.section`
  display: flex;
  flex-direction: column;
  gap: 40px;
  height: 100%;
  margin: 0 auto;
  overflow-y: hidden;
  padding: 40px;
  @media screen and ${({ theme }) => theme.device.mobile} {
    max-width: 100%;
    min-width: 100%;
    min-height: unset;
    overflow-y: auto;
  }

  h2 {
    display: flex;
    align-items: center;
    gap: 8px;
    color: ${({ theme }) => theme.colors.topLine};
    font-size: ${({ theme }) => theme.sizes.large};
    font-weight: bold;

    svg {
      width: 24px;
      height: 24px;
    }
  }
`;

const ExcelSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 2;
`;
const ExcelButton = styled.button`
  display: flex;
  align-items: center;
  background: ${({ color }) => color};
  border: none;
  color: white;
  font-weight: bold;
  font-size: ${({ theme }) => theme.sizes.medium};
  padding: 10px 16px;
  border-radius: 4px;
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
  flex: 1;
  min-width: 0;

  padding: 4px;
  &:focus {
    outline: none;
    border-bottom: 2px solid ${({ theme }) => theme.colors.primary};
  }
`;

export const TableSection = styled.div`
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  margin: 0 auto;
`;

export const TableScrollBox = styled.div`
  @media ${({ theme }) => theme.device.mobile} {
    width: 100%;
    overflow-x: auto;
  }
`;

export const Table = styled.table`
  width: 100%;
  border-collapse: collapse;
  white-space: nowrap;
  font-size: ${({ theme }) => theme.sizes.medium};
  table-layout: fixed;

  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.sizes.small};
  }

  th,
  td {
    padding: 14px;
    text-align: center;
  }
  tbody tr {
    cursor: pointer;
  }
  td {
    border-bottom: 1px solid #ffffff26;
  }
`;

export const Th = styled.th`
  background-color: ${({ theme }) => theme.colors.liteGray};
  color: ${({ theme }) => theme.colors.bg};
`;

export const Td = styled.td`
  color: ${({ color }) => color};
  overflow: hidden;

  input[type='checkbox'] {
    accent-color: ${({ theme }) => theme.colors.primary};
    cursor: pointer;
  }

  div {
    display: flex;
    align-items: center;
    justify-content: center;
    label {
      display: flex;
      gap: 4px;

      input {
        margin-right: 6px;
        accent-color: ${({ theme }) => theme.colors.primary};
        cursor: pointer;
      }
    }
  }
`;

export const PaginationSetion = styled.section`
  display: flex;
  width: 100%;
  height: 76px;
  padding: 20px 0;
`;

const ButtonBox = styled.div`
  display: flex;
  width: 100%;
  gap: 12px;
  justify-content: space-between;
  margin-bottom: 10px;
  align-items: center;

  > div {
    display: flex;
    gap: 8px;
  }
`;

const Button = styled.button`
  padding: 10px 16px;
  background-color: ${({ theme }) => theme.colors.primary};
  color: #ffffff;
  font-weight: bold;
  font-size: ${({ theme }) => theme.sizes.medium};
  border: none;
  border-radius: 4px;
  cursor: pointer;

  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.sizes.small};
  }
`;
