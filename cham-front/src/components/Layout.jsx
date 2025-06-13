import styled from 'styled-components';
import TopHeader from './TopHeader.jsx';
import { Outlet } from 'react-router-dom';

export default function Layout() {
  return (
    <Wrapper>
      <TopArea>
        <TopHeader />
      </TopArea>
      <MainArea>
        <Outlet />
      </MainArea>
    </Wrapper>
  );
}

const Wrapper = styled.div`
  height: 100vh;
  overflow: hidden;
`;

const TopArea = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 100px;
  border-bottom: 2px solid ${({ theme }) => theme.colors.topLine};
  z-index: 1000;
`;

const MainArea = styled.main`
  position: relative;
  margin-top: 100px;
  height: calc(100vh - 100px);
  overflow-y: auto;
`;
