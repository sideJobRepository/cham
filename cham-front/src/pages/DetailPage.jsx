import styled from 'styled-components';
import VisitCard from '../components/VisiitCard.jsx';
import { useEffect, useRef, useState } from 'react';
import { FaCommentDots } from 'react-icons/fa';
import { GiFootprint } from 'react-icons/gi';
import { FaEye, FaPen, FaCheckCircle } from 'react-icons/fa';

import { useFetchCard, useFetchDetailData, useMapSearch } from '@/recoil/fetchAppState.js';
import { useDetailMapState } from '@/recoil/useAppState.js';
import { useRecoilValue } from 'recoil';
import { checkDataState, userState } from '@/recoil/appState.js';
import api from '@/utils/axiosInstance.js';
import { toast } from 'react-toastify';
import 'react-confirm-alert/src/react-confirm-alert.css';
import basicLogo from '/basicLogo.png';

import Lightbox from 'yet-another-react-lightbox';
import 'yet-another-react-lightbox/styles.css';
import { MdDelete } from 'react-icons/md';
import { showConfirmModal } from '@/components/ConfirmAlert.jsx';
import Modal from '@/components/modal/Modal.jsx';
import LoginMoadl from '@/components/modal/LoginModal.jsx';

export default function DetailPage({ initialParams }) {
  const { mapDetailData } = useDetailMapState();

  //로그인 모달
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
  //가봤어요, 의심돼요
  const cardFetch = useFetchCard();
  const cardData = useRecoilValue(checkDataState);

  const [detail, setDetail] = useState(null);

  const [editingReplyId, setEditingReplyId] = useState(null);
  const [editingText, setEditingText] = useState('');

  const [imagePreviews, setImagePreviews] = useState([]);
  const [imageFiles, setImageFiles] = useState([]);

  const [editImage, setEditImage] = useState({});

  // 수정시 이미지 복사
  const [editingImageUrls, setEditingImageUrls] = useState({});

  const user = useRecoilValue(userState);

  const detailSearch = useFetchDetailData();

  const [showInput, setShowInput] = useState(false);
  const inputRef = useRef(null);

  const [lightboxIndex, setLightboxIndex] = useState(-1);
  const [lightboxImages, setLightboxImages] = useState([]);

  const handleCreate = async () => {
    const replyCont = inputRef.current.value;
    const memberId = user?.id;
    const cardUseAddrId = detail.cardUseAddrId;

    const formData = new FormData();
    formData.append('cardUseAddrId', cardUseAddrId);
    formData.append('memberId', memberId);
    formData.append('replyCont', replyCont);

    imageFiles.forEach(file => {
      formData.append('fileList', file); // key 이름은 반드시 fileList!
    });

    if (!replyCont) {
      toast.error('내용을 입력해주세요.');
      return;
    }

    const toastId = toast.loading('댓글을 저장 중 입니다.');

    try {
      await api.post('/cham/reply', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setShowInput(false);
      toast.update(toastId, {
        render: '댓글 저장이 완료됐습니다.',
        type: 'success',
        isLoading: false,
        autoClose: 1000,
      });
      await detailSearch(initialParams.addrDetail);

      // 입력 상태 초기화
      setEditingText('');
      setImageFiles([]);
      setImagePreviews([]);
    } catch (e) {
      console.log('error', e);
      toast.update(toastId, {
        render: '댓글 저장이 실패했습니다.',
        type: 'error',
        isLoading: false,
        autoClose: 3000,
      });
    }
  };

  const handleUpdate = async reply => {
    const replyId = reply.replyId;

    const currentUrls = editingImageUrls[replyId] || [];
    const originalUrls = reply.replyImageUrls || [];
    const deletedUrls = originalUrls.filter(url => !currentUrls.includes(url));
    const newFiles = editImage[replyId]?.files || [];

    const formData = new FormData();
    formData.append('replyId', replyId);
    formData.append('replyCont', editingText);

    let index = 0;

    // 남아있는 기존 이미지 (normal)
    currentUrls.forEach(url => {
      formData.append(`images[${index}].state`, 'normal');
      formData.append(`images[${index}].imgUrl`, url);
      index++;
    });

    // 삭제된 기존 이미지 (delete)
    deletedUrls.forEach(url => {
      formData.append(`images[${index}].state`, 'delete');
      formData.append(`images[${index}].imgUrl`, url);
      index++;
    });

    // 새로 추가된 이미지 (create)
    newFiles.forEach(file => {
      formData.append(`images[${index}].state`, 'create');
      formData.append(`images[${index}].file`, file);
      index++;
    });

    const toastId = toast.loading('댓글을 수정 중 입니다.');

    try {
      await api.put('/cham/reply', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });

      toast.update(toastId, {
        render: '댓글 수정이 완료됐습니다.',
        type: 'success',
        isLoading: false,
        autoClose: 1000,
      });

      // 상태 초기화
      setEditingReplyId(null);
      setEditingText('');
      setEditImage(prev => {
        const newMap = { ...prev };
        delete newMap[replyId];
        return newMap;
      });
      setEditingImageUrls(prev => {
        const newMap = { ...prev };
        delete newMap[replyId];
        return newMap;
      });

      await detailSearch(initialParams.addrDetail);
    } catch (e) {
      toast.update(toastId, {
        render: '댓글 수정이 실패했습니다.',
        type: 'error',
        isLoading: false,
        autoClose: 3000,
      });
    }
  };

  //가봤어요, 의심돼요
  const handleSubCreate = async gb => {
    const userId = user?.id;

    if (!userId) {
      showConfirmModal({
        title: <>로그인 후 이용하실 수 있습니다.</>,
        message: (
          <>
            로그인하시면 [댓글, 가봤어요, 궁금해요 등 관심가게 등록] 등 더 많은 기능을 이용할 수
            있습니다.
          </>
        ),
        gb: true,
        firstText: '둘러보기 계속',
        secondText: '로그인하고 모든 기능 사용하기',
        onConfirm: () => {
          setIsLoginModalOpen(true);
        },
      });

      return;
    }

    const url = gb ? 'check-visited' : 'check-suspicious';

    const visitGb = cardData?.myVisited === 'Y' ? null : 'Y';
    const suspiciousGb = cardData?.mySpicioused === 'Y' ? null : 'Y';

    try {
      await api.post(
        `/cham/${url}`,
        {
          memberId: userId,
          addrId: cardData?.chamMonimapCardUseAddrId,
          visited: visitGb,
          suspicioused: suspiciousGb,
        },
        {}
      );

      await cardFetch(cardData?.chamMonimapCardUseAddrId);
    } catch (e) {
      console.log('error', e);
    }
  };

  // 초기/파라미터 변경 시 조회
  useEffect(() => {
    if (initialParams) {
      console.log('initialParams', initialParams);
      detailSearch(initialParams.addrDetail);
    }
  }, [initialParams]);

  useEffect(() => {
    if (mapDetailData) {
      const first = Object.values(mapDetailData)[0];
      const cardId = first?.cardUseAddrId;

      console.log('mapDetailData 받고나서 호출', first);
      cardFetch(cardId);
    }
  }, [mapDetailData]);

  // recoil detail 상태 반영
  useEffect(() => {
    if (mapDetailData) {
      setDetail(Object.values(mapDetailData)[0]);
    }
  }, [mapDetailData]);

  return (
    <DetailWrapper>
      {detail ? (
        <>
          <FixedTop>
            <TopContent>
              <ImageBox>
                {detail?.cardUseImageUrl ? (
                  <img src={detail?.cardUseImageUrl} alt="식당 대표 이미지" />
                ) : (
                  <img src={basicLogo} alt="기본 로고 이미지" />
                )}
              </ImageBox>
              <InfoBox>
                <Title>
                  {detail.addrName} <span>{initialParams.catLabel}</span>
                </Title>
                <SubIconBox>
                  <IconSpan
                    $color="#1A7D55"
                    data-active={cardData?.myVisited === 'Y'}
                    onClick={() => handleSubCreate(true)}
                  >
                    가봤어요
                    <GiFootprint />
                    <span>{cardData?.visitedCnt}명</span>
                    <FaCheckCircle className="check" />
                  </IconSpan>

                  <IconSpan
                    $color="#FF5E57"
                    data-active={cardData?.mySpicioused === 'Y'}
                    onClick={() => handleSubCreate(false)}
                  >
                    궁금해요
                    <FaEye />
                    <span>{cardData?.suspiciousedCnt}명</span>
                    <FaCheckCircle className="check" />
                  </IconSpan>
                </SubIconBox>
                <MetaGroup>
                  <strong>{detail.visitMember} 방문</strong>
                  <span>{detail.addrDetail}</span>
                </MetaGroup>
                <TotalPrice>
                  총 이용 금액 <strong>{detail.totalSum.toLocaleString()}원</strong>
                </TotalPrice>
                <SubMeta>방문횟수 {detail.visits}</SubMeta>
                <BottomCards>
                  {detail.cardUseGroupedResponses.map((item, i) => (
                    <VisitCard key={i} data={item} />
                  ))}
                </BottomCards>
              </InfoBox>
            </TopContent>
          </FixedTop>
          <ScrollableBottom>
            <CommentSection>
              <CommentTitle>
                <FaCommentDots style={{ marginRight: '4px' }} />
                댓글 <span>{detail.replies.length}건</span>
                {user && !showInput && (
                  <WriteButton
                    onClick={() => {
                      setShowInput(true);
                      setTimeout(() => {
                        inputRef.current?.focus();
                      }, 0); // 렌더 직후 포커스
                    }}
                  >
                    <PenIcon size={12} />
                    작성
                  </WriteButton>
                )}
              </CommentTitle>
              <CommentItemSection>
                {detail.replies.map(reply => (
                  <CommentItem key={reply.replyId}>
                    <CommentTop>
                      <Avatar src={reply.memberImageUrl} alt="프로필 이미지" />
                      <CommentText>
                        <strong>{reply.memberName} </strong> 2025.06.12
                      </CommentText>
                      {editingReplyId === reply.replyId ? (
                        <>
                          <WriteButton
                            onClick={() => {
                              showConfirmModal({
                                title: <>댓글 수정</>,
                                message: <>해당 댓글을 수정하시겠습니까?</>,
                                gb: true,
                                firstText: '취소',
                                secondText: '수정',
                                onConfirm: () => handleUpdate(reply),
                              });
                            }}
                          >
                            저장
                          </WriteButton>
                          <WriteButton
                            onClick={() => {
                              setEditingReplyId(null);
                              setEditingText('');
                              setEditImage({});
                              setEditingImageUrls(prev => {
                                const newMap = { ...prev };
                                delete newMap[reply.replyId];
                                return newMap;
                              });
                            }}
                          >
                            취소
                          </WriteButton>
                        </>
                      ) : (
                        <>
                          {!showInput && user?.email === reply.memberEmail && (
                            <>
                              <WriteButton
                                onClick={() => {
                                  setEditingReplyId(reply.replyId);
                                  setEditingText(reply.replyCont);
                                  setEditingImageUrls(prev => ({
                                    ...prev,
                                    [reply.replyId]: [...reply.replyImageUrls], // 복사해서 저장
                                  }));
                                }}
                              >
                                수정
                              </WriteButton>
                              <WriteButton
                                onClick={() => {
                                  showConfirmModal({
                                    title: <>댓글 삭제</>,
                                    message: <>해당 댓글을 삭제하시겠습니까?</>,
                                    gb: false,
                                    firstText: '취소',
                                    secondText: '삭제',
                                    onConfirm: async () => {
                                      try {
                                        await api.delete(`/cham/reply/${reply.replyId}`);
                                        toast.success('댓글이 삭제되었습니다.');
                                        await detailSearch(initialParams.addrDetail);
                                      } catch (e) {
                                        toast.error('댓글 삭제가 실패했습니다.');
                                      }
                                    },
                                  });
                                }}
                              >
                                삭제
                              </WriteButton>
                            </>
                          )}
                        </>
                      )}
                    </CommentTop>
                    <ContBox>
                      {/* 기존 이미지 리스트 */}
                      <ImageRow>
                        {editingReplyId === reply.replyId
                          ? editingImageUrls[reply.replyId]
                              ?.concat(editImage[reply.replyId]?.previews || [])
                              .slice(0, 3)
                              .map((imgUrl, idx) => (
                                <ImageWrapper key={idx}>
                                  <ImageThumb src={imgUrl} alt={`이미지 ${idx + 1}`} />
                                  {/* 기존 이미지인 경우에만 삭제 버튼 활성화 */}
                                  {idx < (editingImageUrls[reply.replyId]?.length || 0) && (
                                    <DeleteBtn
                                      onClick={() => {
                                        setEditingImageUrls(prev => ({
                                          ...prev,
                                          [reply.replyId]: prev[reply.replyId].filter(
                                            url => url !== imgUrl
                                          ),
                                        }));
                                      }}
                                    >
                                      <MdDelete />
                                    </DeleteBtn>
                                  )}
                                </ImageWrapper>
                              ))
                          : reply.replyImageUrls?.slice(0, 3).map((imgUrl, idx) => (
                              <ImageWrapper key={idx}>
                                <ImageThumb
                                  src={imgUrl}
                                  alt={`이미지 ${idx + 1}`}
                                  onClick={() => {
                                    setLightboxImages(reply.replyImageUrls);
                                    setLightboxIndex(idx);
                                  }}
                                />
                              </ImageWrapper>
                            ))}
                      </ImageRow>

                      {/* 텍스트 영역 */}
                      {editingReplyId === reply.replyId ? (
                        <>
                          <ButtonRow>
                            <WriteButton
                              onClick={() =>
                                document.getElementById(`edit-image-${reply.replyId}`).click()
                              }
                            >
                              + 이미지 추가
                            </WriteButton>
                          </ButtonRow>
                          <TextArea
                            value={editingText}
                            onChange={e => setEditingText(e.target.value)}
                            rows={3}
                          />

                          {/* 새 이미지 추가 버튼 */}
                          <div>
                            <input
                              type="file"
                              multiple
                              accept="image/*"
                              style={{ display: 'none' }}
                              id={`edit-image-${reply.replyId}`}
                              onChange={e => {
                                const existingUrls = editingImageUrls[reply.replyId] || [];
                                const currentFiles = editImage[reply.replyId]?.files || [];
                                const selectedFiles = Array.from(e.target.files || []);
                                const totalCount = existingUrls.length + currentFiles.length;
                                const allowedCount = Math.max(0, 3 - totalCount);

                                if (allowedCount <= 0) {
                                  toast.warning('이미지는 3장까지 첨부할 수 있습니다.');
                                  return;
                                }

                                const limitedFiles = selectedFiles.slice(0, allowedCount);
                                const previews = limitedFiles.map(file =>
                                  URL.createObjectURL(file)
                                );

                                setEditImage(prev => ({
                                  ...prev,
                                  [reply.replyId]: {
                                    ...(prev[reply.replyId] || {}),
                                    files: [...(prev[reply.replyId]?.files || []), ...limitedFiles],
                                    previews: [
                                      ...(prev[reply.replyId]?.previews || []),
                                      ...previews,
                                    ],
                                  },
                                }));

                                if (selectedFiles.length > allowedCount) {
                                  toast.warning('이미지는 3장까지 첨부할 수 있습니다.');
                                }
                              }}
                            />
                          </div>
                        </>
                      ) : (
                        <TextContent>{reply.replyCont}</TextContent>
                      )}
                    </ContBox>
                  </CommentItem>
                ))}
                {showInput && (
                  <InputBox>
                    <TextareaBox>
                      {imagePreviews.length > 0 && (
                        <PreviewContainer>
                          {imagePreviews.map((src, idx) => (
                            <Preview key={idx} src={src} alt={`preview-${idx}`} />
                          ))}
                        </PreviewContainer>
                      )}
                      <ButtonRow>
                        <WriteButton onClick={() => document.getElementById('fileInput')?.click()}>
                          + 이미지 첨부
                        </WriteButton>
                        <WriteButton onClick={handleCreate}>저장</WriteButton>
                        <WriteButton
                          onClick={() => {
                            setShowInput(false);
                            setEditingText('');
                            setImageFiles([]);
                            setImagePreviews([]);
                          }}
                        >
                          취소
                        </WriteButton>
                      </ButtonRow>
                      <TextArea
                        ref={inputRef}
                        placeholder="댓글을 입력하세요"
                        rows={3}
                        value={editingText}
                        onChange={e => setEditingText(e.target.value)}
                      />
                      <input
                        id="fileInput"
                        type="file"
                        accept="image/*"
                        multiple
                        style={{ display: 'none' }}
                        onChange={e => {
                          const files = Array.from(e.target.files).slice(0, 3);
                          const previews = files.map(file => URL.createObjectURL(file));
                          setImageFiles(files);
                          setImagePreviews(previews);
                        }}
                      />
                    </TextareaBox>
                  </InputBox>
                )}
              </CommentItemSection>
            </CommentSection>
          </ScrollableBottom>
        </>
      ) : (
        <></>
      )}
      <Lightbox
        open={lightboxIndex >= 0}
        close={() => setLightboxIndex(-1)}
        index={lightboxIndex}
        slides={lightboxImages.map(src => ({ src }))}
        on={{ view: ({ index }) => setLightboxIndex(index) }}
        render={{
          buttonPrev: lightboxIndex > 0 ? undefined : () => null,
          buttonNext: lightboxIndex < lightboxImages.length - 1 ? undefined : () => null,
          slide: ({ slide }) => (
            <div
              style={{
                width: '100%',
                height: '100%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                overflow: 'hidden',
              }}
              onTouchStart={e => e.stopPropagation()}
              onTouchMove={e => e.stopPropagation()}
              onTouchEnd={e => e.stopPropagation()}
              onPointerDown={e => e.stopPropagation()}
              onPointerMove={e => e.stopPropagation()}
              onPointerUp={e => e.stopPropagation()}
              draggable={false}
            >
              <img
                src={slide.src}
                alt=""
                style={{
                  maxWidth: '100%',
                  maxHeight: '100%',
                  userSelect: 'none',
                  pointerEvents: 'none',
                }}
              />
            </div>
          ),
        }}
      />
      {isLoginModalOpen && (
        <Modal open={isLoginModalOpen} onClose={() => setIsLoginModalOpen(false)} title="로그인">
          <LoginMoadl />
        </Modal>
      )}
    </DetailWrapper>
  );
}

const DetailWrapper = styled.section`
  display: flex;
  flex-direction: column;
  height: 100%;
  margin: 0 auto;
  overflow-y: hidden;
  width: 1200px;
  min-height: 600px;
  @media screen and ${({ theme }) => theme.device.mobile} {
    max-width: 100%;
    min-width: 100%;
    min-height: unset;
    height: 600px;
    overflow-y: auto;
  }
`;

const FixedTop = styled.div`
  flex-shrink: 0;
  padding: 20px;
`;

const ScrollableBottom = styled.div`
  flex: 1;
  padding: 0 20px;
  overflow-y: auto;
  @media screen and ${({ theme }) => theme.device.mobile} {
    overflow-y: unset;
  }
`;

const TopContent = styled.div`
  display: flex;
  gap: 20px;
  height: 100%;
  @media screen and ${({ theme }) => theme.device.mobile} {
    flex-direction: column;
    height: auto;
  }
`;

const ImageBox = styled.div`
  flex: 1;
  min-width: 300px;
  height: 323px;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 8px;
  }
`;

const InfoBox = styled.div`
  flex: 2;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
`;

const Title = styled.span`
  color: ${({ theme }) => theme.colors.primary};
  font-size: ${({ theme }) => theme.sizes.menu};
  font-weight: bold;

  > span {
    color: ${({ theme }) => theme.colors.liteGray};
    font-size: ${({ theme }) => theme.sizes.medium};
  }
`;

const SubMeta = styled.span`
  font-size: 14px;
  color: #666;
  margin: 6px 0;
`;

const SubIconBox = styled.div`
  display: flex;
  font-size: 14px;
`;

const IconSpan = styled.span`
  display: flex;
  gap: 4px;
  color: ${({ $color }) => $color};
  font-weight: bold;
  cursor: pointer;
  align-items: center;

  .check {
    opacity: 0;
    will-change: transform, opacity;
    backface-visibility: hidden;
    transform: translateZ(0);
  }

  &[data-active='true'] .check {
    opacity: 1;
    transform: scale(1.3);
    animation: popShrink 0.4s ease forwards;
    margin-right: 10px;
  }

  @keyframes popShrink {
    0% {
      transform: scale(0.6);
      opacity: 0;
    }
    40% {
      transform: scale(1.4);
      opacity: 1;
    }
    100% {
      transform: scale(1);
      opacity: 1;
    }
  }

  > span {
    color: ${({ theme }) => theme.colors.text};
    font-weight: normal;
  }

  @media ${({ theme }) => theme.device.device} {
    &:hover {
      transform: scale(1.1);
      transform-origin: left center;
      margin-right: 10px;
    }
  }
`;

const MetaGroup = styled.div`
  display: flex;
  list-style: none;
  padding: 0;
  margin: 12px 0;
  flex-direction: column;
  color: ${({ theme }) => theme.colors.liteGray};

  strong {
    font-weight: bold;
    font-size: ${({ theme }) => theme.sizes.lage};
  }
  span {
    font-size: ${({ theme }) => theme.sizes.medium};
  }
`;

const TotalPrice = styled.p`
  font-size: ${({ theme }) => theme.sizes.large};
  font-weight: bold;
  margin: 12px 0;
`;

const CommentSection = styled.div`
  font-size: 13px;
  color: #333;
  margin-top: 12px;
`;

const CommentTitle = styled.div`
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  font-weight: bold;
  font-size: ${({ theme }) => theme.sizes.large};
  color: ${({ theme }) => theme.colors.primary};
  gap: 2px;

  span {
    margin: 0 4px;
    font-size: ${({ theme }) => theme.sizes.medium};
    color: ${({ theme }) => theme.colors.liteGray};
  }
`;

const WriteButton = styled.button`
  display: flex;
  align-items: center;
  background: ${({ theme }) => theme.colors.primary};
  border: none;
  color: white;
  font-weight: bold;
  font-size: ${({ theme }) => theme.sizes.small};
  padding: 6px 10px;
  margin-left: 4px;
  border-radius: 999px;
  cursor: pointer;
  white-space: nowrap;
`;

const PenIcon = styled(FaPen)`
  margin-right: 6px;
`;

const BottomCards = styled.div`
  display: flex;
  overflow-x: auto;
  margin-top: auto;
  gap: 12px;
  min-width: 0;
  max-width: 100%;

  & > * {
    flex: 0 0 calc(50% - 6px);
    width: calc(50% - 6px);
  }

  @media ${({ theme }) => theme.device.mobile} {
    & > * {
      flex: 0 0 calc(100%);
      width: calc(100%);
    }
  }
`;

const CommentItemSection = styled.div`
  display: flex;
  flex-direction: column;
  padding: 4px 0;
  gap: 6px;
  overflow-y: auto;

  input {
    border: none;
    width: 100%;
    border-bottom: 2px solid ${({ theme }) => theme.colors.primary};
    padding: 4px;
    &:focus {
      outline: none;
      border-bottom: 2px solid ${({ theme }) => theme.colors.primary};
    }
  }

  img {
    cursor: pointer;
    &:hover {
      opacity: 0.8;
    }
  }
`;

const CommentItem = styled.div`
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
`;

const Avatar = styled.img`
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 12px;
`;

const CommentText = styled.p`
  margin-right: 12px;
  font-size: ${({ theme }) => theme.sizes.medium};
  color: ${({ theme }) => theme.colors.liteGray};
  font-weight: bold;
  strong {
    margin-right: 4px;
    color: ${({ theme }) => theme.colors.primary};
    font-size: 16px;
  }
`;

const InputBox = styled.div`
  display: flex;
  width: 100%;
`;

const ContBox = styled.div`
  display: flex;
  flex-direction: column;
  margin-top: 8px;
  gap: 8px;
`;

const ImageRow = styled.div`
  display: flex;
  gap: 8px;
`;

const ImageThumb = styled.img`
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  margin: 20px 0 10px 0;
`;

const TextContent = styled.p`
  font-size: ${({ theme }) => theme.sizes.medium};
  font-weight: bold;
  color: ${({ theme }) => theme.colors.liteGray};
  white-space: pre-wrap;
  margin-top: 10px;
`;

const CommentTop = styled.div`
  display: flex;
  align-items: center;
`;

const TextareaBox = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
`;

const PreviewContainer = styled.div`
  display: flex;
  gap: 8px;
`;

const Preview = styled.img`
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid ${({ theme }) => theme.colors.border};
`;

const ButtonRow = styled.div`
  display: flex;
  gap: 2px;
  margin-top: 6px;
`;

const TextArea = styled.textarea`
  width: 100%;
  padding: 8px;
  resize: none;
  font-size: ${({ theme }) => theme.sizes.medium};
  border: 2px solid ${({ theme }) => theme.colors.primary};
  border-radius: 6px;
  margin-bottom: 20px;
  &:focus {
    outline: none;
    border-color: ${({ theme }) => theme.colors.primary};
  }
`;

const ImageWrapper = styled.div`
  position: relative;
`;

const DeleteBtn = styled.button`
  position: absolute;
  top: 50%;
  right: 30px;
  background: red;
  color: white;
  border: none;
  border-radius: 999px;
  width: 20px;
  height: 20px;
  font-size: 12px;
  cursor: pointer;
`;
