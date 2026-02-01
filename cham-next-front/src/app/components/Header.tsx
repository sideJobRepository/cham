'use client';

import styled from 'styled-components';
import { useEffect, useRef, useState } from 'react';
import { CaretDown, CaretUp, X, List } from 'phosphor-react';
import { usePathname, useRouter } from 'next/navigation';

import React from 'react';
import { useFetchMainMenu } from '@/services/menu.service';
import { Article, useMenuStore } from '@/store/menu';
import Link from 'next/link';
import { useUserStore } from '@/store/user';
import { withBasePath } from '@/lib/path';
import { useMediaQuery } from 'react-responsive';
import { SignIn } from 'phosphor-react';
import { useArticleStore } from '@/store/aricle';
import { tokenStore } from '@/services/tokenStore';
import api from '@/lib/axiosInstance';
import { useDialogUtil } from '@/utils/dialog';

export default function TopHeader() {
  const [mounted, setMounted] = useState(false);
  const { alert, confirm } = useDialogUtil();

  useEffect(() => {
    setMounted(true);
  }, []);

  useFetchMainMenu();
  const menuData = useMenuStore((state) => state.menu);
  const [initialized, setInitialized] = useState(false);

  const setArticlesData = useArticleStore((state) => state.setArticles);

  const [activeLegislation, setActiveLegislation] = useState<number>(0);
  const [openChapter, setOpenChapter] = useState<string | null>(null);
  const [openPart, setOpenPart] = useState<string | null>(null);
  const [openSection, setOpenSection] = useState<string | null>(null);
  const [activeArticleId, setActiveArticleId] = useState<number | null>(null);

  const currentLaw = menuData?.legislations[activeLegislation];

  const user = useUserStore((state) => state.user);
  const resetUser = useUserStore((state) => state.clearUser);

  const router = useRouter();
  const pathname = usePathname();

  const isDesktopQuery = useMediaQuery({ minWidth: 1281 });

  const isDesktop = mounted ? isDesktopQuery : false;

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

  const logout = async () => {
    const ok = await confirm('정말 로그아웃하시겠습니까?');

    if (!ok) return;
    const channel = new BroadcastChannel('auth');
    channel.postMessage('logout');
    channel.close();

    try {
      await api.delete('/cham/refresh', { withCredentials: true });
    } catch (err) {
      console.error('서버 리프레시 토큰 삭제 실패:', err);
    }

    alert('로그아웃 되었습니다.');

    tokenStore.clear();
    resetUser();
  };

  const uniqById = (list: Article[]) => {
    const map = new Map<number, Article>();
    for (const a of list) map.set(a.articleId, a);
    return [...map.values()];
  };

  const collectPartArticles = (part: any) => {
    const list =
      part.chapters?.flatMap((ch: any) =>
        (ch.sections ?? []).flatMap((sec: any) => sec.articles ?? [])
      ) ?? [];
    return uniqById(list);
  };

  const collectChapterArticles = (chapterObj: any) => {
    const list = (chapterObj.sections ?? []).flatMap((sec: any) => sec.articles ?? []);
    return uniqById(list);
  };

  const articleClick = (data: Article) => {
    if (!data) return;

    setActiveArticleId(data.articleId);
    setArticlesData([data]);

    if (!isDesktop) toggleMenu();
  };
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

  useEffect(() => {
    if (!menuData || initialized) return;

    const firstLaw = menuData.legislations?.[0];
    if (!firstLaw) return;

    const firstPart = firstLaw.parts?.[0];
    if (!firstPart) return;

    // PART 열기
    setActiveLegislation(0);
    setOpenPart(firstPart.part);
    setOpenChapter(null);
    setOpenSection(null);

    // 해당 PART 하위 article들 자동 세팅
    const articles = collectPartArticles(firstPart);
    setArticlesData(articles);

    setInitialized(true);
  }, [menuData, initialized]);

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
      <Link href="/">
        <LogoBox>
          <img
            src={withBasePath('/headerLogo.png')}
            alt="로고"
            onClick={() => {
              setIsSubOpen(false);
            }}
          />
        </LogoBox>
      </Link>
      <Login>
        {user ? <a onClick={() => logout()}>로그아웃</a> : <Link href="/login">로그인</Link>}
      </Login>
      <Menu ref={menuRef} $open={isOpen} className={isSubOpen ? 'show' : ''}>
        <MenuTopBox>
          {menuData?.legislations.map((law, idx) => (
            <TopTab
              key={law.id}
              $active={activeLegislation === idx}
              onClick={() => {
                setActiveLegislation(idx);
                setOpenPart(null);
                setOpenSection(null);
              }}
            >
              {law.title}
            </TopTab>
          ))}
        </MenuTopBox>
        <ul>
          {currentLaw?.parts.map((part) => (
            <React.Fragment key={part.part}>
              {/* PART */}
              <li
                className="part-item"
                data-open={openPart === part.part}
                onClick={() => {
                  const nextOpen = openPart === part.part ? null : part.part;

                  setOpenPart(nextOpen);
                  setOpenChapter(null);
                  setOpenSection(null);

                  if (nextOpen) {
                    setArticlesData(collectPartArticles(part)); // PART 하위 전부 배열 저장
                  }
                }}
              >
                <a>{part.part}</a>
                {openPart === part.part ? <CaretUp weight="bold" /> : <CaretDown weight="bold" />}
              </li>

              {openPart === part.part &&
                part.chapters?.map((chapterObj, cIdx) => {
                  const chapterKey = chapterObj.chapter ?? `chapter-${cIdx}`;

                  const sectionGroups = (chapterObj.sections ?? []).filter(
                    (s) => s.section && s.section.trim()
                  );

                  const articleOnlyGroups = (chapterObj.sections ?? []).filter(
                    (s) => !s.section || !s.section.trim()
                  );

                  return (
                    <PartBlock key={chapterKey}>
                      {/* CHAPTER (null 이면 제목 생략) */}
                      {chapterObj.chapter && (
                        <ChapterLi
                          data-open={openChapter === chapterKey}
                          onClick={() => {
                            const nextOpen = openChapter === chapterKey ? null : chapterKey;

                            setOpenChapter(nextOpen);
                            setOpenSection(null);

                            if (nextOpen) {
                              setArticlesData(collectChapterArticles(chapterObj)); // chapter 하위 배열 저장
                            }
                          }}
                        >
                          <a>{chapterObj.chapter}</a>
                          {openChapter === chapterKey ? <CaretUp /> : <CaretDown />}
                        </ChapterLi>
                      )}

                      {(openChapter === chapterKey || !chapterObj.chapter) && (
                        <>
                          {/* SECTION */}
                          {sectionGroups.map((section) => (
                            <React.Fragment key={section.section}>
                              <SubLi
                                $open={openSection === section.section}
                                onClick={() => {
                                  const nextOpen =
                                    openSection === section.section ? null : section.section;
                                  setOpenSection(nextOpen);

                                  if (nextOpen) {
                                    setArticlesData(section.articles ?? []); // section articles 배열 저장
                                  }
                                }}
                              >
                                <a>{section.section}</a>
                                {openSection === section.section ? <CaretUp /> : <CaretDown />}
                              </SubLi>

                              {openSection === section.section &&
                                section.articles.map((article) => (
                                  <ArticleLi
                                    key={article.articleId}
                                    $active={activeArticleId === article.articleId}
                                    onClick={() => articleClick(article)}
                                  >
                                    <span>
                                      {article.articleNo} {article.articleTitle}
                                    </span>
                                  </ArticleLi>
                                ))}
                            </React.Fragment>
                          ))}

                          {/* SECTION 없는 article */}
                          {articleOnlyGroups
                            .flatMap((s) => s.articles ?? [])
                            .map((article) => (
                              <ArticleLi
                                key={article.articleId}
                                $active={activeArticleId === article.articleId}
                                onClick={() => articleClick(article)}
                              >
                                <span>
                                  {article.articleNo} {article.articleTitle}
                                </span>
                              </ArticleLi>
                            ))}
                        </>
                      )}
                    </PartBlock>
                  );
                })}
            </React.Fragment>
          ))}
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
      width: 200px;
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

  a {
    cursor: pointer;
  }

  @media ${({ theme }) => theme.device.tablet} {
    font-size: ${({ theme }) => theme.mobile.sizes.h5Size};
  }

  svg {
    width: 16px;
    height: 16px;

    @media ${({ theme }) => theme.device.mobile} {
      width: 12px;
      height: 12px;
    }
  }
`;

const BgSubWrapper = styled.div<{ $height: number }>`
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  height: ${({ $height }) => `${$height}px`};
  background-color: ${({ theme }) => theme.colors.softColor};

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
  display: flex;
  flex-direction: column;
  position: absolute;
  overflow: auto;
  top: 100%;
  left: 0;
  width: 50%;
  max-width: 300px;
  height: calc(100vh - 80px);
  // border-top: 1px solid ${({ theme }) => theme.colors.lineColor};
  background-color: ${({ theme }) => theme.colors.softColor};
  transform: ${({ $open }) => ($open ? 'translateX(0)' : 'translateX(-100%)')};
  opacity: ${({ $open }) => ($open ? 0.96 : 0)};
  pointer-events: ${({ $open }) => ($open ? 'auto' : 'none')};
  transition:
    transform 0.3s ease,
    opacity 0.3s ease;

  //첫 계층
  ul li[data-open='true'] {
    background-color: #e8eeff;
    color: #1e3a8a;
  }

  .part-item[data-open='true'] {
    color: #1e3a8a;
    background-color: transparent;
    font-weight: 800;
  }

  ul {
    display: flex;
    flex-direction: column;
    width: 100%;
    margin-top: 20px;
    color: ${({ theme }) => theme.colors.blackColor};
    font-size: ${({ theme }) => theme.desktop.sizes.tree};
    font-weight: 600;
    padding: 0 12px;

    @media ${({ theme }) => theme.device.tablet} {
      font-size: ${({ theme }) => theme.mobile.sizes.tree};
    }

    li {
      width: 100%;
      //min-height: 50px;
      padding: 12px 8px;
      align-items: center;
      display: flex;
      gap: 8px;
      justify-content: space-between;

      a {
        min-width: 80%;
        word-break: keep-all;
        overflow-wrap: break-word;
        white-space: normal;
      }

      svg {
        min-width: 20%;
      }
    }
  }
`;

const MenuTopBox = styled.div`
  display: flex;
  background-color: ${({ theme }) => theme.colors.whiteColor};
  text-align: center;
`;

const TopTab = styled.div<{ $active: boolean }>`
  flex: 1;
  text-align: center;
  padding: 12px 0;
  cursor: pointer;
  font-weight: 700;
  background-color: ${({ $active, theme }) =>
    $active ? theme.colors.blueColor : theme.colors.softColor2};
  color: ${({ $active, theme }) => ($active ? theme.colors.whiteColor : theme.colors.blackColor)};
  font-size: ${({ theme }) => theme.desktop.sizes.md};
  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.mobile.sizes.md};
  }
`;

const ChapterLi = styled.li`
  font-weight: 600;
  a {
    padding-left: 8px;
  }

  font-size: ${({ theme }) => theme.desktop.sizes.tree2};

  @media ${({ theme }) => theme.device.tablet} {
    font-size: ${({ theme }) => theme.mobile.sizes.tree2};
  }
`;

const SubLi = styled.li<{ $open?: boolean }>`
  color: ${({ $open, theme }) => ($open ? '#1E40AF' : '000000')};
  font-size: ${({ theme }) => theme.desktop.sizes.tree3};
  font-weight: 600;
  @media ${({ theme }) => theme.device.tablet} {
    font-size: ${({ theme }) => theme.mobile.sizes.tree3};
  }

  a {
    padding-left: 12px;
  }
`;

const ArticleLi = styled.li<{ $active?: boolean }>`
  font-weight: ${({ $active }) => ($active ? 600 : 400)};
  cursor: pointer;
  font-size: ${({ theme }) => theme.desktop.sizes.tree4};
  // background-color: ${({ $active, theme }) => ($active ? '#eef7fb' : 'transparent')};
  color: ${({ $active }) => ($active ? '#344b87' : 'inherit')};

  @media ${({ theme }) => theme.device.tablet} {
    font-size: ${({ theme }) => theme.mobile.sizes.tree4};
  }

  span {
    padding-left: 16px;
  }

  &:hover {
    background-color: ${({ theme }) => theme.colors.border};
  }
`;

const PartBlock = styled.div`
  background-color: #f3f6ff; // PART 전체 영역 배경
  width: 100%;
  border-radius: 8px;
  margin: 8px 0;
`;
