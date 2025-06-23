import styled, { useTheme } from 'styled-components';
import SearchBar from '../components/SearchBar';
import Sidebar from '../components/Sidebar';
import MapPanel from '../components/MapPanel';
import { useState, useEffect, useRef } from 'react';

export default function MainPage() {
  const theme = useTheme();
  const [isMobile, setIsMobile] = useState(false);

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
    <MainPageWrapper>
      <ContentSection>
        {isMobile ? (
          <>
            <ListSection>
              <SearchBar />
              <MapSection>
                <MapPanel />
              </MapSection>
              <ListContent>
                <Sidebar />
              </ListContent>
            </ListSection>
          </>
        ) : (
          <>
            <ListSection>
              <SearchBar />
              <ListContent>
                <Sidebar />
              </ListContent>
            </ListSection>
            <MapSection>
              <MapPanel />
            </MapSection>
          </>
        )}
      </ContentSection>
    </MainPageWrapper>
  );
}

const MainPageWrapper = styled.div`
  margin: 0 auto;
  height: 100%;
  padding: 20px 0;
`;

const ContentSection = styled.div`
  display: flex;
  height: 100%;
  flex: 1;
  overflow: hidden;
  @media screen and ${({ theme }) => theme.device.mobile} {
    flex-direction: column;
    height: auto;
  }
`;

const ListSection = styled.div`
  display: flex;
  flex-direction: column;
  width: 50%;
  height: 100%;
  padding: 10px;
  @media ${({ theme }) => theme.device.mobile} {
    width: 100%;
    height: auto;
  }
`;

const ListContent = styled.div`
  flex: 1;
  overflow-y: auto;
  margin-top: 20px;

  @media ${({ theme }) => theme.device.mobile} {
    overflow-y: visible;
  }
`;

const MapSection = styled.div`
  flex: 1;
  height: 100%;
  border-radius: 8px;
  padding: 0 12px;
  @media ${({ theme }) => theme.device.mobile} {
    flex: none;
    height: 400px;
    padding: 0;
    margin-top: 20px;
    overflow: hidden;
  }
`;
