import styled from 'styled-components';
import test from '/test.jpg';
import { useNavigate } from 'react-router-dom';
import { useSetRecoilState } from 'recoil';
import { selectedCardDataState } from '@/recoil/appState.js';

export default function ItemCard({ data }) {
  const navigate = useNavigate();

  const setSelectedCard = useSetRecoilState(selectedCardDataState);

  const handleClick = item => {
    setSelectedCard(item);
  };

  return (
    <Card
      onClick={() => {
        navigate('detail');
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
