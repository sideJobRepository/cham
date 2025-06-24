import styled from 'styled-components';
import VisitCard from '../components/VisiitCard.jsx';
import { useEffect, useRef, useState } from 'react';
import { FaCommentDots } from 'react-icons/fa';
import { FaPen } from 'react-icons/fa';

import { useMapSearch } from '@/recoil/fetchAppState.js';
import { useDetailMapState } from '@/recoil/useAppState.js';
import { useRecoilValue } from 'recoil';
import { userState } from '@/recoil/appState.js';
import { useSearchParams } from 'react-router-dom';
import api from '@/utils/axiosInstance.js';
import { toast } from 'react-toastify';
import { confirmAlert } from 'react-confirm-alert';
import 'react-confirm-alert/src/react-confirm-alert.css';
import basicLogo from '/basicLogo.png';

import Lightbox from 'yet-another-react-lightbox';
import 'yet-another-react-lightbox/styles.css';

export default function DetailPage() {
  const [searchParams] = useSearchParams();
  const { mapDetailData } = useDetailMapState();
  const [detail, SetDetail] = useState(null);

  const [editingReplyId, setEditingReplyId] = useState(null);
  const [editingText, setEditingText] = useState('');

  const [imagePreviews, setImagePreviews] = useState([]);
  const [imageFiles, setImageFiles] = useState([]);

  const [editImage, setEditImage] = useState({});

  //수정시 이미지 복사
  const [editingImageUrls, setEditingImageUrls] = useState({});

  const paramsObj = Object.fromEntries(searchParams.entries());

  const user = useRecoilValue(userState);

  const detailSearch = useMapSearch();

  const [showInput, setShowInput] = useState(false);
  const inputRef = useRef(null);

  const [lightboxIndex, setLightboxIndex] = useState(-1);
  const [lightboxImages, setLightboxImages] = useState([]);

  const handleCreate = async () => {
    const replyCont = inputRef.current.value;
    const memberId = user?.memberId;
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

    try {
      await api.post('/cham/reply', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      setShowInput(false);
      toast.success('댓글이 저장되었습니다.');
      await detailSearch(paramsObj);
    } catch (e) {
      toast.error('댓글 저장이 실패했습니다.');
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

    try {
      await api.put('/cham/reply', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      toast.success('댓글이 수정되었습니다.');

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

      await detailSearch(paramsObj);
    } catch (e) {
      toast.error('댓글 수정이 실패했습니다.');
    }
  };

  useEffect(() => {
    detailSearch(paramsObj);
  }, []);

  useEffect(() => {
    if (mapDetailData) {
      SetDetail(Object.values(mapDetailData)[0]);
      console.log('mapDe', mapDetailData);
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
                  <img src={detail?.cardUseImageUrl} alt="sample" />
                ) : (
                  <img src={basicLogo} alt="sample" />
                )}
              </ImageBox>
              <InfoBox>
                <Title>{detail.visitMember} 방문</Title>
                <SubMeta>방문횟수 {detail.visits}</SubMeta>
                <MetaGroup>
                  <strong>{detail.addrName}</strong>
                  <span>{detail.addrDetail}</span>
                </MetaGroup>
                <TotalPrice>
                  총 이용 금액 <strong>{detail.totalSum.toLocaleString()}원</strong>
                </TotalPrice>
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
                              confirmAlert({
                                message: '해당 댓글을 수정하시겠습니까?',
                                buttons: [
                                  {
                                    label: '수정',
                                    onClick: () => handleUpdate(reply),
                                  },
                                  {
                                    label: '취소',
                                    onClick: () => {},
                                  },
                                ],
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
                                  confirmAlert({
                                    message: '해당 댓글을 삭제하시겠습니까?',
                                    buttons: [
                                      {
                                        label: '삭제',
                                        onClick: async () => {
                                          try {
                                            await api.delete(`/cham/reply/${reply.replyId}`);
                                            toast.success('댓글이 삭제되었습니다.');
                                            await detailSearch(paramsObj);
                                          } catch (e) {
                                            toast.error('댓글 삭제가 실패했습니다.');
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
                                      ×
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
                                    setLightboxImages(reply.replyImageUrls); // or editingImageUrls[reply.replyId]
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
                        <WriteButton onClick={() => setShowInput(false)}>취소</WriteButton>
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

          // 👉 이미지 드래그/스와이프 차단
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
    </DetailWrapper>
  );
}

const DetailWrapper = styled.section`
  display: flex;
  flex-direction: column;
  height: 100%;
  margin: 0 auto;
  overflow-y: hidden;
  @media screen and ${({ theme }) => theme.device.mobile} {
    overflow-y: unset;
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

const EmptyImage = styled.div`
  width: 100%;
  height: 100%;
  border-radius: 8px;
  background-color: ${({ theme }) => theme.colors.liteGray};
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;

  span {
    color: white;
  }
`;

const InfoBox = styled.div`
  flex: 2;
  display: flex;
  flex-direction: column;
`;

const Title = styled.span`
  color: ${({ theme }) => theme.colors.primary};
  font-size: ${({ theme }) => theme.sizes.menu};
  font-weight: bold;
`;

const SubMeta = styled.span`
  font-size: 14px;
  color: #666;
  margin: 6px 0;
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

  & > * {
    flex: 0 0 calc(50% - 6px); /* gap 고려해서 반씩 */
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
