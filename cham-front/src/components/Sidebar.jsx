import styled from 'styled-components';
import ItemCard from './ItemCard.jsx';
import { useMemo } from 'react';
import { useRecoilValue } from 'recoil';
import { mapCenterAddrState, mapSearchFilterState } from '@/recoil/appState.js';
import { toast } from 'react-toastify';

export default function Sidebar() {
  const centerAddr = useRecoilValue(mapCenterAddrState);
  const searchCondition = useRecoilValue(mapSearchFilterState);

  const sortedList = useMemo(() => {
    const list = Object.values(centerAddr);

    return list.sort((a, b) => {
      const dateA = new Date(a.useDate);
      const dateB = new Date(b.useDate);

      const totalA = a.totalSum;
      const totalB = b.totalSum;

      const visitsA = a.visits;
      const visitsB = b.visits;

      if (searchCondition.sortOrder === 1) {
        // 최신순 (최근이 먼저)
        return dateB - dateA;
      } else if (searchCondition.sortOrder === 2) {
        // 오래된순 (과거가 먼저)
        return dateA - dateB;
      } else if (searchCondition.sortOrder === 3) {
        // 가격 높은순
        return totalB - totalA;
      } else if (searchCondition.sortOrder === 4) {
        //방문횟수 높은순
        return visitsB - visitsA;
      }
    });
  }, [centerAddr, searchCondition.sortOrder]);

  return (
    <Wrapper>
      {sortedList.length > 0 ? (
        sortedList.map((item, idx) => <ItemCard key={idx} data={item} />)
      ) : (
        <EmptyMessage>검색된 결과가 없습니다.</EmptyMessage>
      )}
    </Wrapper>
  );
}

const Wrapper = styled.aside`
  width: 100%;
  display: grid;
  grid-template-columns: repeat(1, 1fr);
`;

const EmptyMessage = styled.div`
  width: 100%;
  margin-top: 20px;
  font-weight: bold;
  font-size: ${({ theme }) => theme.sizes.large};
  color: ${({ theme }) => theme.colors.liteGray};
`;
