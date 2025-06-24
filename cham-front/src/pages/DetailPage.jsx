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

export default function DetailPage() {
  const [searchParams] = useSearchParams();
  const { mapDetailData } = useDetailMapState();
  const [detail, SetDetail] = useState(null);

  const [editingReplyId, setEditingReplyId] = useState(null);
  const [editingText, setEditingText] = useState('');

  const [imagePreviews, setImagePreviews] = useState([]);
  const [imageFiles, setImageFiles] = useState([]);

  const paramsObj = Object.fromEntries(searchParams.entries());

  const user = useRecoilValue(userState);

  const detailSearch = useMapSearch();

  const [showInput, setShowInput] = useState(false);
  const inputRef = useRef(null);

  const handleCreate = async () => {
    const replyCont = inputRef.current.value;
    const memberId = user?.memberId;
    const cardUseAddrId = detail.cardUseAddrId;

    const params = { cardUseAddrId, memberId, replyCont };

    try {
      await api.post('/cham/reply', params);
      setShowInput(false);
      toast.success('댓글이 저장되었습니다.');
      await detailSearch(paramsObj);
    } catch (e) {
      toast.error('댓글 저장이 실패했습니다.');
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
                  <EmptyImage>
                    <span>대표 이미지가 없습니다.</span>
                  </EmptyImage>
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
                                    onClick: async () => {
                                      try {
                                        await api.put('/cham/reply', {
                                          replyId: reply.replyId,
                                          replyCont: editingText,
                                        });
                                        toast.success('댓글이 수정되었습니다.');
                                        setEditingReplyId(null);
                                        await detailSearch(paramsObj);
                                      } catch (e) {
                                        toast.error('댓글 수정이 실패했습니다.');
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
                            저장
                          </WriteButton>
                          <WriteButton onClick={() => setEditingReplyId(null)}>취소</WriteButton>
                        </>
                      ) : (
                        <>
                          {!showInput && user?.email === reply.memberEmail && (
                            <>
                              <WriteButton
                                onClick={() => {
                                  setEditingReplyId(reply.replyId);
                                  setEditingText(reply.replyCont);
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
                      <ImageRow>
                        {/*{reply.replyImages?.slice(0, 3).map((imgUrl, idx) => (*/}
                        {/*  <ImageThumb key={idx} src={imgUrl} alt={`이미지 ${idx + 1}`} />*/}
                        {/*))}*/}
                        {(
                          reply.replyImages ?? [
                            'https://picsum.photos/id/237/80/80',
                            'https://picsum.photos/id/237/80/80',
                            'https://images.unsplash.com/photo-1503023345310-bd7c1de61c7d?w=80&h=80&fit=crop',
                          ]
                        )
                          .slice(0, 3)
                          .map((imgUrl, idx) => (
                            <ImageThumb key={idx} src={imgUrl} alt={`이미지 ${idx + 1}`} />
                          ))}
                      </ImageRow>
                      {/*<TextContent>{reply.replyCont}</TextContent>*/}
                      {editingReplyId === reply.replyId ? (
                        <TextArea
                          value={editingText}
                          onChange={e => setEditingText(e.target.value)}
                          rows={3}
                        />
                      ) : (
                        <TextContent>{reply.replyCont}</TextContent>
                      )}
                    </ContBox>
                  </CommentItem>
                ))}
                {showInput && (
                  <InputBox>
                    <TextareaBox>
                      <ButtonRow>
                        <WriteButton htmlFor="fileInput">+ 이미지 첨부</WriteButton>
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
                      {imagePreviews.length > 0 && (
                        <PreviewContainer>
                          {imagePreviews.map((src, idx) => (
                            <Preview key={idx} src={src} alt={`preview-${idx}`} />
                          ))}
                        </PreviewContainer>
                      )}
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
  margin-bottom: 10px;
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
`;

const CommentItem = styled.div`
  display: flex;
  flex-direction: column;
  margin-bottom: 10px;
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
  margin: 20px 0;
  gap: 8px;
`;

const ImageThumb = styled.img`
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
`;

const TextContent = styled.p`
  font-size: ${({ theme }) => theme.sizes.medium};
  font-weight: bold;
  color: ${({ theme }) => theme.colors.liteGray};
  white-space: pre-wrap;
`;

const CommentTop = styled.div`
  display: flex;
  margin-top: 12px;
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
