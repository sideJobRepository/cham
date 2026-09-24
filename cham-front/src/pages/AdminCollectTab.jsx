import { Fragment, useEffect, useMemo, useRef, useState } from 'react';
import styled from 'styled-components';
import { toast } from 'react-toastify';
import { FaSyncAlt, FaFileExcel, FaExternalLinkAlt } from 'react-icons/fa';
import { AiOutlineDownload } from 'react-icons/ai';
import { useSetRecoilState } from 'recoil';
import { loadingState } from '@/recoil/appState.js';
import { showConfirmModal } from '@/components/ConfirmAlert.jsx';
import api from '@/utils/axiosInstance.js';
import Pagination from '@/components/Pagination.jsx';

const JOB_PAGE_SIZE = 10;
const MONTHS = Array.from({ length: 12 }, (_, i) => i + 1);

const FILE_STATUS = {
  COLLECTED: { label: '검수대기', color: '#093A6E' },
  IMPORTED: { label: '반영됨', color: '#1A7D55' },
  IGNORED: { label: '무시', color: '#66696D' },
  FAILED: { label: '실패', color: '#FF5E57' },
  DUPLICATE: { label: '중복', color: '#A0A4A8' },
};

const JOB_STATUS = {
  REQUESTED: { label: '대기', color: '#E5A000' },
  RUNNING: { label: '실행중', color: '#093A6E' },
  DONE: { label: '완료', color: '#1A7D55' },
  FAILED: { label: '실패', color: '#FF5E57' },
};

const errorMessage = (e, fallback) => e.response?.data?.message ?? fallback;

const formatDateTime = v => (v ? String(v).replace('T', ' ').slice(0, 16) : '-');

const formatSize = bytes => {
  if (bytes == null) return '-';
  if (bytes < 1024) return `${bytes}B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)}KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)}MB`;
};

/**
 * 수집관리 탭.
 *
 * 수집기(cham-collector)가 매일 기관 게시판을 돌며 받아 둔 원본 엑셀을 확인하고 지도 자료로 반영한다.
 * 반영하면 비공개로 들어가고, 공개는 공개관리 탭에서 켠다. 구청 자료는 장소가 없어
 * 자리값이 들어가니 자료정리 탭에서 고친다.
 *
 * 월 표 칸을 누르면 그 달 파일이 열리고, 자료가 없는 달은 그 자리에서 수집을 요청할 수 있다.
 */
export default function AdminCollectTab({ onImported }) {
  const setLoading = useSetRecoilState(loadingState);
  const nowYear = new Date().getFullYear();
  const nowMonth = new Date().getMonth() + 1;

  const [year, setYear] = useState(nowYear);
  const [matrix, setMatrix] = useState(null);

  // 수집 요청 폼. 기관·달 모두 비우면 전체 기관, 달 지정 없음
  const [reqSourceId, setReqSourceId] = useState('');
  const [reqMonth, setReqMonth] = useState('');

  // 월 표에서 고른 칸
  const [selected, setSelected] = useState(null); // { sourceId, name, enabled, month }
  const [files, setFiles] = useState([]);
  const [filesLoaded, setFilesLoaded] = useState(false);

  const [preview, setPreview] = useState(null);
  const [previewFile, setPreviewFile] = useState(null);
  const [deleteKey, setDeleteKey] = useState('');
  const [defaultName, setDefaultName] = useState('');
  const [pending, setPending] = useState(false);

  const [jobs, setJobs] = useState([]);
  const [jobPage, setJobPage] = useState(0);
  const [jobTotalPages, setJobTotalPages] = useState(0);
  const [openedJobId, setOpenedJobId] = useState(null);

  const previewRef = useRef(null);

  const startYear = matrix?.startYear ?? 2026;
  const startMonth = matrix?.startMonth ?? 8;
  const yearOptions = useMemo(() => {
    const list = [];
    for (let y = nowYear; y >= startYear; y--) list.push(y);
    return list;
  }, [nowYear, startYear]);

  const isCollectable = (y, m) => {
    const ym = y * 12 + m;
    return ym >= startYear * 12 + startMonth && ym <= nowYear * 12 + nowMonth;
  };

  const latestJob = jobs[0] && jobPage === 0 ? jobs[0] : null;
  const jobActive = latestJob && ['REQUESTED', 'RUNNING'].includes(latestJob.status);

  // ── 조회 ──

  const fetchMatrix = async (nextYear = year) => {
    try {
      const { data } = await api.get('/cham/admin/collect/months', { params: { year: nextYear } });
      setMatrix(data);
    } catch (e) {
      console.error(e);
      toast.error(errorMessage(e, '수집 현황을 불러오지 못했습니다.'));
    }
  };

  const fetchJobs = async (nextPage = jobPage) => {
    try {
      const { data } = await api.get('/cham/admin/collect/jobs', {
        params: { page: nextPage, size: JOB_PAGE_SIZE },
      });
      setJobs(data?.content ?? []);
      setJobTotalPages(data?.totalPages ?? 0);
      setJobPage(data?.page ?? nextPage);
    } catch (e) {
      console.error(e);
    }
  };

  const fetchFiles = async cell => {
    setFilesLoaded(false);
    try {
      const { data } = await api.get('/cham/admin/collect/files', {
        params: { sourceId: cell.sourceId, year, month: cell.month, size: 50 },
      });
      const list = data?.content ?? [];
      setFiles(list);
      // 검수할 파일이 하나뿐이면 바로 미리보기를 연다
      const reviewable = list.filter(f => f.status !== 'DUPLICATE');
      if (reviewable.length === 1) openPreview(reviewable[0]);
    } catch (e) {
      console.error(e);
      toast.error(errorMessage(e, '파일 목록을 불러오지 못했습니다.'));
      setFiles([]);
    } finally {
      setFilesLoaded(true);
    }
  };

  useEffect(() => {
    fetchMatrix(year);
    fetchJobs(0);
  }, []);

  // 대기·실행 중인 수집이 있으면 10초마다 다시 본다. 끝나면 월 표도 새로 읽는다
  useEffect(() => {
    if (!jobActive) return undefined;
    const timer = setInterval(async () => {
      await fetchJobs(0);
      await fetchMatrix(year);
    }, 10000);
    return () => clearInterval(timer);
  }, [jobActive, year]);

  const changeYear = next => {
    setYear(next);
    setSelected(null);
    setFiles([]);
    closePreview();
    setReqMonth('');
    fetchMatrix(next);
  };

  // ── 월 표 ──

  const selectCell = (row, month) => {
    if (!isCollectable(year, month) && !row.months[month - 1]?.fileId) return;
    const cell = { sourceId: row.sourceId, name: row.name, enabled: row.enabled, month };
    setSelected(cell);
    setFiles([]);
    closePreview();
    fetchFiles(cell);
  };

  // ── 수집 요청 ──

  const requestJob = ({ sourceId, sourceName, month }) => {
    if (jobActive) {
      toast.error('이미 대기 중이거나 실행 중인 수집이 있습니다.');
      return;
    }
    const target = `${sourceName ?? '전체 기관'}${month ? ` ${year}년 ${month}월분` : ''}`;
    showConfirmModal({
      title: <>수집 요청</>,
      message: (
        <>
          {target}을(를) 수집합니다.
          <br />
          {month
            ? '그 달 게시물만 찾아 받습니다. 이미 받은 파일은 다시 받지 않습니다.'
            : '새로 올라온 게시물을 찾아 받습니다. 이미 받은 파일은 다시 받지 않습니다.'}
          <br />
          수집기가 1분 안에 시작합니다.
        </>
      ),
      gb: true,
      firstText: '취소',
      secondText: '요청',
      onConfirm: async () => {
        try {
          const body = { sourceId: sourceId || null };
          if (month) {
            body.year = year;
            body.month = Number(month);
          }
          const { data } = await api.post('/cham/admin/collect/jobs', body);
          toast.success(data?.message ?? '수집을 요청했습니다.');
          await fetchJobs(0);
        } catch (e) {
          toast.error(errorMessage(e, '수집 요청에 실패했습니다.'));
        }
      },
    });
  };

  const requestFromForm = () => {
    const source = matrix?.rows?.find(r => String(r.sourceId) === String(reqSourceId));
    requestJob({ sourceId: source?.sourceId, sourceName: source?.name, month: reqMonth || null });
  };

  // ── 미리보기 / 반영 ──

  const closePreview = () => {
    setPreview(null);
    setPreviewFile(null);
    setDeleteKey('');
    setDefaultName('');
  };

  const openPreview = async (file, nameOverride) => {
    setLoading(true);
    try {
      const params = { limit: 50 };
      if (nameOverride?.trim()) params.defaultName = nameOverride.trim();
      const { data } = await api.get(`/cham/admin/collect/files/${file.fileId}/preview`, { params });
      setPreview(data);
      setPreviewFile(file);
      if (nameOverride === undefined) {
        setDeleteKey(data?.suggestedDeleteKey ?? '');
        setDefaultName('');
      }
      setTimeout(() => previewRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 50);
    } catch (e) {
      toast.error(errorMessage(e, '미리보기를 만들지 못했습니다.'));
    } finally {
      setLoading(false);
    }
  };

  const reloadAfterChange = async () => {
    await fetchMatrix(year);
    if (selected) await fetchFiles(selected);
  };

  const importFile = () => {
    if (!preview || !previewFile) return;
    showConfirmModal({
      title: <>지도 자료로 반영</>,
      message: (
        <>
          {preview.sourceName} {preview.totalRows}건을 <strong>비공개</strong>로 반영합니다.
          <br />
          삭제키: {deleteKey.trim() || preview.suggestedDeleteKey}
          <br />
          {preview.placeMissing && (
            <>
              장소가 없는 자료라 자리값(경조사비)이 들어갑니다. 반영 후 자료정리에서 고쳐 주세요.
              <br />
            </>
          )}
          공개관리 탭에서 확인한 뒤 공개로 돌려 주세요.
        </>
      ),
      gb: true,
      firstText: '취소',
      secondText: '반영',
      onConfirm: async () => {
        setPending(true);
        setLoading(true);
        try {
          const { data } = await api.post(`/cham/admin/collect/files/${previewFile.fileId}/import`, {
            deleteKey: deleteKey.trim() || null,
            defaultName: defaultName.trim() || null,
          });
          toast.success(data?.message ?? '반영했습니다.');
          closePreview();
          onImported?.();
          await reloadAfterChange();
        } catch (e) {
          toast.error(errorMessage(e, '반영에 실패했습니다.'));
        } finally {
          setPending(false);
          setLoading(false);
        }
      },
    });
  };

  const changeStatus = (file, action) => {
    const label = action === 'ignore' ? '무시' : '검수 대기로 되돌리기';
    showConfirmModal({
      title: <>{label}</>,
      message: (
        <>
          '{file.originName}' 을(를) {label} 합니다.
          {action === 'reset' && file.status === 'IMPORTED' && (
            <>
              <br />
              반영한 자료('{file.deleteKey}')를 먼저 삭제키로 지워야 되돌릴 수 있습니다.
            </>
          )}
        </>
      ),
      gb: action !== 'ignore',
      firstText: '취소',
      secondText: '확인',
      onConfirm: async () => {
        setPending(true);
        try {
          const { data } = await api.put(`/cham/admin/collect/files/${file.fileId}/${action}`);
          toast.success(data?.message ?? '처리했습니다.');
          if (previewFile?.fileId === file.fileId) closePreview();
          await reloadAfterChange();
        } catch (e) {
          toast.error(errorMessage(e, '처리에 실패했습니다.'));
        } finally {
          setPending(false);
        }
      },
    });
  };

  const downloadFile = async file => {
    try {
      const res = await api.get(`/cham/admin/collect/files/${file.fileId}/download`, {
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const a = document.createElement('a');
      a.href = url;
      a.download = file.originName ?? `collect-${file.fileId}`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (e) {
      console.error(e);
      toast.error('원본을 내려받지 못했습니다.');
    }
  };

  // ── 화면 ──

  const enabledSources = (matrix?.rows ?? []).filter(r => r.enabled);
  const selectedCell = selected
    ? matrix?.rows?.find(r => r.sourceId === selected.sourceId)?.months?.[selected.month - 1]
    : null;

  return (
    <>
      <Guide>
        수집기가 매일 한 번 기관 게시판을 확인해 새 원본을 받아 둡니다({startYear}년 {startMonth}월분부터).
        칸을 눌러 원본을 확인하고 <strong>반영</strong>하면 비공개로 들어갑니다. 공개는 공개관리
        탭에서 켜고, 장소가 없는 구청 자료는 자료정리 탭에서 고칩니다.
      </Guide>

      <TopBar>
        <select value={year} onChange={e => changeYear(Number(e.target.value))}>
          {yearOptions.map(y => (
            <option key={y} value={y}>
              {y}년
            </option>
          ))}
        </select>

        <RequestBox>
          <select value={reqSourceId} onChange={e => setReqSourceId(e.target.value)}>
            <option value="">전체 기관</option>
            {enabledSources.map(r => (
              <option key={r.sourceId} value={r.sourceId}>
                {r.name}
              </option>
            ))}
          </select>
          <select value={reqMonth} onChange={e => setReqMonth(e.target.value)}>
            <option value="">새 게시물 (달 지정 안 함)</option>
            {MONTHS.filter(m => isCollectable(year, m)).map(m => (
              <option key={m} value={m}>
                {year}년 {m}월분
              </option>
            ))}
          </select>
          <SmallButton type="button" $color="#093A6E" disabled={!!jobActive} onClick={requestFromForm}>
            <FaSyncAlt /> 수집 요청
          </SmallButton>
        </RequestBox>

        {latestJob && (
          <LatestJob>
            최근 수집{' '}
            <Chip $color={JOB_STATUS[latestJob.status]?.color}>
              {JOB_STATUS[latestJob.status]?.label ?? latestJob.status}
            </Chip>{' '}
            {formatDateTime(latestJob.finishedAt ?? latestJob.startedAt ?? latestJob.registDate)}
          </LatestJob>
        )}
      </TopBar>

      <Legend>
        {Object.entries(FILE_STATUS)
          .filter(([k]) => k !== 'DUPLICATE')
          .map(([k, v]) => (
            <span key={k}>
              <Dot $color={v.color} /> {v.label}
            </span>
          ))}
        <span>
          <Dot $color="#E0E0E0" /> 없음
        </span>
      </Legend>

      <TableScrollBox>
        <Table>
          <thead>
            <tr>
              <Th>기관</Th>
              {MONTHS.map(m => (
                <Th key={m}>{m}월</Th>
              ))}
            </tr>
          </thead>
          <tbody>
            {(matrix?.rows ?? []).map(row => (
              <MatrixRow key={row.sourceId} $off={!row.enabled}>
                <Td className="left">
                  {row.name}
                  {!row.enabled && <Muted> (수집 안 함{row.fileFormat === 'PDF' ? ' · PDF' : ''})</Muted>}
                  {row.enabled && row.lastError && <Warn title={row.lastError}>오류</Warn>}
                </Td>
                {row.months.map(cell => {
                  const collectable = isCollectable(year, cell.month);
                  const status = FILE_STATUS[cell.status];
                  const isSelected = selected?.sourceId === row.sourceId && selected?.month === cell.month;
                  return (
                    <CellTd
                      key={cell.month}
                      $clickable={collectable || !!cell.fileId}
                      $selected={isSelected}
                      onClick={() => selectCell(row, cell.month)}
                    >
                      {cell.fileId ? (
                        <Chip $color={status?.color}>
                          {status?.label}
                          {cell.fileCount > 1 ? ` ${cell.fileCount}` : ''}
                        </Chip>
                      ) : (
                        <Empty $collectable={collectable}>{collectable ? '·' : ''}</Empty>
                      )}
                    </CellTd>
                  );
                })}
              </MatrixRow>
            ))}
            {matrix && matrix.rows.length === 0 && (
              <tr>
                <Td colSpan={13} className="empty">
                  등록된 수집 기관이 없습니다. 수집ddl.sql 의 시드를 넣어 주세요.
                </Td>
              </tr>
            )}
          </tbody>
        </Table>
      </TableScrollBox>

      {selected && (
        <Panel>
          <PanelTitle>
            {selected.name} · {year}년 {selected.month}월분
          </PanelTitle>

          {filesLoaded && files.length === 0 && (
            <EmptyBox>
              이 달 자료를 아직 받지 못했습니다.
              {selected.enabled && isCollectable(year, selected.month) ? (
                <SmallButton
                  type="button"
                  $color="#093A6E"
                  disabled={!!jobActive}
                  onClick={() =>
                    requestJob({ sourceId: selected.sourceId, sourceName: selected.name, month: selected.month })
                  }
                >
                  <FaSyncAlt /> 이 달 수집
                </SmallButton>
              ) : (
                <Muted>수집 대상이 아닌 기관입니다.</Muted>
              )}
            </EmptyBox>
          )}

          {files.length > 0 && (
            <TableScrollBox>
              <Table>
                <thead>
                  <tr>
                    <Th>원본 파일</Th>
                    <Th>게시물</Th>
                    <Th>크기</Th>
                    <Th>수집일</Th>
                    <Th>상태</Th>
                    <Th>삭제키</Th>
                    <Th>관리</Th>
                  </tr>
                </thead>
                <tbody>
                  {files.map(file => (
                    <tr key={file.fileId}>
                      <Td className="left">
                        <FaFileExcel color="#1A7D55" /> {file.originName}
                      </Td>
                      <Td className="left">
                        {file.detailUrl ? (
                          <a href={file.detailUrl} target="_blank" rel="noreferrer">
                            {file.postTitle ?? '게시물'} <FaExternalLinkAlt size={10} />
                          </a>
                        ) : (
                          file.postTitle
                        )}
                      </Td>
                      <Td>{formatSize(file.size)}</Td>
                      <Td>{formatDateTime(file.collectedAt)}</Td>
                      <Td>
                        <Chip $color={FILE_STATUS[file.status]?.color}>
                          {FILE_STATUS[file.status]?.label ?? file.status}
                        </Chip>
                        {file.error && <Warn title={file.error}>오류</Warn>}
                      </Td>
                      <Td>{file.deleteKey ?? '-'}</Td>
                      <Td>
                        <ButtonRow>
                          {file.status !== 'DUPLICATE' && (
                            <SmallButton type="button" $color="#093A6E" onClick={() => openPreview(file)}>
                              미리보기
                            </SmallButton>
                          )}
                          <IconButton type="button" title="원본 받기" onClick={() => downloadFile(file)}>
                            <AiOutlineDownload />
                          </IconButton>
                          {['COLLECTED', 'FAILED'].includes(file.status) && (
                            <SmallButton
                              type="button"
                              $color="#66696D"
                              disabled={pending}
                              onClick={() => changeStatus(file, 'ignore')}
                            >
                              무시
                            </SmallButton>
                          )}
                          {['IGNORED', 'FAILED', 'IMPORTED'].includes(file.status) && (
                            <SmallButton
                              type="button"
                              $color="#66696D"
                              disabled={pending}
                              onClick={() => changeStatus(file, 'reset')}
                            >
                              되돌리기
                            </SmallButton>
                          )}
                        </ButtonRow>
                      </Td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </TableScrollBox>
          )}

          {selectedCell?.fileId &&
            selected.enabled &&
            isCollectable(year, selected.month) &&
            files.length > 0 && (
              <MutedLine>
                파일이 바뀌었을 것 같으면{' '}
                <LinkButton
                  type="button"
                  disabled={!!jobActive}
                  onClick={() =>
                    requestJob({ sourceId: selected.sourceId, sourceName: selected.name, month: selected.month })
                  }
                >
                  이 달 다시 수집
                </LinkButton>
                을 요청할 수 있습니다. 같은 파일은 다시 받지 않습니다.
              </MutedLine>
            )}
        </Panel>
      )}

      {preview && previewFile && (
        <Panel ref={previewRef}>
          <PanelTitle>
            미리보기 · {previewFile.originName}
            <Chip $color={FILE_STATUS[previewFile.status]?.color}>
              {FILE_STATUS[previewFile.status]?.label}
            </Chip>
          </PanelTitle>

          <Stats>
            <span>전체 {preview.totalRows}건</span>
            <span>경고 {preview.warningRows}건</span>
            <span className={preview.blockingRows > 0 ? 'bad' : ''}>
              반영 불가 {preview.blockingRows}건
            </span>
            <span className={preview.noNameRows > 0 ? 'warn' : ''}>이름 없음 {preview.noNameRows}건</span>
          </Stats>

          {preview.fileWarnings?.map(w => (
            <Notice key={w} className="bad">
              {w}
            </Notice>
          ))}
          {preview.placeMissing && (
            <Notice>
              이 자료에는 장소·주소가 없습니다. 자리값(경조사비 / 도산로370번길 22-1)이 들어가니 반영 후
              자료정리 탭에서 고쳐 주세요.
            </Notice>
          )}
          {preview.blockingRows > 0 && (
            <Notice className="bad">
              날짜를 읽지 못한 줄이 있어 반영할 수 없습니다. 원본을 받아 고친 뒤 수동 업로드를 쓰거나,
              날짜 형식을 알려 주세요.
            </Notice>
          )}

          <SheetList>
            {preview.sheets.map(sheet => (
              <SheetItem key={sheet.name}>
                <strong>{sheet.name}</strong>
                {sheet.used ? (
                  <>
                    <Muted>
                      {' '}
                      · {sheet.headerRowNum}행 헤더 · {sheet.rowCount}건
                    </Muted>
                    <ChipRow>
                      {Object.entries(sheet.mapping).map(([raw, field]) => (
                        <MapChip key={raw}>
                          {raw} → {field}
                        </MapChip>
                      ))}
                      {sheet.unmapped.map(raw => (
                        <MapChip key={raw} $off title="사전에 없는 헤더라 읽지 않았습니다">
                          {raw}
                        </MapChip>
                      ))}
                    </ChipRow>
                  </>
                ) : (
                  <Muted> · 헤더를 찾지 못해 읽지 않았습니다</Muted>
                )}
              </SheetItem>
            ))}
          </SheetList>

          {previewFile.status === 'COLLECTED' && (
            <FormRow>
              <label>
                삭제키
                <EditInput
                  value={deleteKey}
                  onChange={e => setDeleteKey(e.target.value)}
                  placeholder={preview.suggestedDeleteKey}
                />
              </label>
              <label>
                빈 이름 채우기
                <EditInput
                  value={defaultName}
                  onChange={e => setDefaultName(e.target.value)}
                  placeholder="예: 공무원"
                />
              </label>
              <SmallButton
                type="button"
                $color="#66696D"
                onClick={() => openPreview(previewFile, defaultName)}
              >
                다시 보기
              </SmallButton>
            </FormRow>
          )}

          <TableScrollBox>
            <Table>
              <thead>
                <tr>
                  <Th>행</Th>
                  <Th>사용자</Th>
                  <Th>이름</Th>
                  <Th>일자</Th>
                  <Th>시간</Th>
                  <Th>장소</Th>
                  <Th>주소</Th>
                  <Th>목적</Th>
                  <Th>인원</Th>
                  <Th>금액</Th>
                  <Th>방법</Th>
                  <Th>비고</Th>
                </tr>
              </thead>
              <tbody>
                {preview.rows.map(r => (
                  <Fragment key={`${r.sheet}-${r.rowNum}`}>
                    <PreviewTr $bad={r.blocking} $warn={r.warnings.length > 0}>
                      <Td>{r.rowNum}</Td>
                      <Td>{r.user}</Td>
                      <Td>{r.name ?? '-'}</Td>
                      <Td>{r.date ?? '-'}</Td>
                      <Td>{r.time ? String(r.time).slice(0, 5) : '-'}</Td>
                      <Td className="left">{r.addrName}</Td>
                      <Td className="left">{r.addrDetail}</Td>
                      <Td className="left">{r.purpose}</Td>
                      <Td>{r.personnel}</Td>
                      <Td>{r.amount != null ? Number(r.amount).toLocaleString() : '-'}</Td>
                      <Td>{r.method}</Td>
                      <Td>{r.remark}</Td>
                    </PreviewTr>
                    {r.warnings.length > 0 && (
                      <tr>
                        <WarnTd colSpan={12}>{r.warnings.join(' · ')}</WarnTd>
                      </tr>
                    )}
                  </Fragment>
                ))}
              </tbody>
            </Table>
          </TableScrollBox>
          {preview.totalRows > preview.rows.length && (
            <MutedLine>
              앞 {preview.rows.length}건만 보여줍니다. 반영은 {preview.totalRows}건 전부 들어갑니다.
            </MutedLine>
          )}

          <ButtonRow className="end">
            <SmallButton type="button" $color="#66696D" onClick={closePreview}>
              닫기
            </SmallButton>
            <SmallButton type="button" $color="#093A6E" onClick={() => downloadFile(previewFile)}>
              원본 받기
            </SmallButton>
            {previewFile.status === 'COLLECTED' && (
              <SmallButton
                type="button"
                $color="#1A7D55"
                disabled={pending || preview.blockingRows > 0 || preview.totalRows === 0}
                onClick={importFile}
              >
                비공개로 반영
              </SmallButton>
            )}
          </ButtonRow>
        </Panel>
      )}

      <Panel>
        <PanelTitle>
          수집 이력
          <SmallButton type="button" $color="#66696D" onClick={() => fetchJobs(jobPage)}>
            새로고침
          </SmallButton>
        </PanelTitle>
        <TableScrollBox>
          <Table>
            <thead>
              <tr>
                <Th>요청</Th>
                <Th>구분</Th>
                <Th>요청자</Th>
                <Th>대상</Th>
                <Th>상태</Th>
                <Th>시작 / 종료</Th>
                <Th>게시물</Th>
                <Th>새 파일</Th>
                <Th>건너뜀</Th>
                <Th>실패</Th>
              </tr>
            </thead>
            <tbody>
              {jobs.map(job => (
                <Fragment key={job.jobId}>
                  <tr
                    onClick={() => setOpenedJobId(openedJobId === job.jobId ? null : job.jobId)}
                    style={{ cursor: job.log ? 'pointer' : 'default' }}
                  >
                    <Td>{formatDateTime(job.registDate)}</Td>
                    <Td>{job.trigger === 'MANUAL' ? '수동' : '정기'}</Td>
                    <Td>{job.requestedByName ?? (job.trigger === 'MANUAL' ? '-' : '수집기')}</Td>
                    <Td>
                      {job.sourceName ?? '전체'}
                      {job.targetYear ? ` · ${job.targetYear}.${String(job.targetMonth).padStart(2, '0')}` : ''}
                    </Td>
                    <Td>
                      <Chip $color={JOB_STATUS[job.status]?.color}>
                        {JOB_STATUS[job.status]?.label ?? job.status}
                      </Chip>
                    </Td>
                    <Td>
                      {formatDateTime(job.startedAt)}
                      <br />
                      {formatDateTime(job.finishedAt)}
                    </Td>
                    <Td>{job.postsSeen}</Td>
                    <Td>{job.filesNew}</Td>
                    <Td>{job.filesSkipped}</Td>
                    <Td className={job.filesFailed > 0 ? 'bad' : ''}>{job.filesFailed}</Td>
                  </tr>
                  {openedJobId === job.jobId && job.log && (
                    <tr>
                      <LogTd colSpan={10}>
                        <pre>{job.log}</pre>
                      </LogTd>
                    </tr>
                  )}
                </Fragment>
              ))}
              {jobs.length === 0 && (
                <tr>
                  <Td colSpan={10} className="empty">
                    아직 수집 이력이 없습니다.
                  </Td>
                </tr>
              )}
            </tbody>
          </Table>
        </TableScrollBox>
        {jobTotalPages > 1 && (
          <PaginationBox>
            <Pagination current={jobPage} totalPages={jobTotalPages} onChange={next => fetchJobs(next)} />
          </PaginationBox>
        )}
      </Panel>
    </>
  );
}

const Guide = styled.p`
  color: ${({ theme }) => theme.colors.liteGray};
  font-size: ${({ theme }) => theme.sizes.medium};
  line-height: 1.6;

  strong {
    color: ${({ theme }) => theme.colors.primary};
  }
`;

const TopBar = styled.div`
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;

  select {
    padding: 7px 8px;
    border: 1px solid ${({ theme }) => theme.colors.border};
    border-radius: 4px;
    font-size: ${({ theme }) => theme.sizes.medium};
    background: #fff;
  }
`;

const RequestBox = styled.div`
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
`;

const LatestJob = styled.span`
  margin-left: auto;
  color: ${({ theme }) => theme.colors.liteGray};
  font-size: ${({ theme }) => theme.sizes.medium};
`;

const Legend = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: ${({ theme }) => theme.colors.liteGray};
  font-size: ${({ theme }) => theme.sizes.small};
`;

const Dot = styled.i`
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: ${({ $color }) => $color};
  vertical-align: -1px;
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

  &.bad {
    color: #d33a32;
    font-weight: bold;
  }

  a {
    color: ${({ theme }) => theme.colors.primary};
  }
`;

const MatrixRow = styled.tr`
  opacity: ${({ $off }) => ($off ? 0.45 : 1)};

  td:first-child {
    white-space: nowrap;
  }
`;

const CellTd = styled(Td)`
  padding: 6px 2px;
  cursor: ${({ $clickable }) => ($clickable ? 'pointer' : 'default')};
  background: ${({ $selected }) => ($selected ? '#e8f0fb' : 'transparent')};

  &:hover {
    background: ${({ $clickable, $selected }) => ($selected ? '#e8f0fb' : $clickable ? '#f5f7fa' : 'transparent')};
  }
`;

const Empty = styled.span`
  display: inline-block;
  width: 100%;
  color: ${({ $collectable }) => ($collectable ? '#bdbdbd' : 'transparent')};
  font-weight: bold;
`;

const Chip = styled.span`
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  background: ${({ $color }) => $color ?? '#E0E0E0'};
  color: #fff;
  font-size: ${({ theme }) => theme.sizes.small};
  font-weight: bold;
  white-space: nowrap;
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

const Muted = styled.span`
  color: ${({ theme }) => theme.colors.liteGray};
  font-size: ${({ theme }) => theme.sizes.small};
`;

const MutedLine = styled.p`
  color: ${({ theme }) => theme.colors.liteGray};
  font-size: ${({ theme }) => theme.sizes.small};
`;

const Panel = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: 6px;
  scroll-margin-top: 80px;
`;

const PanelTitle = styled.h3`
  display: flex;
  align-items: center;
  gap: 10px;
  color: ${({ theme }) => theme.colors.primary};
  font-size: ${({ theme }) => theme.sizes.medium};
  font-weight: bold;

  button {
    margin-left: auto;
  }
`;

const EmptyBox = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 8px;
  color: ${({ theme }) => theme.colors.liteGray};
  font-size: ${({ theme }) => theme.sizes.medium};
`;

const Stats = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: ${({ theme }) => theme.sizes.medium};
  font-weight: bold;

  .bad {
    color: #d33a32;
  }

  .warn {
    color: #c77700;
  }
`;

const Notice = styled.p`
  padding: 10px 12px;
  border-radius: 4px;
  background: #fff7e6;
  color: #8a5a00;
  font-size: ${({ theme }) => theme.sizes.medium};
  line-height: 1.5;

  &.bad {
    background: #ffe9e8;
    color: #d33a32;
  }
`;

const SheetList = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: ${({ theme }) => theme.sizes.medium};
`;

const SheetItem = styled.div``;

const ChipRow = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 4px;
`;

const MapChip = styled.span`
  padding: 2px 8px;
  border: 1px solid ${({ $off, theme }) => ($off ? theme.colors.border : theme.colors.primary)};
  border-radius: 10px;
  color: ${({ $off, theme }) => ($off ? theme.colors.liteGray : theme.colors.primary)};
  font-size: ${({ theme }) => theme.sizes.small};
  text-decoration: ${({ $off }) => ($off ? 'line-through' : 'none')};
`;

const FormRow = styled.div`
  display: flex;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 12px;

  label {
    display: flex;
    flex-direction: column;
    gap: 4px;
    color: ${({ theme }) => theme.colors.liteGray};
    font-size: ${({ theme }) => theme.sizes.small};
  }
`;

const EditInput = styled.input`
  width: 240px;
  max-width: 100%;
  padding: 6px 8px;
  border: 1px solid ${({ theme }) => theme.colors.primary};
  border-radius: 4px;
  font-size: ${({ theme }) => theme.sizes.medium};

  &:focus {
    outline: none;
  }
`;

const PreviewTr = styled.tr`
  background: ${({ $bad, $warn }) => ($bad ? '#ffe9e8' : $warn ? '#fffaf0' : 'transparent')};
`;

const WarnTd = styled.td`
  padding: 2px 8px 8px;
  border-bottom: 1px solid ${({ theme }) => theme.colors.border};
  color: #c77700;
  font-size: ${({ theme }) => theme.sizes.small};
  text-align: left;
`;

const LogTd = styled.td`
  padding: 8px;
  border-bottom: 1px solid ${({ theme }) => theme.colors.border};
  background: #f9fafb;

  pre {
    margin: 0;
    max-height: 240px;
    overflow: auto;
    font-size: ${({ theme }) => theme.sizes.small};
    white-space: pre-wrap;
    word-break: break-all;
  }
`;

const ButtonRow = styled.div`
  display: flex;
  gap: 4px;
  justify-content: center;
  flex-wrap: wrap;

  &.end {
    justify-content: flex-end;
  }
`;

const SmallButton = styled.button`
  display: inline-flex;
  align-items: center;
  gap: 5px;
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
    cursor: not-allowed;
  }

  svg {
    width: 12px;
    height: 12px;
  }
`;

const IconButton = styled.button`
  display: inline-flex;
  align-items: center;
  padding: 4px 6px;
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: 4px;
  background: #fff;
  cursor: pointer;

  svg {
    width: 16px;
    height: 16px;
  }
`;

const LinkButton = styled.button`
  padding: 0;
  border: none;
  background: none;
  color: ${({ theme }) => theme.colors.primary};
  font-size: inherit;
  font-weight: bold;
  text-decoration: underline;
  cursor: pointer;

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
`;

const PaginationBox = styled.div`
  margin-top: 12px;
`;
