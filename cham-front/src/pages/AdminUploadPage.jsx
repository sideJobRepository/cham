import { Fragment, useEffect, useMemo, useRef, useState } from 'react';
import styled from 'styled-components';
import { useNavigate } from 'react-router-dom';
import { useRecoilValue } from 'recoil';
import { toast } from 'react-toastify';
import { FaMapMarkedAlt, FaPen, FaChevronDown, FaChevronUp } from 'react-icons/fa';
import { AiOutlineDownload } from 'react-icons/ai';
import { userState } from '@/recoil/appState.js';
import { useFetchCardUseUploadList } from '@/recoil/fetchAppState.js';
import { useCardUseUploadListState } from '@/recoil/useAppState.js';
import { showConfirmModal } from '@/components/ConfirmAlert.jsx';
import api from '@/utils/axiosInstance.js';
import AdminCleanupTab from '@/pages/AdminCleanupTab.jsx';
import AdminCollectTab from '@/pages/AdminCollectTab.jsx';

/**
 * 맛집지도 공개관리.
 *
 * 지도 데이터는 엑셀 한 건 단위로 들어오고 엑셀마다 삭제키가 붙는다.
 * 그래서 공개/비공개도 삭제키 단위로 켜고 끈다. 비공개로 두면 지도·상세·합계
 * 조회에서 통째로 빠지고, 데이터는 지워지지 않으니 언제든 되돌릴 수 있다.
 */
export default function AdminUploadPage() {
  const navigate = useNavigate();
  const user = useRecoilValue(userState);
  const isAdmin = user?.roles?.includes('ROLE_ADMIN');

  const uploadListFetch = useFetchCardUseUploadList();
  const { uploadData, uploadLoading } = useCardUseUploadListState();

  const [tab, setTab] = useState('upload'); // upload | cleanup | collect
  const [keyword, setKeyword] = useState('');
  // 전환 요청이 도는 동안 그 줄의 스위치를 잠근다. 연타로 요청이 겹치는 것을 막는다.
  const [pendingKey, setPendingKey] = useState(null);
  const [authChecked, setAuthChecked] = useState(false);
  // 이름을 고치는 중인 줄. editingKey 는 고치기 전의 삭제키(줄 식별자)다.
  const [editingKey, setEditingKey] = useState(null);
  const [editingValue, setEditingValue] = useState('');
  // 사용자 명단을 펼쳐 둔 줄들
  const [openedKeys, setOpenedKeys] = useState([]);
  // 엑셀을 만드는 중인 줄. 건수가 많으면 몇 초 걸려서 버튼을 잠근다.
  const [downloadKey, setDownloadKey] = useState(null);

  const toggleOpen = deleteKey =>
    setOpenedKeys(prev =>
      prev.includes(deleteKey) ? prev.filter(k => k !== deleteKey) : [...prev, deleteKey]
    );

  // userState 는 Layout 이 첫 API 를 쏠 때 토큰 갱신이 성공해야 채워진다.
  // 비로그인 이용자에게는 갱신 자체가 실패해서 끝까지 null 로 남는다.
  // 그래서 잠시 기다렸다가 그때도 비어 있으면 비로그인으로 보고 내보낸다.
  useEffect(() => {
    if (user) {
      setAuthChecked(true);
      return;
    }
    const timer = setTimeout(() => setAuthChecked(true), 1500);
    return () => clearTimeout(timer);
  }, [user]);

  // 이 화면을 관리자로 한 번이라도 열었는지. 로그아웃으로 권한이 사라진 것과
  // 애초에 권한 없이 들어온 것을 구분하는 데 쓴다.
  const wasAdminRef = useRef(false);
  useEffect(() => {
    if (isAdmin) wasAdminRef.current = true;
  }, [isAdmin]);

  // 주소를 직접 쳐서 들어오는 경우를 막는다. 서버도 ROLE_ADMIN 으로 막혀 있고
  // 여기는 빈 화면 대신 안내를 주기 위한 것이다.
  useEffect(() => {
    if (!authChecked || isAdmin) return;
    // 보던 중에 로그아웃한 경우라면 '로그아웃 되었습니다' 가 이미 떴다.
    // 거기에 권한 경고까지 겹쳐 띄우면 뭔가 잘못된 것처럼 보인다. 조용히 내보낸다.
    if (!wasAdminRef.current) {
      toast.error('관리자만 접근할 수 있습니다.');
    }
    navigate('/', { replace: true });
  }, [authChecked, isAdmin, navigate]);

  useEffect(() => {
    if (isAdmin) uploadListFetch().then();
  }, [isAdmin]);

  const rows = useMemo(() => {
    const list = uploadData ?? [];
    const k = keyword.trim();
    if (!k) return list;
    return list.filter(
      r =>
        r.deleteKey?.includes(k) ||
        r.positionName?.includes(k) ||
        // 사람 이름으로도 찾을 수 있게 한다. '이장우' 를 쳐서 그 업로드를 바로 집는 쪽이 빠르다.
        r.members?.some(m => m.memberName?.includes(k))
    );
  }, [uploadData, keyword]);

  const startEdit = row => {
    setEditingKey(row.deleteKey);
    setEditingValue(row.deleteKey);
  };

  const cancelEdit = () => {
    setEditingKey(null);
    setEditingValue('');
  };

  const handleRename = async row => {
    const next = editingValue.trim();

    // 그대로거나 비었으면 조용히 닫는다. 확인창까지 띄울 일이 아니다.
    if (!next || next === row.deleteKey) {
      cancelEdit();
      return;
    }

    setPendingKey(row.deleteKey);
    try {
      const { data } = await api.put('/cham/admin/card-use-uploads/delete-key', {
        deleteKey: row.deleteKey,
        newDeleteKey: next,
      });
      toast.success(data?.message ?? '변경되었습니다.');
      cancelEdit();
      await uploadListFetch();
    } catch (e) {
      console.error(e);
      // 이미 쓰는 이름이면 서버가 400 으로 막는다. 입력은 그대로 두고 고치게 한다.
      toast.error(e.response?.data?.message ?? '삭제키 변경에 실패하였습니다.');
    } finally {
      setPendingKey(null);
    }
  };

  const handleDownload = async row => {
    setDownloadKey(row.deleteKey);
    try {
      const res = await api.get('/cham/admin/card-use-uploads/excel', {
        params: { deleteKey: row.deleteKey },
        responseType: 'blob',
      });

      const url = URL.createObjectURL(new Blob([res.data], { type: res.headers['content-type'] }));
      const link = document.createElement('a');
      link.href = url;
      link.download = `${row.deleteKey}.xlsx`;
      link.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      console.error(e);
      // 실패 응답도 blob 으로 와서 e.response.data 를 그대로 읽으면 메시지가 안 나온다.
      toast.error('엑셀 내려받기에 실패하였습니다.');
    } finally {
      setDownloadKey(null);
    }
  };

  const handleToggle = row => {
    const next = !row.isPublic;

    showConfirmModal({
      title: <>{next ? '공개' : '비공개'} 전환</>,
      message: (
        <>
          '{row.deleteKey}' {row.useCount}건을
          <br />
          {next ? '지도에 공개' : '지도에서 숨김'} 처리하시겠습니까?
        </>
      ),
      gb: next,
      firstText: '취소',
      secondText: '확인',
      onConfirm: async () => {
        setPendingKey(row.deleteKey);
        try {
          const { data } = await api.put('/cham/admin/card-use-uploads/public', {
            deleteKey: row.deleteKey,
            isPublic: next,
          });
          toast.success(data?.message ?? '전환되었습니다.');
          await uploadListFetch();
        } catch (e) {
          console.error(e);
          toast.error(e.response?.data?.message ?? '전환에 실패하였습니다.');
        } finally {
          setPendingKey(null);
        }
      },
    });
  };

  if (!authChecked) {
    return (
      <Wrapper>
        <Guide>권한을 확인하고 있습니다.</Guide>
      </Wrapper>
    );
  }
  if (!isAdmin) return null;

  const publicCount = (uploadData ?? []).filter(r => r.isPublic).length;

  return (
    <Wrapper>
      <Header>
        <h2>
          <FaMapMarkedAlt />
          맛집지도 관리
        </h2>
        {tab === 'upload' && (
          <Summary>
            전체 {uploadData?.length ?? 0}건 · 공개 {publicCount}건 · 비공개{' '}
            {(uploadData?.length ?? 0) - publicCount}건
          </Summary>
        )}
      </Header>

      <Tabs>
        <Tab type="button" $on={tab === 'upload'} onClick={() => setTab('upload')}>
          공개관리
        </Tab>
        <Tab type="button" $on={tab === 'cleanup'} onClick={() => setTab('cleanup')}>
          자료정리
        </Tab>
        <Tab type="button" $on={tab === 'collect'} onClick={() => setTab('collect')}>
          수집관리
        </Tab>
      </Tabs>

      {tab === 'cleanup' && <AdminCleanupTab />}

      {tab === 'collect' && <AdminCollectTab onImported={uploadListFetch} />}

      {tab === 'upload' && (
        <>
      <Guide>
        비공개로 두면 지도와 상세, 합계에서 모두 빠집니다. 자료는 지워지지 않으니 다시 공개할 수
        있습니다. 삭제키를 누르면 알아보기 쉬운 이름으로 바꿀 수 있습니다.
      </Guide>

      <SearchInput
        type="text"
        value={keyword}
        onChange={e => setKeyword(e.target.value)}
        placeholder="삭제키 · 기관 · 사용자 이름으로 찾기"
      />

      <TableScrollBox>
        <Table>
          <thead>
            <tr>
              <Th style={{ minWidth: '260px' }}>삭제키</Th>
              <Th style={{ width: '140px' }}>기관</Th>
              <Th style={{ width: '200px' }}>사용자</Th>
              <Th style={{ width: '90px' }}>건수</Th>
              <Th style={{ width: '200px' }}>사용기간</Th>
              <Th style={{ width: '120px' }}>업로드</Th>
              <Th style={{ width: '110px' }}>공개</Th>
              <Th style={{ width: '90px' }}>엑셀</Th>
            </tr>
          </thead>
          <tbody>
            {rows.map(row => (
              <Fragment key={row.deleteKey}>
              <tr>
                <Td className="left">
                  {editingKey === row.deleteKey ? (
                    <EditBox>
                      <EditInput
                        autoFocus
                        value={editingValue}
                        maxLength={255}
                        disabled={pendingKey === row.deleteKey}
                        onChange={e => setEditingValue(e.target.value)}
                        onKeyDown={e => {
                          if (e.key === 'Enter') handleRename(row);
                          if (e.key === 'Escape') cancelEdit();
                        }}
                      />
                      <SmallButton
                        type="button"
                        $color="#1A7D55"
                        disabled={pendingKey === row.deleteKey}
                        onClick={() => handleRename(row)}
                      >
                        저장
                      </SmallButton>
                      <SmallButton type="button" $color="#66696D" onClick={cancelEdit}>
                        취소
                      </SmallButton>
                    </EditBox>
                  ) : (
                    <KeyButton type="button" onClick={() => startEdit(row)} title="눌러서 이름 변경">
                      {row.deleteKey}
                      <FaPen />
                    </KeyButton>
                  )}
                </Td>
                <Td>{row.positionName ?? '-'}</Td>
                <Td>
                  {row.members?.length ? (
                    <MemberButton type="button" onClick={() => toggleOpen(row.deleteKey)}>
                      <span>
                        {row.members[0].memberName}
                        {row.members.length > 1 ? ` 외 ${row.members.length - 1}명` : ''}
                      </span>
                      {openedKeys.includes(row.deleteKey) ? <FaChevronUp /> : <FaChevronDown />}
                    </MemberButton>
                  ) : (
                    '-'
                  )}
                </Td>
                <Td>{row.useCount?.toLocaleString()}</Td>
                <Td>
                  {row.firstUseDate ?? '-'}
                  {row.lastUseDate && row.lastUseDate !== row.firstUseDate
                    ? ` ~ ${row.lastUseDate}`
                    : ''}
                </Td>
                <Td>{row.registDate?.slice(0, 10) ?? '-'}</Td>
                <Td>
                  <ToggleBox>
                    <Toggle
                      type="button"
                      $on={row.isPublic}
                      disabled={pendingKey === row.deleteKey}
                      onClick={() => handleToggle(row)}
                      aria-label={row.isPublic ? '공개 상태' : '비공개 상태'}
                    >
                      <Knob $on={row.isPublic} />
                    </Toggle>
                    <StateText $on={row.isPublic}>
                      {row.isPublic ? '공개' : '비공개'}
                    </StateText>
                  </ToggleBox>
                </Td>
                <Td>
                  <DownloadButton
                    type="button"
                    disabled={downloadKey === row.deleteKey}
                    onClick={() => handleDownload(row)}
                    title="업로드용과 같은 형식으로 내려받기"
                  >
                    <AiOutlineDownload />
                    {downloadKey === row.deleteKey ? '생성중' : '받기'}
                  </DownloadButton>
                </Td>
              </tr>
              {openedKeys.includes(row.deleteKey) && (
                <tr>
                  <MemberTd colSpan={8}>
                    <MemberList>
                      {row.members.map(m => (
                        <MemberChip key={m.memberName}>
                          {m.memberName}
                          <em>{m.useCount?.toLocaleString()}건</em>
                        </MemberChip>
                      ))}
                    </MemberList>
                  </MemberTd>
                </tr>
              )}
              </Fragment>
            ))}
            {!uploadLoading && rows.length === 0 && (
              <tr>
                <Td colSpan={8} className="empty">
                  {keyword.trim() ? '검색 결과가 없습니다.' : '업로드된 자료가 없습니다.'}
                </Td>
              </tr>
            )}
          </tbody>
        </Table>
      </TableScrollBox>
        </>
      )}
    </Wrapper>
  );
}

const Wrapper = styled.section`
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 1100px;
  width: 100%;
  margin: 0 auto;
  padding: 40px 20px;

  h2 {
    display: flex;
    align-items: center;
    gap: 12px;
    color: ${({ theme }) => theme.colors.topLine};
    font-size: ${({ theme }) => theme.sizes.large};
    font-weight: bold;

    svg {
      width: 20px;
      height: 20px;
    }
  }
`;

const Tabs = styled.div`
  display: flex;
  gap: 2px;
  border-bottom: 2px solid ${({ theme }) => theme.colors.border};
`;

const Tab = styled.button`
  padding: 10px 24px;
  border: none;
  border-bottom: 2px solid ${({ $on, theme }) => ($on ? theme.colors.primary : 'transparent')};
  margin-bottom: -2px;
  background: none;
  color: ${({ $on, theme }) => ($on ? theme.colors.primary : theme.colors.liteGray)};
  font-size: ${({ theme }) => theme.sizes.medium};
  font-weight: bold;
  cursor: pointer;
`;

const Header = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
`;

const Summary = styled.span`
  color: ${({ theme }) => theme.colors.liteGray};
  font-size: ${({ theme }) => theme.sizes.medium};
`;

const Guide = styled.p`
  color: ${({ theme }) => theme.colors.liteGray};
  font-size: ${({ theme }) => theme.sizes.medium};
  line-height: 1.6;
`;

const SearchInput = styled.input`
  width: 260px;
  max-width: 100%;
  padding: 8px 4px;
  border: none;
  border-bottom: 1px solid ${({ theme }) => theme.colors.border};
  font-size: ${({ theme }) => theme.sizes.medium};

  &:focus {
    outline: none;
    border-bottom: 2px solid ${({ theme }) => theme.colors.primary};
  }
`;

const TableScrollBox = styled.div`
  width: 100%;
  overflow-x: auto;
`;

const Table = styled.table`
  width: 100%;
  border-collapse: collapse;
  font-size: ${({ theme }) => theme.sizes.medium};
`;

const Th = styled.th`
  padding: 12px 8px;
  background: #f5f7fa;
  color: ${({ theme }) => theme.colors.primary};
  font-weight: bold;
  white-space: nowrap;
  border-bottom: 1px solid ${({ theme }) => theme.colors.border};
`;

const Td = styled.td`
  padding: 12px 8px;
  text-align: center;
  border-bottom: 1px solid ${({ theme }) => theme.colors.border};
  word-break: break-all;

  &.left {
    text-align: left;
  }

  &.empty {
    padding: 40px 8px;
    color: ${({ theme }) => theme.colors.liteGray};
  }
`;

const KeyButton = styled.button`
  display: inline-flex;
  align-items: center;
  gap: 8px;
  max-width: 100%;
  padding: 4px 6px;
  border: none;
  border-radius: 4px;
  background: none;
  color: ${({ theme }) => theme.colors.text};
  font-size: ${({ theme }) => theme.sizes.medium};
  text-align: left;
  word-break: break-all;
  cursor: pointer;

  svg {
    width: 11px;
    height: 11px;
    flex-shrink: 0;
    opacity: 0;
    color: ${({ theme }) => theme.colors.liteGray};
  }

  &:hover {
    background: #f5f7fa;
  }

  &:hover svg {
    opacity: 1;
  }
`;

const EditBox = styled.div`
  display: flex;
  align-items: center;
  gap: 6px;
`;

const EditInput = styled.input`
  flex: 1;
  min-width: 0;
  padding: 6px 8px;
  border: 1px solid ${({ theme }) => theme.colors.primary};
  border-radius: 4px;
  font-size: ${({ theme }) => theme.sizes.medium};

  &:focus {
    outline: none;
  }
`;

const SmallButton = styled.button`
  flex-shrink: 0;
  padding: 6px 10px;
  border: none;
  border-radius: 4px;
  background: ${({ $color }) => $color};
  color: #fff;
  font-size: ${({ theme }) => theme.sizes.medium};
  font-weight: bold;
  cursor: pointer;

  &:disabled {
    opacity: 0.5;
    cursor: wait;
  }
`;

const MemberButton = styled.button`
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 100%;
  padding: 4px 6px;
  border: none;
  border-radius: 4px;
  background: none;
  color: ${({ theme }) => theme.colors.text};
  font-size: ${({ theme }) => theme.sizes.medium};
  cursor: pointer;

  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  svg {
    width: 10px;
    height: 10px;
    flex-shrink: 0;
    color: ${({ theme }) => theme.colors.liteGray};
  }

  &:hover {
    background: #f5f7fa;
  }
`;

const DownloadButton = styled.button`
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: 4px;
  background: #fff;
  color: ${({ theme }) => theme.colors.primary};
  font-size: ${({ theme }) => theme.sizes.medium};
  font-weight: bold;
  white-space: nowrap;
  cursor: pointer;

  svg {
    width: 14px;
    height: 14px;
  }

  &:hover {
    background: #f5f7fa;
  }

  &:disabled {
    opacity: 0.5;
    cursor: wait;
  }
`;

const MemberTd = styled.td`
  padding: 12px 16px;
  background: #fafbfc;
  border-bottom: 1px solid ${({ theme }) => theme.colors.border};
`;

const MemberList = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
`;

const MemberChip = styled.span`
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: 14px;
  background: #fff;
  font-size: ${({ theme }) => theme.sizes.medium};

  em {
    color: ${({ theme }) => theme.colors.liteGray};
    font-style: normal;
  }
`;

const ToggleBox = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
`;

const Toggle = styled.button`
  position: relative;
  width: 44px;
  height: 24px;
  flex-shrink: 0;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  padding: 0;
  transition: background 0.2s;
  background: ${({ $on, theme }) => ($on ? theme.colors.primary : '#c9ccd1')};

  &:disabled {
    opacity: 0.5;
    cursor: wait;
  }
`;

const Knob = styled.span`
  position: absolute;
  top: 3px;
  left: ${({ $on }) => ($on ? '23px' : '3px')};
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #fff;
  transition: left 0.2s;
`;

const StateText = styled.span`
  width: 36px;
  text-align: left;
  color: ${({ $on, theme }) => ($on ? theme.colors.primary : theme.colors.liteGray)};
  font-weight: bold;
`;
