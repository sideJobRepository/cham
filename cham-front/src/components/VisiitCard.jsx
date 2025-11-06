import styled from 'styled-components';
import test from '/test.jpg';

export default function VisitCard(item) {
  return (
    <CardWrapper>
      <CardContent>
        <CardTitle>
          {item.data.region} {item.data.useUser} {item.data.userName}
          <span>
            대상인원{' '}
            {Number(item.data.cardUsePersonnel)
              ? item.data.cardUsePersonnel + '명'
              : item.data.cardUsePersonnel}
          </span>
        </CardTitle>
        <CardMeta>사용일시 {item.data.cardUseDate}</CardMeta>
        <CardPrice>
          {item.data.cardUseMethod} {item.data.cardUseAmount.toLocaleString()}원 사용
        </CardPrice>
        <CardDesc>1인당 {item.data.amountPerPerson} 이용</CardDesc>
        <CardText>{item.data.cardUsePurpose}</CardText>
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

  @media ${({ theme }) => theme.device.mobile} {
    flex-direction: column;
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
  word-break: keep-all;
  margin: 0;
  color: black;
  font-size: ${({ theme }) => theme.sizes.large};
  font-weight: bold;

  span {
    margin-left: 8px;
    word-break: keep-all;
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
  margin-top: 12px;
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
  margin-top: 12px;
  font-weight: bold;
  font-size: ${({ theme }) => theme.sizes.medium};
  color: ${({ theme }) => theme.colors.liteGray};
  strong {
    margin-right: 4px;
  }
`;
