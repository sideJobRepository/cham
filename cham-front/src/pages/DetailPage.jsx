import styled from 'styled-components';
import test from '/test.jpg';
import profile from '/profile.png';
import VisitCard from '../components/VisiitCard.jsx';
import { useEffect, useState } from 'react';
import { FaCommentDots } from 'react-icons/fa';
import { FaPen } from 'react-icons/fa';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useMapSearch } from '@/recoil/fetchAppState.js';
import { useDetailMapState } from '@/recoil/useAppState.js';

export default function DetailPage() {
  const [searchParams] = useSearchParams();
  const { mapDetailData } = useDetailMapState();
  const [detail, SetDetail] = useState(null);

  const navigate = useNavigate();

  const detailSearch = useMapSearch();

  useEffect(() => {
    const paramsObj = Object.fromEntries(searchParams.entries());
    detailSearch(paramsObj);
  }, []);

  useEffect(() => {
    if (mapDetailData) {
      SetDetail(Object.values(mapDetailData)[0]);
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
                  <img src={test} alt="sample" />
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
                <CommentSection>
                  <CommentTitle>
                    <FaCommentDots style={{ marginRight: '4px' }} />
                    댓글 <span>3건</span>
                    <WriteButton>
                      <PenIcon size={12} />
                      작성
                    </WriteButton>
                  </CommentTitle>
                  <CommentItemSection>
                    <CommentItem>
                      <Avatar src={profile} />
                      <CommentText>
                        <strong>박용우 : </strong> 여기 진짜 리얼 맛집입니다.
                      </CommentText>
                    </CommentItem>
                  </CommentItemSection>
                </CommentSection>
              </InfoBox>
            </TopContent>
          </FixedTop>
          <ScrollableBottom>
            <BottomCards>
              {detail.cardUseGroupedResponses.map((item, i) => (
                <VisitCard key={i} data={item} />
              ))}
            </BottomCards>
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
  margin-left: 8px;
  border-radius: 999px;
  cursor: pointer;
  white-space: nowrap;
`;

const PenIcon = styled(FaPen)`
  margin-right: 6px;
`;

const BottomCards = styled.div`
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;

  @media ${({ theme }) => theme.device.mobile} {
    display: flex;
    overflow-x: auto;
    padding-bottom: 20px;
    gap: 12px;

    & > * {
      flex: 0 0 calc(100%); // 한 화면에 2개씩 보이게
      width: calc(100%);
    }
  }
`;

const CommentItemSection = styled.div`
  display: flex;
  flex-direction: column;
  height: 120px;
  padding: 4px 0;
  gap: 6px;
  overflow-y: auto;
`;

const CommentItem = styled.div`
  display: flex;
  align-items: center;
`;

const Avatar = styled.img`
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 8px;
`;

const CommentText = styled.p`
  margin: 0;
  font-size: 13px;
`;
