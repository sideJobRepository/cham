import styled from 'styled-components';
import ItemCard from './ItemCard.jsx';
import { useEffect } from 'react';
import { useSearchMapState } from '@/recoil/useAppState.js';

export default function Sidebar() {
  const { mapData, mapLoading } = useSearchMapState();

  useEffect(() => {
    //맵데이터
    if (!mapLoading && mapData) {
      console.log('데이터', mapData);
    }
  }, [mapLoading, mapData]);
  const dummy = Array.from({ length: 20 });

  return (
    <Wrapper>
      {mapData && Object.keys(mapData).length > 0 ? (
        Object.entries(mapData).map(([key, value]) => <ItemCard key={key} data={value} />)
      ) : (
        <EmptyMessage>검색 결과가 없습니다.</EmptyMessage>
      )}
    </Wrapper>
  );
}

const Wrapper = styled.aside`
  width: 100%;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;

  @media ${({ theme }) => theme.device.mobile} {
    display: flex;
    overflow-x: auto;
    padding: 16px 0;
    gap: 12px;

    & > * {
      flex: 0 0 calc(50% - 6px); // 한 화면에 2개씩 보이게
      width: calc(50% - 6px);
    }
  }
`;

const EmptyMessage = styled.div`
  width: 100%;
  font-weight: bold;
  font-size: ${({ theme }) => theme.sizes.large};
  color: ${({ theme }) => theme.colors.liteGray};
`;
