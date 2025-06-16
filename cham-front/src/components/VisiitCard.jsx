import styled from 'styled-components';
import test from '/test.jpg';

export default function VisitCard() {
  return (
    <CardWrapper>
      <CardContent>
        <CardTitle>
          하천관리사업소장
          <span>대상인원 10명</span>
        </CardTitle>
        <CardMeta>사용일시 2025.06.01 12:00</CardMeta>
        <CardPrice>계좌 150,000원 사용</CardPrice>
        <CardDesc>1인당 15,000원 이용</CardDesc>
        <CardText>
          <strong>목적 : </strong>
          간담회로 인한 식사 했고, 맛있게 식사 햇습니다. 간담회로 인한 식사 했고, 맛있게 식사
          햇습니다. 간담회로 인한 식사 했고, 맛있게 식사 햇습니다.
        </CardText>
      </CardContent>
    </CardWrapper>
  );
}

const CardWrapper = styled.div`
  display: flex;
  gap: 16px;
  padding: 12px;
  border: 1px solid ${({ theme }) => theme.colors.border || '#ddd'};
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;

  @media ${({ theme }) => theme.device.mobile} {
    flex-direction: column;
  }
`;

const Thumb = styled.div`
  width: 120px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
`;

const CardContent = styled.div`
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
`;

const CardTitle = styled.div`
  display: flex;
  align-items: center;
  margin: 0;
  color: black;
  font-size: ${({ theme }) => theme.sizes.large};
  font-weight: bold;

  span {
    margin-left: 8px;
    font-weight: normal;
    font-size: ${({ theme }) => theme.sizes.medium};
    color: ${({ theme }) => theme.colors.liteGray};
  }
`;

const CardMeta = styled.p`
  font-size: ${({ theme }) => theme.sizes.medium};
  color: ${({ theme }) => theme.colors.liteGray};
  font-weight: bold;
`;

const CardPrice = styled.div`
  display: flex;
  margin-top: 4px;
  font-size: ${({ theme }) => theme.sizes.large};
  color: ${({ theme }) => theme.colors.primary};
  font-weight: bold;
`;

const CardDesc = styled.p`
  font-weight: bold;
  font-size: ${({ theme }) => theme.sizes.medium};
  color: ${({ theme }) => theme.colors.liteGray};
`;

const CardText = styled.p`
  margin-top: 8px;
  font-weight: bold;
  font-size: ${({ theme }) => theme.sizes.medium};
  color: ${({ theme }) => theme.colors.liteGray};
  strong {
    margin-right: 4px;
  }
`;
