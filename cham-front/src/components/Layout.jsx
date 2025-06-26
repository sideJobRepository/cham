import styled, { useTheme } from 'styled-components';
import TopHeader from './TopHeader.jsx';
import { Outlet } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { useFetchSelectSearch } from '@/recoil/fetchAppState.js';
import { useRecoilValue, useSetRecoilState } from 'recoil';
import { userState } from '@/recoil/appState.js';

export default function Layout() {
  const fetchSelect = useFetchSelectSearch();
  const setUser = useSetRecoilState(userState);
  const user = useRecoilValue(userState);
  const theme = useTheme();
  const [isMobile, setIsMobile] = useState(false);

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

  useEffect(() => {
    const checkMobile = () => {
      const result = window.matchMedia(theme.device.mobile).matches;
      setIsMobile(result);
    };

    checkMobile();
    window.addEventListener('resize', checkMobile);

    return () => {
      window.removeEventListener('resize', checkMobile);
    };
  }, [theme.device.mobile]);

  return (
    <Wrapper>
      <Inner>
        <TopArea>
          <TopHeader />
        </TopArea>
        <MainArea>
          {isMobile ? (
            <OutletWrapper>
              <Outlet />
            </OutletWrapper>
          ) : (
            <Outlet />
          )}
          <Footer>
            <FooterSection>
              <h4>왜 만들었나요?</h4>
              <p>
                업무추진비 맛집지도’는 공공기관·지방정부가 사용한 업무추진비(판공비) 지출 내역을
                식당·카페 등 실제 결제 장소와 연결해 한눈에 보여주는 지도 서비스입니다. <br />
                시민 누구나 클릭 한 번으로 어디서 얼마를 무엇을 위해 썼는지를 확인할 수 있어, 예산
                집행의 투명성과 책임성을 높이는 데 목적이 있습니다. <br />
                “세금은 발자국을 남긴다”는 원칙으로, 작은 지출까지도 공개·검증되는 민주적 문화를
                함께 만들어 갑니다. <br />
                여러분의 참여가 더 투명한 사회를 만듭니다.
              </p>
            </FooterSection>

            <FooterSection>
              <h4>누가 만들었나요?</h4>
              <p>
                대전참여자치시민연대는 1995년부터 지역사회 예산 감시·정책 제안·시민교육을 통해
                “시민이 주인 되는 대전”을 꿈꾸는 독립적인 시민단체입니다. <br />
                정보공개 운동, 참여예산제 도입, 예산·정책 분석 보고서 발간 등으로 시민의 알 권리와
                자치 역량을 키워 왔습니다. <br />
                “투명한 행정, 책임 있는 권력, 깨어 있는 시민”을 모토로 오늘도 현장에서 발로 뛰며
                변화를 만들어 갑니다. <br />
                정부와 지방자치단체 보조금 0% 로 회원의 회비와 시민의 후원금으로 독립적으로
                운영됩니다.
              </p>
            </FooterSection>

            <FooterSection>
              <h4>도움을 주신 분들</h4>
              <p>이 프로젝트에 함께한 분들: [이름1], [이름2], ...</p>
            </FooterSection>
          </Footer>
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
  height: 100%;
  display: flex;
  margin: 0 auto;
  flex-direction: column;
`;
const TopArea = styled.div`
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  height: 100px;
  border-bottom: 2px solid ${({ theme }) => theme.colors.topLine};
  z-index: 1000;
  }
`;

const MainArea = styled.main`
  position: relative;
  height: calc(100vh - 100px);
  overflow-y: auto;
  overflow-x: hidden;
`;

const OutletWrapper = styled.div`
  flex: 1;
`;

const Footer = styled.footer`
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 32px;
  padding: 20px;
  border-top: 2px solid ${({ theme }) => theme.colors.primary};
  font-size: ${({ theme }) => theme.sizes.small};
  line-height: 1.5;
  flex-wrap: wrap;

  @media ${({ theme }) => theme.device.mobile} {
    flex-direction: column;
    gap: 20px;
  }
`;

const FooterSection = styled.div`
  flex: 1;
  min-width: 250px;

  h4 {
    font-weight: bold;
    margin-bottom: 8px;
    font-size: ${({ theme }) => theme.sizes.medium};
    color: ${({ theme }) => theme.colors.primary};
  }
`;
