import styled from 'styled-components';
import test from '/test.jpg';
import { FaUserFriends } from 'react-icons/fa';

export default function ItemCard() {
  return (
    <Card>
      <img src={test} alt="sample" />
      <CardBody>
        <Title>시장 외 2명 방문</Title>
        <Stats>
          <FaUserFriends size={12} style={{ marginRight: 4 }} /> 31
        </Stats>
        <Menu>춘추는 김치찌개</Menu>
        <Address>대전 은행동 은행사거리 고층빌딩 1층</Address>
        <Price>총 6,500,000원</Price>
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

const Title = styled.h4`
  font-size: 14px;
  font-weight: bold;
  color: ${({ theme }) => theme.colors.primary};
  margin: 0;
`;

const Stats = styled.div`
  display: flex;
  font-size: 12px;
  color: #666;
  position: absolute;
  top: 8px;
  right: 8px;
`;

const Menu = styled.p`
  font-size: 13px;
  margin: 4px 0 0;
  color: #111;
`;

const Address = styled.p`
  font-size: 12px;
  color: #555;
  margin: 0;
`;

const Price = styled.p`
  font-size: 13px;
  font-weight: bold;
  color: black;
  margin: 4px 0 0;
`;
