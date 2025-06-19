import styled from 'styled-components';
import logo from '/logo.png';
import logo2 from '/logo2.png';
import kakaoImg from '/kaka.png';
import linkLogo from '/linkLogo.png';
import ddemocracy from '/ddemocracy.png';
import { useRef, useState, useEffect } from 'react';
import { GiHamburgerMenu } from 'react-icons/gi';
import { useNavigate } from 'react-router-dom';
import { useRecoilValue, useResetRecoilState } from 'recoil';
import { userState } from '@/recoil/appState.js';

export default function TopHeader() {
  const navigate = useNavigate();

  const user = useRecoilValue(userState);
  const resetUser = useResetRecoilState(userState);

  const menuRef = useRef(null);
  const hamburgerRef = useRef(null);

  const [activeIndex, setActiveIndex] = useState(0);

  const menus = ['업무추진비 맛집지도', '수의계약'];
  const menuLinks = ['/', 'contract'];

  const [isOpen, setIsOpen] = useState(false);
  const toggleMenu = () => setIsOpen(prev => !prev);

  //카카오 로그인
  const kakaoClientId = 'cea9fa380d4db5159271874966b03bc4';
  const redirectUri = `${window.location.origin}/oauth/kakao/callback`;

  const loginWithKakao = () => {
    const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${kakaoClientId}&redirect_uri=${redirectUri}&response_type=code`;

    window.location.href = kakaoAuthUrl;
    setIsOpen(false);
  };

  const logoutKakao = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    resetUser();
    setIsOpen(false);
  };

  //메뉴바 닫기
  useEffect(() => {
    const handleClickOutside = e => {
      if (
        menuRef.current &&
        !menuRef.current.contains(e.target) &&
        hamburgerRef.current &&
        !hamburgerRef.current.contains(e.target)
      ) {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen]);

  //메뉴바 닫기
  useEffect(() => {
    console.log('user', user);
  }, [user]);

  return (
    <Wrapper>
      <Left
        onClick={() => {
          navigate(menuLinks[0]);
          setActiveIndex(0);
        }}
      >
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
        <LinkGroup>
          <FieldsWrapper>
            <a href="http://www.cham.or.kr/app/main/index" target="_blank" rel="external">
              <img src={linkLogo} alt="링크이동" />
            </a>
            <Divider />
            <a href="http://moni-budget.cham.or.kr" target="_blank" rel="external">
              예산감시 플랫폼
            </a>
            <Divider />
            <a href="https://ddemocracy.stibee.com" target="_blank" rel="external">
              <Ddemocracy src={ddemocracy} alt="링크이동" rel="external" />
            </a>
            <Divider />
            <a href="https://secure.donus.org/djcham/pay/step1" target="_blank" rel="external">
              후원하기
            </a>
          </FieldsWrapper>
        </LinkGroup>
        {user ? (
          <KakaoButton onClick={logoutKakao}>
            {/*<img src={user.thumbnailImageUrl} alt="카카오" />*/}
            <img src={kakaoImg} alt="카카오" />
            로그아웃
          </KakaoButton>
        ) : (
          <KakaoButton onClick={loginWithKakao}>
            <img src={kakaoImg} alt="카카오" />
            로그인
          </KakaoButton>
        )}
      </Right>
      <Hamburger size={24} onClick={toggleMenu} />
      <MobileMenu ref={menuRef} $open={isOpen}>
        {menus.map((menu, i) => (
          <MenuItemMobile
            key={i}
            $active={activeIndex === i}
            onClick={() => {
              setActiveIndex(i);
              navigate(menuLinks[i]);
              setIsOpen(false); // 메뉴 닫기까지 같이
            }}
          >
            {menu}
          </MenuItemMobile>
        ))}
        {user ? (
          <MenuButtonWrapper onClick={logoutKakao}>
            <img src={kakaoImg} alt="카카오" />
            카카오 로그아웃
          </MenuButtonWrapper>
        ) : (
          <MenuButtonWrapper onClick={loginWithKakao}>
            <img src={kakaoImg} alt="카카오" />
            카카오 로그인
          </MenuButtonWrapper>
        )}

        <LinkWrapper>
          <a href="http://www.cham.or.kr/app/main/index" target="_blank" rel="noreferrer">
            <img src={linkLogo} alt="링크이동" />
          </a>
          <a href="http://moni-budget.cham.or.kr" target="_blank" rel="external">
            예산감시 <br /> 플랫폼
          </a>
          <a href="https://ddemocracy.stibee.com" target="_blank" rel="external">
            <Ddemocracy src={ddemocracy} alt="링크이동" rel="external" />
          </a>
          <a href="https://secure.donus.org/djcham/pay/step1" target="_blank" rel="external">
            후원하기
          </a>
        </LinkWrapper>
      </MobileMenu>
    </Wrapper>
  );
}

const Wrapper = styled.div`
  height: 100%;
  padding: 0 20px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;

  img {
    cursor: pointer;
  }

  @media ${({ theme }) => theme.device.mobile} {
    padding: 0 16px;
  }
`;

const Left = styled.div`
  display: flex;
  align-items: center;
`;

const Center = styled.div`
  display: flex;
  align-items: center;
  margin-top: 12px;
  gap: 80px;
  @media ${({ theme }) => theme.device.tablet} {
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
  @media ${({ theme }) => theme.device.tablet} {
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
  margin-left: 26px;
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

  @media ${({ theme }) => theme.device.tablet} {
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

const MenuItemMobile = styled.a`
  padding: 12px 20px;
  color: ${({ $active, theme }) =>
    $active ? 'white !important' : `${theme.colors.primary} !important`};
  background-color: ${({ $active, theme }) => ($active ? theme.colors.primary : 'transparent')};
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

  cursor: pointer;
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
  cursor: pointer;
  img {
    height: 26px;
  }
`;

const LinkWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  font-weight: bold;
  text-decoration: none;

  a {
    border: none;
    font-size: ${({ theme }) => theme.sizes.medium};
  }

  img {
    height: 36px;
  }
`;

const LinkGroup = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: nowrap;
  margin-right: auto;
`;

const FieldsWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
  color: ${({ theme }) => theme.colors.liteGray};
  font-weight: bold;
  padding: 0 20px;
  overflow-x: auto;
  flex-wrap: nowrap;

  a {
    cursor: pointer;
  }
`;

const Divider = styled.div`
  width: 1px;
  height: 36px;
  background: #ddd;
  flex-shrink: 0;
`;

const Ddemocracy = styled.img`
  width: 30px;
`;
