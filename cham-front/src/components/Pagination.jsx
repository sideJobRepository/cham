// Pagination.jsx
import styled from 'styled-components';
import { ReactComponent as PrevIcon } from '../assets/prev.svg?react';
import { ReactComponent as NextIcon } from '../assets/next.svg?react';

export default function Pagination({ current, totalPages, onChange }) {
  if (!totalPages || totalPages < 1) return null;

  const windowSize = 6;

  const pages = () => {
    if (!totalPages || totalPages <= 0) return [];
    const last = totalPages - 1;

    // 전체 페이지가 7 이하라면 모두 표시
    if (totalPages <= windowSize + 1) {
      return Array.from({ length: totalPages }, (_, i) => i);
    }

    // current가 앞쪽에 있을 때
    if (current <= windowSize - 1) {
      return [...Array.from({ length: windowSize }, (_, i) => i), '···', last];
    }

    // current가 뒤쪽에 있을 때
    if (current >= last - (windowSize - 1)) {
      const start = last - (windowSize - 1);
      return [0, '···', ...Array.from({ length: windowSize }, (_, i) => start + i)];
    }

    // 가운데 구간
    const left = Math.max(0, current - 2);
    const right = Math.min(last, current + 2);

    return [0, '···', left, left + 1, current, right - 1, right, '···', last]
      .filter((v, i, arr) => typeof v === 'string' || arr.indexOf(v) === i)
      .filter(v => typeof v === 'string' || (v >= 0 && v <= last));
  };

  return (
    <Nav aria-label="pagination">
      <PrevIcon
        type="button"
        className={current === 0 ? 'active' : ''}
        aria-label="previous page"
        onClick={() => current > 0 && onChange(current - 1)}
      />

      <PageNumberBox>
        {pages().map((p, i) =>
          typeof p === 'number' ? (
            <PageButton
              key={i}
              type="button"
              aria-current={p === current ? 'page' : undefined}
              className={p === current ? 'active' : ''}
              onClick={() => onChange(p)}
            >
              {p + 1}
            </PageButton>
          ) : (
            <Ellipsis key={i}>···</Ellipsis>
          )
        )}
      </PageNumberBox>

      <NextIcon
        type="button"
        aria-label="next page"
        className={current === totalPages - 1 ? 'active' : ''}
        onClick={() => current < totalPages - 1 && onChange(current + 1)}
      />
    </Nav>
  );
}

const Nav = styled.nav`
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: center;
  margin: 0 auto;

  svg {
    width: 24px;
    height: 24px;
    color: #66696d;
    cursor: pointer;

    &.active {
      color: #222;
    }
  }
`;

const PageNumberBox = styled.div`
  display: flex;
  gap: 8px;
  padding: 8px 0;
`;

const PageButton = styled.button`
  background: transparent;
  border: none;
  cursor: pointer;
  color: #66696d;
  font-size: ${({ theme }) => theme.sizes.medium};

  &.active {
    color: #222;
  }

  &:hover:not(.active) {
    opacity: 0.8;
  }
`;

const Ellipsis = styled.span`
  color: #66696d;
  user-select: none;
`;
