import styled from 'styled-components';
import ItemCard from './ItemCard.jsx';

export default function Sidebar() {
  // 더미 8개
  const dummy = Array.from({ length: 20 });

  return (
    <Wrapper>
      {dummy.map((_, i) => (
        <ItemCard key={i} />
      ))}
    </Wrapper>
  );
}

const Wrapper = styled.aside`
  width: 100%;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;

  @media ${({ theme }) => theme.device.tablet} {
    display: flex;
    overflow-x: auto;
    padding: 16px 0;
    gap: 12px;

    & > * {
      flex: 0 0 calc(50% - 4px); // 한 화면에 2개씩 보이게
      width: calc(50% - 4px);
    }
  }
`;
