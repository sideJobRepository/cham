import styled from 'styled-components';
import TopHeader from './TopHeader.jsx';
import { Outlet } from 'react-router-dom';
import { useEffect } from 'react';
import { useFetchSelectSearch } from '@/recoil/fetchAppState.js';
import { useRecoilValue, useSetRecoilState } from 'recoil';
import { userState } from '@/recoil/appState.js';

export default function Layout() {
  const fetchSelect = useFetchSelectSearch();
  const setUser = useSetRecoilState(userState);
  const user = useRecoilValue(userState);

  useEffect(() => {
    fetchSelect(); // 서버에서 최초 한 번 불러오기
  }, []);

  //유저정보
  useEffect(() => {
    const stored = sessionStorage.getItem('user');
    if (!user) {
      setUser(JSON.parse(stored));
    }
  }, []);

  useEffect(() => {
    const channel = new BroadcastChannel('auth');

    channel.onmessage = event => {
      if (event.data === 'logout') {
        sessionStorage.clear();
        setUser(null);
      }

      if (event.data.type === 'login') {
        sessionStorage.setItem('user', JSON.stringify(event.data.user));
        setUser(event.data.user);
      }
    };

    return () => {
      channel.close();
    };
  }, []);

  return (
    <Wrapper>
      <Inner>
        <TopArea>
          <TopHeader />
        </TopArea>
        <MainArea>
          <OutletWrapper>
            <Outlet />
          </OutletWrapper>
          <Footer>© 2025 디지털개발소. All rights reserved.</Footer>
        </MainArea>
      </Inner>
    </Wrapper>
  );
}

const Wrapper = styled.div`
  height: 100vh;
  overflow-y: hidden;

  button {
    &:hover {
      opacity: 0.8;
    }
  }
`;
const Inner = styled.div`
  max-width: 1500px;
  min-width: 1023px;
  height: 100%;
  display: flex;
  margin: 0 auto;
  flex-direction: column;
  @media ${({ theme }) => theme.device.mobile} {
    max-width: 100%;
    min-width: 100%;
  }
`;
const TopArea = styled.div`
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  height: 100px;
  border-bottom: 2px solid ${({ theme }) => theme.colors.topLine};
  z-index: 1000;
`;

const MainArea = styled.main`
  position: relative;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px);
  overflow-y: auto;
`;

const OutletWrapper = styled.div`
  flex: 1;
  height: 100%;
  @media ${({ theme }) => theme.device.mobile} {
    height: unset;
  }
`;

const Footer = styled.footer`
  padding: 20px 0;
  text-align: center;
  color: ${({ theme }) => theme.colors.liteGray};
  font-size: ${({ theme }) => theme.sizes.small};
  border-top: 1px solid ${({ theme }) => theme.colors.border};
`;
