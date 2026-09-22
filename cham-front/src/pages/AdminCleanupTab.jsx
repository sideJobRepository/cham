import { useEffect, useState } from 'react';
import styled from 'styled-components';
import { toast } from 'react-toastify';
import { FaMapPin, FaUserEdit } from 'react-icons/fa';
import { useSetRecoilState } from 'recoil';
import { loadingState } from '@/recoil/appState.js';
import { showConfirmModal } from '@/components/ConfirmAlert.jsx';
import api from '@/utils/axiosInstance.js';
import Pagination from '@/components/Pagination.jsx';

const PAGE_SIZE = 20;

/**
 * 자료정리 탭.
 *
 * 엑셀에 '어딘지 모르겠어요' 처럼 메모가 적혀 들어온 값을 고친다.
 * 검색어가 없으면 손봐야 할 것만 서버가 골라 주고, 넣으면 그 이름으로 찾는다.
 * 목록이 길어질 수 있어 브라우저에서 거르지 않고 서버에 물어본다.
 */
export default function AdminCleanupTab() {
  const setLoading = useSetRecoilState(loadingState);

  const [target, setTarget] = useState('addr'); // addr | name
  const [keyword, setKeyword] = useState('');
  const [rows, setRows] = useState([]);
  const [loaded, setLoaded] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  // 기본은 전체. 켜면 손봐야 할 것만 남는다.
  const [onlyIssues, setOnlyIssues] = useState(false);

  // 편집 중인 줄. 장소는 addrId, 이름은 표기 자체가 식별자다.
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState({ addrName: '', detailAddr: '', newName: '' });
  const [pendingId, setPendingId] = useState(null);

  const fetchRows = async (
    nextTarget = target,
    nextKeyword = keyword,
    nextPage = page,
    nextOnlyIssues = onlyIssues
  ) => {
    setLoading(true);
    setLoaded(false);
    try {
      const path = nextTarget === 'addr' ? 'addrs' : 'names';
      const params = { page: nextPage, size: PAGE_SIZE, onlyIssues: nextOnlyIssues };
      if (nextKeyword.trim()) params.keyword = nextKeyword.trim();

      const res = await api.get(`/cham/admin/cleanup/${path}`, { params });

      setRows(res.data?.content ?? []);
      setTotalPages(res.data?.totalPages ?? 0);
      setTotalElements(res.data?.totalElements ?? 0);
      setPage(res.data?.page ?? nextPage);
    } catch (e) {
      console.error(e);
      toast.error('조회에 실패하였습니다.');
      setRows([]);
      setTotalPages(0);
      setTotalElements(0);
    } finally {
      setLoaded(true);
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRows('addr', '', 0);
  }, []);

  const switchTarget = next => {
    if (next === target) return;
    setTarget(next);
    setKeyword('');
    setEditingId(null);
    setPage(0);
    fetchRows(next, '', 0);
  };

  // 수정 뒤에는 보던 페이지를 그대로 다시 읽는다. 다만 그 줄이 목록에서 빠져
  // 페이지가 통째로 비면 한 장 앞으로 물러난다.
  const reload = async () => {
    const isLastRowOnPage = rows.length === 1 && page > 0;
    await fetchRows(target, keyword, isLastRowOnPage ? page - 1 : page);
  };

  const startEditAddr = row => {
    setEditingId(row.addrId);
    setForm({ addrName: row.addrName ?? '', detailAddr: row.detailAddr ?? '', newName: '' });
  };

  const startEditName = row => {
    setEditingId(row.memberName);
    setForm({ addrName: '', detailAddr: '', newName: row.memberName ?? '' });
  };

  const cancelEdit = () => setEditingId(null);

  const saveAddr = row => {
    const addrName = form.addrName.trim();
    const detailAddr = form.detailAddr.trim();

    if (!addrName || !detailAddr) {
      toast.error('장소명과 상세주소를 모두 입력해 주세요.');
      return;
    }

    const addrChanged = detailAddr !== (row.detailAddr ?? '').trim();

    showConfirmModal({
      title: <>장소 수정</>,
      message: (
        <>
          '{row.addrName}' {row.useCount}건을 수정합니다.
          <br />
          {addrChanged ? (
            <strong>
              상세주소가 바뀌어 좌표를 다시 찾습니다. 지도의 핀 위치가 함께 움직입니다.
            </strong>
          ) : (
            '상세주소는 그대로라 좌표는 바뀌지 않습니다.'
          )}
        </>
      ),
      gb: true,
      firstText: '취소',
      secondText: '확인',
      onConfirm: async () => {
        setPendingId(row.addrId);
        try {
          const { data } = await api.put('/cham/admin/cleanup/addr', {
            addrId: row.addrId,
            addrName,
            detailAddr,
          });
          toast.success(data?.message ?? '수정되었습니다.');
          cancelEdit();
          await reload();
        } catch (e) {
          console.error(e);
          toast.error(e.response?.data?.message ?? '수정에 실패하였습니다.');
        } finally {
          setPendingId(null);
        }
      },
    });
  };

  const saveName = row => {
    const newName = form.newName.trim();

    if (!newName || newName === row.memberName) {
      cancelEdit();
      return;
    }

    showConfirmModal({
      title: <>이름 수정</>,
      message: (
        <>
          '{row.memberName}' {row.useCount}건을
          <br />'{newName}' 로 바꿉니다.
        </>
      ),
      gb: true,
      firstText: '취소',
      secondText: '확인',
      onConfirm: async () => {
        setPendingId(row.memberName);
        try {
          const { data } = await api.put('/cham/admin/cleanup/name', {
            oldName: row.memberName,
            newName,
          });
          toast.success(data?.message ?? '수정되었습니다.');
          cancelEdit();
          await reload();
        } catch (e) {
          console.error(e);
          toast.error(e.response?.data?.message ?? '수정에 실패하였습니다.');
        } finally {
          setPendingId(null);
        }
      },
    });
  };

  return (
    <>
      <TargetTabs>
        <TargetTab type="button" $on={target === 'addr'} onClick={() => switchTarget('addr')}>
          <FaMapPin /> 장소
        </TargetTab>
        <TargetTab type="button" $on={target === 'name'} onClick={() => switchTarget('name')}>
          <FaUserEdit /> 이름
        </TargetTab>
      </TargetTabs>

      <Guide>
        {target === 'addr' ? (
          <>
            지도에 그대로 노출되고 핀 위치까지 정하는 값입니다.{' '}
            <strong>상세주소를 고치면 그 주소로 좌표를 다시 찾아 핀이 옮겨집니다.</strong> 좌표를
            못 찾으면 이전 위치를 그대로 둡니다.
            <br />
            장소명에 '모르겠어요', '미상', '확인', '없다' 같은 말이 들어갔거나, 좌표가 없거나,
            상세주소가 기본값이면 표시가 붙고 맨 위로 올라옵니다.
          </>
        ) : (
          <>
            이름은 지도에 나오지 않고 관리자 명단·검색에만 쓰입니다. 다만 '최중규' 와 '최중규
            (은혜식당)' 처럼 표기가 갈리면 같은 사람이 둘로 세어집니다. 이미 있는 표기로 바꾸면 두
            묶음이 하나로 합쳐집니다.
            <br />
            '최중규 (은혜식당)' 처럼 이름 옆에 메모가 붙었거나 '미상', '?' 가 들어가면 표시가
            붙고 맨 위로 올라옵니다.
          </>
        )}
      </Guide>

      <SearchRow
        onSubmit={e => {
          e.preventDefault();
          setEditingId(null);
          fetchRows(target, keyword, 0);
        }}
      >
        <SearchInput
          type="text"
          value={keyword}
          onChange={e => setKeyword(e.target.value)}
          placeholder={
            target === 'addr' ? '장소명 · 주소로 찾기' : '이름으로 찾기'
          }
        />
        <SmallButton type="submit" $color="#093A6E">
          검색
        </SmallButton>
        <FilterLabel>
          <input
            type="checkbox"
            checked={onlyIssues}
            onChange={e => {
              const next = e.target.checked;
              setOnlyIssues(next);
              setEditingId(null);
              fetchRows(target, keyword, 0, next);
            }}
          />
          손봐야 할 것만
        </FilterLabel>
        {keyword && (
          <SmallButton
            type="button"
            $color="#66696D"
            onClick={() => {
              setKeyword('');
              setEditingId(null);
              fetchRows(target, '', 0);
            }}
          >
            초기화
          </SmallButton>
        )}
        <Count>전체 {totalElements.toLocaleString()}건</Count>
      </SearchRow>

      <TableScrollBox>
        {target === 'addr' ? (
          <Table>
            <thead>
              <tr>
                <Th style={{ minWidth: '200px' }}>장소명</Th>
                <Th style={{ minWidth: '240px' }}>상세주소</Th>
                <Th style={{ width: '80px' }}>건수</Th>
                <Th style={{ width: '120px' }}>좌표</Th>
                <Th style={{ width: '150px' }}> </Th>
              </tr>
            </thead>
            <tbody>
              {rows.map(row =>
                editingId === row.addrId ? (
                  <tr key={row.addrId}>
                    <Td className="left">
                      <EditInput
                        autoFocus
                        value={form.addrName}
                        onChange={e => setForm(f => ({ ...f, addrName: e.target.value }))}
                      />
                    </Td>
                    <Td className="left">
                      <EditInput
                        value={form.detailAddr}
                        onChange={e => setForm(f => ({ ...f, detailAddr: e.target.value }))}
                      />
                    </Td>
                    <Td>{row.useCount?.toLocaleString()}</Td>
                    <Td>
                      {form.detailAddr.trim() !== (row.detailAddr ?? '').trim() ? (
                        <Warn>재조회됨</Warn>
                      ) : (
                        '유지'
                      )}
                    </Td>
                    <Td>
                      <ButtonRow>
                        <SmallButton
                          type="button"
                          $color="#1A7D55"
                          disabled={pendingId === row.addrId}
                          onClick={() => saveAddr(row)}
                        >
                          저장
                        </SmallButton>
                        <SmallButton type="button" $color="#66696D" onClick={cancelEdit}>
                          취소
                        </SmallButton>
                      </ButtonRow>
                    </Td>
                  </tr>
                ) : (
                  <tr key={row.addrId}>
                    <Td className="left">
                      {row.addrName}
                      {row.suspiciousName && <Warn>확인필요</Warn>}
                    </Td>
                    <Td className="left">
                      {row.detailAddr}
                      {row.fallbackAddr && <Warn>기본값</Warn>}
                    </Td>
                    <Td>{row.useCount?.toLocaleString()}</Td>
                    <Td>{row.noCoordinate ? <Warn>없음</Warn> : '있음'}</Td>
                    <Td>
                      <SmallButton
                        type="button"
                        $color="#093A6E"
                        onClick={() => startEditAddr(row)}
                      >
                        수정
                      </SmallButton>
                    </Td>
                  </tr>
                )
              )}
              {loaded && rows.length === 0 && (
                <tr>
                  <Td colSpan={5} className="empty">
                    {keyword.trim() ? '검색 결과가 없습니다.' : '손봐야 할 장소가 없습니다.'}
                  </Td>
                </tr>
              )}
            </tbody>
          </Table>
        ) : (
          <Table>
            <thead>
              <tr>
                <Th style={{ minWidth: '280px' }}>이름</Th>
                <Th style={{ width: '80px' }}>건수</Th>
                <Th style={{ width: '150px' }}> </Th>
              </tr>
            </thead>
            <tbody>
              {rows.map(row =>
                editingId === row.memberName ? (
                  <tr key={row.memberName}>
                    <Td className="left">
                      <EditInput
                        autoFocus
                        value={form.newName}
                        onChange={e => setForm(f => ({ ...f, newName: e.target.value }))}
                        onKeyDown={e => {
                          if (e.key === 'Enter') saveName(row);
                          if (e.key === 'Escape') cancelEdit();
                        }}
                      />
                    </Td>
                    <Td>{row.useCount?.toLocaleString()}</Td>
                    <Td>
                      <ButtonRow>
                        <SmallButton
                          type="button"
                          $color="#1A7D55"
                          disabled={pendingId === row.memberName}
                          onClick={() => saveName(row)}
                        >
                          저장
                        </SmallButton>
                        <SmallButton type="button" $color="#66696D" onClick={cancelEdit}>
                          취소
                        </SmallButton>
                      </ButtonRow>
                    </Td>
                  </tr>
                ) : (
                  <tr key={row.memberName}>
                    <Td className="left">
                      {row.memberName}
                      {row.issue && <Warn>확인필요</Warn>}
                    </Td>
                    <Td>{row.useCount?.toLocaleString()}</Td>
                    <Td>
                      <SmallButton
                        type="button"
                        $color="#093A6E"
                        onClick={() => startEditName(row)}
                      >
                        수정
                      </SmallButton>
                    </Td>
                  </tr>
                )
              )}
              {loaded && rows.length === 0 && (
                <tr>
                  <Td colSpan={3} className="empty">
                    {keyword.trim() ? '검색 결과가 없습니다.' : '손봐야 할 이름이 없습니다.'}
                  </Td>
                </tr>
              )}
            </tbody>
          </Table>
        )}
      </TableScrollBox>

      {totalPages > 1 && (
        <PaginationBox>
          <Pagination
            current={page}
            totalPages={totalPages}
            onChange={next => {
              setEditingId(null);
              fetchRows(target, keyword, next);
            }}
          />
        </PaginationBox>
      )}
    </>
  );
}

const TargetTabs = styled.div`
  display: flex;
  gap: 6px;
`;

const TargetTab = styled.button`
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid ${({ $on, theme }) => ($on ? theme.colors.primary : theme.colors.border)};
  border-radius: 4px;
  background: ${({ $on, theme }) => ($on ? theme.colors.primary : '#fff')};
  color: ${({ $on, theme }) => ($on ? '#fff' : theme.colors.liteGray)};
  font-size: ${({ theme }) => theme.sizes.medium};
  font-weight: bold;
  cursor: pointer;

  svg {
    width: 13px;
    height: 13px;
  }
`;

const Guide = styled.p`
  color: ${({ theme }) => theme.colors.liteGray};
  font-size: ${({ theme }) => theme.sizes.medium};
  line-height: 1.6;

  strong {
    color: ${({ theme }) => theme.colors.primary};
  }
`;

const SearchRow = styled.form`
  display: flex;
  align-items: center;
  gap: 6px;
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

const FilterLabel = styled.label`
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: ${({ theme }) => theme.colors.liteGray};
  font-size: ${({ theme }) => theme.sizes.medium};
  white-space: nowrap;
  cursor: pointer;

  input {
    cursor: pointer;
  }
`;

const Count = styled.span`
  margin-left: auto;
  color: ${({ theme }) => theme.colors.liteGray};
  font-size: ${({ theme }) => theme.sizes.medium};
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
  padding: 10px 8px;
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

const EditInput = styled.input`
  width: 100%;
  padding: 6px 8px;
  border: 1px solid ${({ theme }) => theme.colors.primary};
  border-radius: 4px;
  font-size: ${({ theme }) => theme.sizes.medium};

  &:focus {
    outline: none;
  }
`;

const ButtonRow = styled.div`
  display: flex;
  gap: 4px;
  justify-content: center;
`;

const SmallButton = styled.button`
  flex-shrink: 0;
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  background: ${({ $color }) => $color};
  color: #fff;
  font-size: ${({ theme }) => theme.sizes.medium};
  font-weight: bold;
  white-space: nowrap;
  cursor: pointer;

  &:disabled {
    opacity: 0.5;
    cursor: wait;
  }
`;

const PaginationBox = styled.div`
  margin-top: 12px;
`;

const Warn = styled.span`
  display: inline-block;
  margin-left: 6px;
  padding: 2px 8px;
  border-radius: 10px;
  background: #ffe9e8;
  color: #d33a32;
  font-size: ${({ theme }) => theme.sizes.small};
  font-weight: bold;
  white-space: nowrap;
`;
