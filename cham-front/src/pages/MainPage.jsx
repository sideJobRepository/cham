import styled, { useTheme } from 'styled-components';
import SearchBar from '../components/SearchBar';
import Sidebar from '../components/Sidebar';
import MapPanel from '../components/MapPanel';
import { useState, useEffect } from 'react';
import { FiChevronDown, FiChevronUp } from 'react-icons/fi';

export default function MainPage() {
  const theme = useTheme();
  const [isMobile, setIsMobile] = useState(false);
  const [isSheetOpen, setIsSheetOpen] = useState(false); // ← 바텀시트 열림/닫힘

  useEffect(() => {
    const checkMobile = () => {
      const result = window.matchMedia(theme.device.mobile).matches;
      setIsMobile(result);
    };

    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, [theme.device.mobile]);

  return (
    <MainPageWrapper>
      <ContentSection>
        {isMobile ? (
          <>
            <SearchBar />

            <ListSection>
              <MapSection>
                <MapPanel />
                {!isSheetOpen && (
                  <SlideButton onClick={() => setIsSheetOpen(true)} aria-label="리스트 열기">
                    <FiChevronUp />
                  </SlideButton>
                )}

                {isSheetOpen && <Backdrop onClick={() => setIsSheetOpen(false)} />}

                <BottomSheet $open={isSheetOpen} role="dialog" aria-modal="true">
                  <SheetHeader>
                    <SheetTitle>검색 결과</SheetTitle>
                    <CloseBtn onClick={() => setIsSheetOpen(false)} aria-label="리스트 닫기">
                      <FiChevronDown />
                    </CloseBtn>
                  </SheetHeader>

                  <SheetBody>
                    <Sidebar />
                  </SheetBody>
                </BottomSheet>
              </MapSection>
            </ListSection>
          </>
        ) : (
          <>
            <SearchBar />
            <ListSection>
              <ListContent>
                <Sidebar />
              </ListContent>
              <MapSection>
                <MapPanel />
              </MapSection>
            </ListSection>
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
  max-width: 1500px;
  min-width: 1023px;
  min-height: 600px;
  align-items: center;

  @media ${({ theme }) => theme.device.mobile} {
    max-width: 100%;
    min-width: 100%;
    min-height: unset;
  }
`;

const ContentSection = styled.div`
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 10px;
  flex: 1;
  min-height: 0;
  overflow: hidden;

  @media screen and ${({ theme }) => theme.device.mobile} {
    flex-direction: column;
    height: auto;
  }
`;

const ListSection = styled.div`
  display: flex;
  width: 100%;
  height: 100%;
  min-height: 0;
  margin-top: 10px;
  border-top: 1px solid ${({ theme }) => theme.colors.border};

  @media ${({ theme }) => theme.device.mobile} {
    width: 100%;
    height: auto;
  }
`;

const ListContent = styled.div`
  width: 20%;
  height: 100%;
  padding-right: 10px;
  overflow-y: auto;
   border-bottom: 1px solid ${({ theme }) => theme.colors.border};

  @media ${({ theme }) => theme.device.mobile} {
    overflow-y: visible;
  }
`;

const MapSection = styled.div`
  width: 80%;
  height: 100%;
  position: relative;
  @media ${({ theme }) => theme.device.mobile} {
    flex: none;
    width: 100%;
    height: calc(100vh - 258px);
  }
`;

/* 바텀시트가 열려있을 때 뒤 배경 클릭으로 닫기 */
const Backdrop = styled.div`
  position: absolute;
  inset: 0;
  background: rgba(10, 10, 10, 0.25);
  z-index: 30;
`;

/* 하단에서 올라오는 패널 */
const BottomSheet = styled.div`
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 90%;
  max-height: 80vh;
  background: #fff;
  border-top-left-radius: 16px;
  border-top-right-radius: 16px;
  box-shadow: 0 -8px 30px rgba(0, 0, 0, 0.2);

  transform: ${({ $open }) => ($open ? 'translateY(0%)' : 'translateY(150%)')};
  transition: transform 260ms ease;
  z-index: 40;

  display: flex;
  flex-direction: column;
`;

const SheetHeader = styled.div`
  position: relative;
  padding: 10px 44px 6px;
  border-bottom: 1px solid #eee;
`;

const SheetTitle = styled.h3`
  margin: 0;
  font-size: 14px;
  text-align: center;
  color: #333;
`;

const CloseBtn = styled.button`
  width: 36px;
  height: 36px;
  margin: 0 auto;
  display: grid;
  place-items: center;
  border: none;
  background: transparent;
  cursor: pointer;

  svg {
    width: 30px;
    height: 30px;
    color: ${({ theme }) => theme.colors.primary};
  }
`;

const SheetBody = styled.div`
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 10px 12px;
`;

/* 닫혀있을 때 하단 중앙 열기 버튼 */
const SlideButton = styled.button`
  position: absolute;
  left: 50%;
  bottom: 10px;
  transform: translateX(-50%);
  z-index: 20;

  width: 44px;
  height: 44px;
  border-radius: 999px;
  border: none;
  background: #fff;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.15);
  display: grid;
  place-items: center;
  cursor: pointer;

  svg {
    width: 26px;
    height: 26px;
    color: ${({ theme }) => theme.colors.primary};
  }
`;
