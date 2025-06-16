import styled from 'styled-components';
import logo from '/logo.png';
import logo2 from '/logo2.png';
import kakaoImg from '/kaka.png';
import linkLogo from '/linkLogo.png';
import { useState } from 'react';
import { GiHamburgerMenu } from 'react-icons/gi';
import { useNavigate } from 'react-router-dom';

export default function TopHeader() {
  const navigate = useNavigate();

  const [activeIndex, setActiveIndex] = useState(0);
  const menus = ['업무추진비 맛집지도', '수의계약'];
  const menuLinks = ['/', 'contract'];

  const [isOpen, setIsOpen] = useState(false);
  const toggleMenu = () => setIsOpen(prev => !prev);

  return (
    <Wrapper>
      <Left>
        <MeerkatLogo src={logo} alt="로고" />
        <img src={logo2} alt="로고" />
      </Left>

      <Center>
        {menus.map((menu, i) => (
          <MenuItem
            key={i}
            $active={activeIndex === i}
            onClick={() => {
              setActiveIndex(i);
              navigate(menuLinks[i]);
            }}
          >
            {menu}
          </MenuItem>
        ))}
      </Center>

      <Right>
        <KakaoButton>
          <img src={kakaoImg} alt="카카오" />
          카카오 로그인
        </KakaoButton>
        <a href="http://www.cham.or.kr/app/main/index" target="_blank" rel="noopener noreferrer">
          <img src={linkLogo} alt="링크이동" />
        </a>
      </Right>

      <Hamburger size={24} onClick={toggleMenu} />

      <MobileMenu $open={isOpen}>
        <a href="#">업무추진비 맛집지도</a>
        <a href="#">수의계약</a>
        <MenuButtonWrapper>
          <img src={kakaoImg} alt="카카오" />
          카카오 로그인
        </MenuButtonWrapper>

        <LinkWrapper href="http://www.cham.or.kr/app/main/index" target="_blank" rel="noreferrer">
          <img src={linkLogo} alt="링크이동" />
          <span>바로가기</span>
        </LinkWrapper>
      </MobileMenu>
    </Wrapper>
  );
}

const Wrapper = styled.div`
  height: 100%;
  max-width: 1500px;
  min-width: 1024px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  img {
    cursor: pointer;
  }

  @media ${({ theme }) => theme.device.mobile} {
    max-width: 100%;
    min-width: 100%;
    padding: 0 16px;
  }
`;

const Left = styled.div`
  display: flex;
  align-items: center;
`;

const Center = styled.div`
  display: flex;
  gap: 80px;
  @media ${({ theme }) => theme.device.mobile} {
    display: none;
  }
`;

const MenuItem = styled.div`
  font-size: ${({ theme }) => theme.sizes.menu};
  font-weight: bold;
  padding-bottom: 6px;
  cursor: pointer;
  color: ${({ theme }) => theme.colors.primary};
  border-bottom: 2px solid
    ${({ $active, theme }) => ($active ? theme.colors.primary : 'transparent')};
  transition: border 0.6s;
`;

const MeerkatLogo = styled.img`
  height: 100px;
  width: auto;
`;

const Right = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
  @media ${({ theme }) => theme.device.mobile} {
    display: none;
  }
`;

const KakaoButton = styled.button`
  display: flex;
  align-items: center;
  background: ${({ theme }) => theme.colors.kakao};
  border: none;
  border-radius: 50px;
  padding: 8px 16px 8px 8px;
  margin-right: 12px;
  font-size: ${({ theme }) => theme.sizes.large};
  font-weight: bold;
  cursor: pointer;
  color: black;

  img {
    height: 26px;
    margin-right: 4px;
  }
`;

const Hamburger = styled(GiHamburgerMenu)`
  display: none;
  cursor: pointer;

  @media ${({ theme }) => theme.device.mobile} {
    color: ${({ theme }) => theme.colors.primary};
    display: block;
  }
`;

const MobileMenu = styled.div`
  overflow: hidden;
  max-height: ${({ $open }) => ($open ? '500px' : '0')};
  opacity: ${({ $open }) => ($open ? 1 : 0)};
  pointer-events: ${({ $open }) => ($open ? 'auto' : 'none')};
  transition:
    max-height 0.4s ease-in-out,
    opacity 0.4s ease-in-out;
  transform: ${({ $open }) => ($open ? 'translateY(0)' : 'translateY(-10px)')};
  transition:
    max-height 0.4s ease-in-out,
    opacity 0.4s ease-in-out,
    transform 0.4s ease-in-out;

  background: white;
  position: absolute;
  top: 90px;
  right: 0;
  width: 100%;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;

  a,
  div {
    padding: 12px 20px;
    color: ${({ theme }) => theme.colors.primary};
    font-size: ${({ theme }) => theme.sizes.menu};
    font-weight: bold;
    text-decoration: none;
    border-bottom: 1px solid #eee;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;

    &:last-child {
      border-bottom: none;
    }
  }

  img {
    height: 26px;
  }
`;

const MenuButtonWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 0;
  gap: 8px;
  border-bottom: 1px solid #eee;
  color: ${({ theme }) => theme.colors.primary};
  font-size: ${({ theme }) => theme.sizes.menu};
  font-weight: bold;
  text-decoration: none;
  img {
    height: 26px;
  }
`;

const LinkWrapper = styled.a`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 0;
  color: ${({ theme }) => theme.colors.primary};
  font-size: ${({ theme }) => theme.sizes.menu};
  font-weight: bold;
  text-decoration: none;

  img {
    height: 26px;
  }
`;
