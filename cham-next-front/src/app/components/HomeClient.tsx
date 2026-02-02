'use client';

import { Wrapper } from '@/styles/Wrapper.styles';
import { motion } from 'framer-motion';
import styled, { keyframes } from 'styled-components';
import { ArrowRight, ThumbsUp, WarningCircle, ThumbsDown } from 'phosphor-react';
import React, { useEffect, useRef, useState } from 'react';
import { useArticleStore } from '@/store/aricle';
import { useCommentStore } from '@/store/comment';
import { useFetchCommentList } from '@/services/comment.service';
import { useInsertPost, useUpdatePost } from '@/services/main.service';
import { useUserStore } from '@/store/user';
import { useRouter } from 'next/navigation';
import { useDialogUtil } from '@/utils/dialog';
import { OpinionData, useOpinionStore } from '@/store/opinion';
import { useSearchDataStore } from '@/store/search';
import type { Article } from '@/store/menu';
import { useLoadingStore } from '@/store/loading';

type HomeClientProps = {
  initialArticles: Article[];
};

export default function HomeClient({ initialArticles }: HomeClientProps) {
  const { insert } = useInsertPost();
  const { update } = useUpdatePost();

  const router = useRouter();

  const { alert, confirm } = useDialogUtil();

  //유저정보
  const user = useUserStore((state) => state.user);

  const storedArticles = useArticleStore((state) => state.articles);
  const setArticles = useArticleStore((state) => state.setArticles);
  const mainData = storedArticles.length ? storedArticles : initialArticles;

  console.log('mainData--- 확인', mainData);
  const setCommentOpen = useCommentStore((state) => state.setOpen);

  const fetchCommentList = useFetchCommentList();

  const keyword = useSearchDataStore((state) => state.keyword);
  const loading = useLoadingStore((state) => state.loading);
  const hasRequestedInitialOpinion = useRef(false);
  const hasObservedInitialLoading = useRef(false);
  const [heroReady, setHeroReady] = useState(false);
  const [showInitialBlockingLoader, setShowInitialBlockingLoader] = useState(true);

  const opinionData = useOpinionStore((state) => state.opinion);

  const getOpinion = (articleId: number) => opinionData?.find((o) => o.articleId === articleId);

  const openComment = (id: number) => {
    fetchCommentList(id);
    setCommentOpen(true);
  };

  const fetchOpinion = () => {
    if (!mainData.length) return;

    insert({
      url: '/cham/great/greats',
      body: {
        articleIds: mainData.map((a) => a.articleId),
      },
      ignoreErrorRedirect: true,
      onSuccess: (res) => {
        useOpinionStore.getState().setOpinion(res as unknown as OpinionData[]);
      },
    });
  };

  const handleSubmit = async (articleId: number, greatType: string) => {
    if (!user) {
      const ok = await confirm('로그인 후 이용 가능합니다.', '로그인 페이지로 이동하시겠습니까?');
      if (!ok) return;
      router.push('/login');
      return;
    }

    insert({
      url: '/cham/great',
      body: {
        articleId,
        greatType,
      },
      ignoreErrorRedirect: true,
      disableLoading: true,
      onSuccess: () => {
        fetchOpinion();
      },
    });
  };

  const handleUpdate = async (greatId: number, greatType: string) => {
    if (!user) {
      const ok = await confirm('로그인 후 이용 가능합니다.', '로그인 페이지로 이동하시겠습니까?');
      if (!ok) return;
      router.push('/login');
      return;
    }

    update({
      url: '/cham/great',
      body: {
        greatId,
        greatType,
      },
      ignoreErrorRedirect: true,
      disableLoading: true,
      onSuccess: () => {
        fetchOpinion();
      },
    });
  };

  const highlightKeyword = (text: string, keyword: string) => {
    if (!keyword) return text;

    const escaped = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'); // ?뺢퇋???덉쟾
    const regex = new RegExp(`(${escaped})`, 'gi');

    return text
      .split(regex)
      .map((part, idx) =>
        part.toLowerCase() === keyword.toLowerCase() ? <mark key={idx}>{part}</mark> : part
      );
  };

  useEffect(() => {
    if (storedArticles.length === 0 && initialArticles.length > 0) {
      setArticles(initialArticles);
    }
  }, [storedArticles.length, initialArticles, setArticles]);

  useEffect(() => {
    if (mainData.length === 0) return;
    setCommentOpen(false);

    if (!hasRequestedInitialOpinion.current) {
      hasRequestedInitialOpinion.current = true;
      fetchOpinion();
      return;
    }

    fetchOpinion();

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  }, [mainData]);

  useEffect(() => {
    if (mainData.length === 0) {
      setShowInitialBlockingLoader(false);
    }
  }, [mainData.length]);

  useEffect(() => {
    if (!hasRequestedInitialOpinion.current) return;
    if (loading) {
      hasObservedInitialLoading.current = true;
      return;
    }

    if (!heroReady && hasObservedInitialLoading.current) {
      setHeroReady(true);
      setShowInitialBlockingLoader(false);
    }
  }, [loading, heroReady]);

  return (
    <Wrapper>
      {showInitialBlockingLoader && (
        <InitialLoaderOverlay>
          <InitialLoaderSpinner />
        </InitialLoaderOverlay>
      )}
      <Hero>
        <HeroFillOverlay
          initial={{ scaleX: 0 }}
          animate={{ scaleX: heroReady ? 1 : 0 }}
          transition={{
            duration: 1.1,
            ease: [0.4, 0, 0.2, 1],
          }}
        />
        <motion.div
          style={{ position: 'relative', zIndex: 1 }}
          initial={{ opacity: 0 }}
          animate={{ opacity: heroReady ? 1 : 0 }}
          transition={{
            duration: 1.2,
            ease: [0.4, 0, 0.2, 1],
            delay: 0.1,
          }}
        >
          <HeroContent>
            <h2>충남대전통합 특별법(안) 공개 시민의 의견을 말해주세요.</h2>
            <h4>
              공개된 여당의 광역행정통합 법안을 직접 확인하고,
              <br />각 조항별로 의견을 더해주세요.
              <br />
              여러분이 남긴 의견을 모아 전달하겠습니다.
            </h4>
          </HeroContent>
        </motion.div>
      </Hero>
      {mainData.length > 0 && (
        <ArticleSection>
          {mainData.map((article) => {
            const opinion = getOpinion(article.articleId);
            const selected = opinion?.selectedType;

            return (
              <ArticleItem key={article.articleId}>
                <ArticleTop>
                  <ArticleNo>{article.articleNo}</ArticleNo>
                  <Button
                    $bg="#093A6E"
                    $color="#fff"
                    onClick={() => openComment(article.articleId)}
                  >
                    <span>의견 쓰기</span>
                    <ArrowRight weight="bold" />
                  </Button>
                </ArticleTop>
                <ArticleTitle>{article.articleTitle}</ArticleTitle>
                <ArticleContent> {highlightKeyword(article.content, keyword)}</ArticleContent>
                <EditBox>
                  <EditButton
                    $active={selected === 'SUPPORT' && user !== null}
                    onClick={() => {
                      if (opinion?.greatId) {
                        handleUpdate(opinion.greatId, 'SUPPORT');
                      } else {
                        handleSubmit(article.articleId, 'SUPPORT');
                      }
                    }}
                  >
                    <span>찬성해요</span>

                    <span>
                      <ThumbsUp weight="bold" />
                      {opinion?.supportCount ?? 0}
                    </span>
                  </EditButton>

                  <EditButton
                    $active={selected === 'CONCERN' && user !== null}
                    onClick={() => {
                      if (opinion?.greatId) {
                        handleUpdate(opinion.greatId, 'CONCERN');
                      } else {
                        handleSubmit(article.articleId, 'CONCERN');
                      }
                    }}
                  >
                    <span>우려해요</span>
                    <span>
                      <WarningCircle weight="bold" />
                      {opinion?.concernCount ?? 0}
                    </span>
                  </EditButton>
                  <EditButton
                    $active={selected === 'OPPOSITION' && user !== null}
                    onClick={() => {
                      if (opinion?.greatId) {
                        handleUpdate(opinion.greatId, 'OPPOSITION');
                      } else {
                        handleSubmit(article.articleId, 'OPPOSITION');
                      }
                    }}
                  >
                    <span>반대해요</span>
                    <span>
                      <ThumbsDown weight="bold" />
                      {opinion?.oppositionCount ?? 0}
                    </span>
                  </EditButton>
                </EditBox>
              </ArticleItem>
            );
          })}
        </ArticleSection>
      )}
    </Wrapper>
  );
}

const Hero = styled.section`
  width: 100%;
  padding: 48px 12px;
  background: #ffffff;
  position: relative;
  display: flex;
  justify-content: center;

  @media ${({ theme }) => theme.device.mobile} {
    padding: 36px 12px;
  }
`;

const HeroFillOverlay = styled(motion.div)`
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1f3a5f 0%, #2f4f6f 40%, #4b6b7a 100%);
  transform-origin: left center;
  pointer-events: none;
`;

const HeroContent = styled.div`
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  text-align: center;

  h2 {
    display: flex;
    gap: 4px;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    font-size: ${({ theme }) => theme.desktop.sizes.h2Size};
    font-weight: 800;
    letter-spacing: 0.1rem;
    color: ${({ theme }) => theme.colors.whiteColor};
    word-break: keep-all;
    overflow-wrap: break-word;

    @media ${({ theme }) => theme.device.mobile} {
      font-size: ${({ theme }) => theme.mobile.sizes.h3Size};
      img {
        width: 240px;
      }
    }
  }

  h4 {
    font-size: ${({ theme }) => theme.desktop.sizes.h4Size};
    font-weight: 600;
    color: ${({ theme }) => theme.colors.softColor};
    letter-spacing: 0.06rem;
    word-break: keep-all;
    overflow-wrap: break-word;

    @media ${({ theme }) => theme.device.mobile} {
      font-size: ${({ theme }) => theme.mobile.sizes.h4Size};
    }
  }
`;

const Button = styled.button<{ $bg: string; $color: string }>`
  background: ${({ $bg }) => $bg};
  color: ${({ $color }) => $color};
  border: none;
  padding: 10px 12px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  font-weight: 500;
  font-size: ${({ theme }) => theme.desktop.sizes.sm};
  box-shadow: 2px 4px 2px rgba(0, 0, 0, 0.2);

  span {
    display: flex;
    align-items: center;
    line-height: 1;
  }

  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.mobile.sizes.sm};
  }
`;

const ArticleSection = styled.section`
  width: 100%;
  margin: 64px auto 32px;
  padding: 0 32px;
  display: flex;
  flex-direction: column;
  gap: 32px;

  @media ${({ theme }) => theme.device.mobile} {
    padding: 0 16px;
  }
`;

const ArticleItem = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 32px;
  border-bottom: 1px solid ${({ theme }) => theme.colors.lineColor};
`;

const ArticleTop = styled.article`
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
`;

const ArticleNo = styled.div`
  font-size: ${({ theme }) => theme.desktop.sizes.xl};
  font-weight: 800;
  color: #1e3a8a;
  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.mobile.sizes.xl};
  }
`;

const ArticleTitle = styled.h3`
  font-size: ${({ theme }) => theme.desktop.sizes.h5Size};
  font-weight: 800;
  color: ${({ theme }) => theme.colors.blackColor};

  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.mobile.sizes.h5Size};
  }
`;

const ArticleContent = styled.div`
  font-size: ${({ theme }) => theme.desktop.sizes.md};
  line-height: 1.7;
  color: ${({ theme }) => theme.colors.inputColor};
  word-break: keep-all;
  overflow-wrap: break-word;
  white-space: normal;

  mark {
    background-color: #fff3a0;
    padding: 0 8px;
    font-weight: 600;
  }

  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.mobile.sizes.md};
  }
`;

const EditBox = styled.div`
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 24px;
  flex: 1;
`;

const EditButton = styled.button<{ $active?: boolean }>`
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background-color: ${({ $active }) => ($active ? '#428bfb' : 'transparent')};
  color: ${({ $active, theme }) => ($active ? theme.colors.whiteColor : theme.colors.inputColor)};
  font-size: ${({ theme }) => theme.desktop.sizes.sm};
  font-weight: 500;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
  box-shadow: 1px 2px 2px 2px rgba(0, 0, 0, 0.2);
  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.mobile.sizes.sm};
  }

  span {
    display: flex;
    align-items: center;
    line-height: 1;
    gap: 4px;
  }

  svg {
    font-size: ${({ theme }) => theme.desktop.sizes.md};
    @media ${({ theme }) => theme.device.mobile} {
      font-size: ${({ theme }) => theme.mobile.sizes.md};
    }
  }

  &:hover {
    opacity: 0.8;
  }
`;

const spin = keyframes`
  to {
    transform: rotate(360deg);
  }
`;

const InitialLoaderOverlay = styled.div`
  position: fixed;
  inset: 0;
  background-color: rgba(255, 255, 255, 0.7);
  z-index: 9998;
  display: flex;
  align-items: center;
  justify-content: center;
`;

const InitialLoaderSpinner = styled.div`
  width: 56px;
  height: 56px;
  border: 4px solid rgba(9, 58, 110, 0.2);
  border-top-color: #093a6e;
  border-radius: 50%;
  animation: ${spin} 0.8s linear infinite;
`;
