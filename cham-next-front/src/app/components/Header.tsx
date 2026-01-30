'use client';

import styled from 'styled-components';
import { useEffect, useRef, useState } from 'react';
import { CaretDown, CaretUp, X, List } from 'phosphor-react';
import { usePathname, useRouter } from 'next/navigation';

import React from 'react';
import { useFetchMainMenu } from '@/services/menu.service';
import { useMenuStore } from '@/store/menu';
import Link from 'next/link';
import { useUserStore } from '@/store/user';
import { withBasePath } from '@/lib/path';
import { useMediaQuery } from 'react-responsive';
import { SignIn } from 'phosphor-react';

export default function TopHeader() {
  // useFetchMainMenu();
  // const menuData = useMenuStore((state) => state.menu);
  const user = useUserStore((state) => state.user);
  const menuData = [
    {
      name: '특별법',
      subMenu: [
        {
          name: '1-1 특별법',
          link: '/law/special/1-1',
        },
        {
          name: '2-2 특별법',
          link: '/law/special/2-2',
        },
        {
          name: '3-3 특별법',
          link: '/law/special/3-3',
        },
      ],
    },
    {
      name: '조례',
      subMenu: [
        {
          name: '1-1 조례',
          link: '/law/ordinance/1-1',
        },
        {
          name: '2-2 조례',
          link: '/law/ordinance/2-2',
        },
        {
          name: '3-3 조례',
          link: '/law/ordinance/3-3',
        },
      ],
    },
  ];

  const router = useRouter();
  const pathname = usePathname();

  const isDesktop = useMediaQuery({ minWidth: 1281 });

  const [isSubOpen, setIsSubOpen] = useState(false);

  //서브메뉴 높이 측정
  const subMenuRef = useRef<HTMLDivElement>(null);
  const [subMenuHeight, setSubMenuHeight] = useState(0);

  //모바일 메뉴
  const menuRef = useRef<HTMLDivElement>(null);
  const hamburgerRef = useRef<HTMLDivElement>(null);
  const [isOpen, setIsOpen] = useState(false);

  //모바일 서브 메뉴
  const [isMobileSubOpen, setIsMobileSubOpen] = useState<string | null>(null);

  const toggleMenu = () => setIsOpen((prev) => !prev);

  //최초 진입시 데스크탑은 메뉴바 열기
  useEffect(() => {
    setIsOpen(isDesktop);
  }, [isDesktop]);

  //메뉴바 닫기
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (
        menuRef.current &&
        !menuRef.current.contains(e.target as Node) &&
        hamburgerRef.current &&
        !hamburgerRef.current.contains(e.target as Node)
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

  useEffect(() => {
    if (isSubOpen && subMenuRef.current) {
      setSubMenuHeight(subMenuRef.current.offsetHeight + 40);
    }
  }, [isSubOpen]);

  return (
    <Wrapper onMouseLeave={() => setIsSubOpen(false)}>
      <BgSubWrapper $height={subMenuHeight} className={isSubOpen ? 'show' : ''} />
      {!isDesktop && (
        <div ref={hamburgerRef}>
          <div ref={hamburgerRef}>
            <HamburgerButton $open={isOpen} onClick={toggleMenu}>
              <span />
              <span />
              <span />
            </HamburgerButton>
          </div>
        </div>
      )}
      <LogoBox onClick={() => {}}>
        <Link href="/">
          <img
            src={withBasePath('/headerLogo.png')}
            alt="로고"
            onClick={() => {
              setIsSubOpen(false);
            }}
          />
        </Link>
      </LogoBox>
      <Login>
        <SignIn />
        <Link href="/login">로그인</Link>
      </Login>
      <Menu ref={menuRef} $open={isOpen} className={isSubOpen ? 'show' : ''}>
        <MenuTopBox>
          <h5>목차</h5>
        </MenuTopBox>
        <ul>
          {menuData?.map((menu, i) => (
            <React.Fragment key={i}>
              <li
                onClick={() => {
                  if (isMobileSubOpen === menu.name) {
                    setIsMobileSubOpen(null);
                  } else {
                    setIsMobileSubOpen(menu.name);
                  }
                }}
              >
                <a>{menu.name}</a>
                {isMobileSubOpen === menu.name ? <CaretUp /> : <CaretDown />}
              </li>
              {menu.subMenu.map((sub, j) => (
                <AnimatedSubLiWrapper key={j} $visible={isMobileSubOpen === menu.name}>
                  <SubLi
                    $active={pathname === sub.link}
                    onClick={() => {
                      toggleMenu();
                      router.push(sub.link);
                    }}
                  >
                    <a>{sub.name}</a>
                  </SubLi>
                </AnimatedSubLiWrapper>
              ))}
            </React.Fragment>
          ))}
          <SubMainLi
            onClick={() => {
              toggleMenu();
              router.push('/login');
            }}
          >
            {user ? '로그아웃' : '로그인'}
          </SubMainLi>
        </ul>
      </Menu>
    </Wrapper>
  );
}

const Wrapper = styled.div`
  position: relative;
  height: 80px;
  width: 100%;
  max-width: 1600px;
  padding: 0 12px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;

  ul {
    display: flex;
    height: 100%;
    cursor: pointer;
    transition: border 0.6s;
    align-items: center;
  }

  @media ${({ theme }) => theme.device.tablet} {
    max-width: 100%;
    min-width: 100%;
    padding: 0 16px;
  }
`;

const LogoBox = styled.div`
  display: flex;
  align-items: center;
  height: 100%;
  justify-content: flex-start;

  img {
    width: 224px;
    object-fit: cover;
    cursor: pointer;

    @media ${({ theme }) => theme.device.tablet} {
      width: 184px;
    }
  }
`;

const Login = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: ${({ theme }) => theme.desktop.sizes.h5Size};
  color: ${({ theme }) => theme.colors.blackColor};
  font-weight: 600;
  padding: 4px 0;

  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.mobile.sizes.h5Size};
  }

  svg {
    width: 16px;
    height: 16px;

    @media ${({ theme }) => theme.device.mobile} {
      width: 14px;
      height: 14px;
    }
  }
`;

const BgSubWrapper = styled.div<{ $height: number }>`
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  height: ${({ $height }) => `${$height}px`};
  background-color: ${({ theme }) => theme.colors.softColor2};

  opacity: 0;
  transform: translateY(-10px);
  pointer-events: none;
  transition:
    opacity 0.3s ease,
    transform 0.3s ease;

  &.show {
    opacity: 0.96;
    transform: translateY(0);
    pointer-events: auto;
  }
`;

const HamburgerButton = styled.button<{ $open: boolean }>`
  display: block;
  width: 22px;
  height: 16px;
  position: relative;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;

  span {
    position: absolute;
    left: 0;
    width: 100%;
    height: 2px;
    background-color: ${({ theme }) => theme.colors.blackColor};
    border-radius: 2px;
    transition:
      transform 0.35s cubic-bezier(0.4, 0, 0.2, 1),
      opacity 0.25s ease;
  }

  span:nth-child(1) {
    top: 0;
    transform: ${({ $open }) => ($open ? 'translateY(7px) rotate(45deg)' : 'none')};
  }

  span:nth-child(2) {
    top: 7px;
    opacity: ${({ $open }) => ($open ? 0 : 1)};
  }

  span:nth-child(3) {
    top: 14px;
    transform: ${({ $open }) => ($open ? 'translateY(-7px) rotate(-45deg)' : 'none')};
  }
`;

const Menu = styled.div<{ $open: boolean }>`
  position: absolute;
  top: 100%;
  left: 0;
  width: 50%;
  max-width: 300px;
  height: calc(100vh - 80px);
  // border-top: 1px solid ${({ theme }) => theme.colors.lineColor};
  background-color: ${({ theme }) => theme.colors.softColor2};
  transform: ${({ $open }) => ($open ? 'translateX(0)' : 'translateX(-100%)')};
  opacity: ${({ $open }) => ($open ? 0.96 : 0)};
  pointer-events: ${({ $open }) => ($open ? 'auto' : 'none')};
  transition:
    transform 0.3s ease,
    opacity 0.3s ease;

  ul {
    display: flex;
    flex-direction: column;
    width: 100%;
    margin-top: 20px;
    gap: 6px;
    color: ${({ theme }) => theme.colors.blackColor};
    font-size: ${({ theme }) => theme.desktop.sizes.md};
    font-weight: ${({ theme }) => theme.weight.bold};
    padding: 0 20px;

    li {
      width: 100%;
      height: 50px;
      align-items: center;
      display: flex;
      justify-content: left;
      padding: 0 20px;

      img {
        height: 26px;
        margin-right: 8px;
      }

      svg {
        margin-left: auto;
      }
    }
  }
`;

const MenuTopBox = styled.div`
  background-color: ${({ theme }) => theme.colors.blueColor};
  padding: 24px 8px;
  text-align: center;

  h5 {
    font-size: ${({ theme }) => theme.desktop.sizes.h5Size} !important;
    font-weight: 600;
    color: ${({ theme }) => theme.colors.whiteColor};

    @media ${({ theme }) => theme.device.mobile} {
      font-size: ${({ theme }) => theme.mobile.sizes.h5Size};
    }
  }
`;

const SubLi = styled.li<{ $active: boolean }>`
  font-size: ${({ theme }) => theme.desktop.sizes.sm};
  font-weight: ${({ theme }) => theme.weight.semiBold};
  background-color: ${({ $active, theme }) =>
    $active ? theme.colors.blackColor : theme.colors.softColor2};
  color: ${({ $active, theme }) => ($active ? theme.colors.white : theme.colors.blackColor)};
`;

const AnimatedSubLiWrapper = styled.div<{ $visible: boolean }>`
  width: 100%;
  overflow: hidden;
  max-height: ${({ $visible }) => ($visible ? '60px' : '0')};
  opacity: ${({ $visible }) => ($visible ? 0.96 : 0)};
  transform: translateY(${({ $visible }) => ($visible ? '0' : '-10px')});
  transition:
    max-height 0.3s ease,
    opacity 0.3s ease,
    transform 0.3s ease;
`;

const SubMainLi = styled.li`
  justify-content: center !important;
  svg {
    margin-left: 0 !important;
    margin-right: 8px;
  }
`;
