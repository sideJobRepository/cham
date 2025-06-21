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
    const stored = localStorage.getItem('user');
    if (!user) {
      setUser(JSON.parse(stored));
    }
  }, []);

  useEffect(() => {
    const handleStorageChange = e => {
      if (e.key === 'user') {
        if (e.newValue) {
          // 다른 창에서 로그인됨
          try {
            const userObj = JSON.parse(e.newValue);
            setUser(userObj);
          } catch (error) {
            console.error('유저 파싱 실패', error);
          }
        } else {
          // 다른 창에서 로그아웃됨
          setUser(null);
        }
      }
    };

    window.addEventListener('storage', handleStorageChange);
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, []);

  return (
    <Wrapper>
      <Inner>
        <TopArea>
          <TopHeader />
        </TopArea>
        <MainArea>
          <Outlet />
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
  height: calc(100vh - 100px);
  overflow-y: auto;
`;
