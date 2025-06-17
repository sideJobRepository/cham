import styled from 'styled-components';
import test from '/test.jpg';
import { useNavigate } from 'react-router-dom';

export default function ItemCard({ data }) {
  const navigate = useNavigate();

  return (
    <Card
      onClick={() => {
        navigate('detail');
      }}
    >
      <img src={test} alt="sample" />
      <CardBody>
        <Title>{data.visitMember} 방문</Title>
        <Stats>방문횟수 {data.visits}</Stats>
        <Menu>{data.addrName}</Menu>
        <Address>{data.addrDetail}</Address>
        <Price>총 {data.totalSum.toLocaleString()}원</Price>
      </CardBody>
    </Card>
  );
}

const Card = styled.article`
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: 8px;
  overflow: hidden;
  width: 100%;
  box-sizing: border-box;
  cursor: pointer;

  img {
    width: 100%;
    height: 180px;
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
