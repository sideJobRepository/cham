import styled from 'styled-components';
import test from '/test.jpg';
import { useNavigate } from 'react-router-dom';
import { useRecoilValue, useSetRecoilState } from 'recoil';
import { mapSearchFilterState, selectedCardDataState } from '@/recoil/appState.js';

export default function ItemCard({ data }) {
  const searchCondition = useRecoilValue(mapSearchFilterState);

  const handleClick = data => {
    const query = new URLSearchParams({
      cardOwnerPositionId: searchCondition.selectedRole?.value,
      cardUseName: searchCondition.cardUseName,
      numberOfVisits: searchCondition.numberOfVisits,
      startDate: searchCondition.startDate?.toISOString().split('T')[0],
      endDate: searchCondition.endDate?.toISOString().split('T')[0],
      sortOrder: searchCondition.sortOrder,
      addrDetail: data.addrDetail,
      detail: true,
      // 필요한 필드 추가
    }).toString();
    window.open(`/detail?${query}`, '_blank');
  };

  return (
    <Card
      onClick={() => {
        handleClick(data);
      }}
    >
      {data?.cardUseImageUrl ? (
        <img src={data?.cardUseImageUrl} alt="sample" />
      ) : (
        <img src={test} alt="sample" />
      )}
      <CardBody>
        <Title>{data.visitMember} 방문</Title>
        <Stats>방문횟수 {data.visits}</Stats>
        <Price>총 {data.totalSum.toLocaleString()}원</Price>
        <Menu>{data.addrName}</Menu>
        <Address>{data.addrDetail}</Address>
      </CardBody>
    </Card>
  );
}

const Card = styled.article`
  overflow: hidden;
  width: 100%;

  //max-height: 140px;
  box-sizing: border-box;
  cursor: pointer;

  img {
    width: 100%;
    height: 180px;
    border-radius: 8px;
    object-fit: cover;
  }
`;

const CardBody = styled.div`
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  position: relative;
`;

const Title = styled.span`
  font-size: ${({ theme }) => theme.sizes.large};
  font-weight: bold;
  color: ${({ theme }) => theme.colors.primary};
  margin: 0;
`;

const Stats = styled.div`
  display: flex;
  font-size: ${({ theme }) => theme.sizes.medium};
  color: ${({ theme }) => theme.colors.liteGray};
`;

const Menu = styled.p`
  font-size: ${({ theme }) => theme.sizes.medium};
  margin-top: 12px;
  color: ${({ theme }) => theme.colors.liteGray};
  font-weight: bold;
`;

const Address = styled.p`
  font-size: ${({ theme }) => theme.sizes.medium};
  color: ${({ theme }) => theme.colors.liteGray};
`;

const Price = styled.p`
  font-size: ${({ theme }) => theme.sizes.large};
  font-weight: bold;
  color: black;
  margin-top: 12px;
`;
