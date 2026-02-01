'use client';

import Image from 'next/image';
import styles from './page.module.css';
import { Wrapper } from '@/styles/Wrapper.styles';
import { motion } from 'framer-motion';
import styled from 'styled-components';
import { ArrowRight, HandPointing, MagnifyingGlass } from 'phosphor-react';
import { useEffect, useState } from 'react';
import { useArticleStore } from '@/store/aricle';

export default function Home() {
  const [searchKeyword, setSearchKeyword] = useState('');

  const mainData = useArticleStore((state) => state.articles);

  useEffect(() => {
    if (mainData.length === 0) return;

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  }, [mainData]);

  return (
    <Wrapper>
      <Hero>
        <HeroFillOverlay
          initial={{ scaleX: 0 }}
          animate={{ scaleX: 1 }}
          transition={{
            duration: 1.1,
            ease: [0.4, 0, 0.2, 1],
          }}
        />
        <motion.div
          style={{ position: 'relative', zIndex: 1 }}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{
            duration: 1.2,
            ease: [0.4, 0, 0.2, 1],
            delay: 1.1,
          }}
        >
          <HeroContent>
            <h2>
              대전을 사람의 만남이 아름다운 도시로,
              <br />
              열린시대 새 지방자치를 만들어갑니다.
            </h2>
            <h4>
              시민의 자발적인 참여와 연대에 기초해 참된 주민자치를 실현하는
              대전참여자치시민연대입니다.
            </h4>
            <ButtonBox>
              <SearchGroup
                onSubmit={(e) => {
                  e.preventDefault();
                  // onSearch?.();
                }}
              >
                <FieldsWrapper>
                  <Field>
                    <label>키워드</label>
                    <input
                      type="text"
                      placeholder="검색어를 입력해주세요."
                      value={searchKeyword ?? ''}
                      onChange={(e) => setSearchKeyword(e.target.value)}
                    />
                  </Field>
                </FieldsWrapper>
                <SearchButton type="submit">
                  <MagnifyingGlass weight="bold" />
                  <span>검색</span>
                </SearchButton>
              </SearchGroup>
              {/*<Button $bg="#093A6E" $color="#fff">*/}
              {/*  <span>의견 보기</span>*/}
              {/*  <ArrowRight weight="bold" />*/}
              {/*</Button>*/}
            </ButtonBox>
          </HeroContent>
        </motion.div>
      </Hero>
      {mainData.length > 0 && (
        <ArticleSection>
          {mainData.map((article) => (
            <ArticleItem key={article.articleId}>
              <ArticleNo>{article.articleNo}</ArticleNo>
              <ArticleTitle>{article.articleTitle}</ArticleTitle>
              <ArticleContent>{article.content}</ArticleContent>
            </ArticleItem>
          ))}
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

    @media ${({ theme }) => theme.device.mobile} {
      font-size: ${({ theme }) => theme.mobile.sizes.h4Size};
    }
  }
`;

const ButtonBox = styled.div`
  display: flex;
  gap: 12px;
  margin-top: 44px;
`;

const Button = styled.button<{ $bg: string; $color: string }>`
  background: ${({ $bg }) => $bg};
  color: ${({ $color }) => $color};
  border: none;
  padding: 12px 16px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: 500;
  font-size: ${({ theme }) => theme.desktop.sizes.xl};
  box-shadow: 2px 4px 2px rgba(0, 0, 0, 0.2);

  span {
    display: flex;
    align-items: center;
    line-height: 1;
  }

  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.mobile.sizes.xl};
  }
`;
const SearchGroup = styled.form`
  display: flex;
  background-color: ${({ theme }) => theme.colors.white};
  flex: 1;
  align-items: center;
  justify-content: space-between;
  padding: 2px 4px 2px 12px;
  border: 1px solid ${({ theme }) => theme.colors.lineColor};
  border-radius: 4px;
  flex-wrap: nowrap;

  @media ${({ theme }) => theme.device.mobile} {
    width: 100%;
  }
`;

const FieldsWrapper = styled.div`
  display: flex;
  width: 100%;
  align-items: center;
  flex: 1;
  overflow-x: auto;
  flex-wrap: nowrap;
  overflow-y: hidden;
`;

const Field = styled.div`
  display: flex;
  flex-direction: column;
  width: 100%;
  flex-shrink: 0;

  label {
    font-size: ${({ theme }) => theme.desktop.sizes.sm};
    color: ${({ theme }) => theme.colors.blackColor};
    font-weight: 600;
    text-align: left;

    @media ${({ theme }) => theme.device.mobile} {
      font-size: ${({ theme }) => theme.mobile.sizes.sm};
    }
  }

  input {
    border: none;
    width: 100%;
    padding-right: 8px;
    outline: none;
    color: ${({ theme }) => theme.colors.inputColor};
    background: transparent;
    font-size: ${({ theme }) => theme.desktop.sizes.md};
    @media ${({ theme }) => theme.device.mobile} {
      font-size: ${({ theme }) => theme.mobile.sizes.md};
    }
  }
`;

const SearchButton = styled.button`
  display: flex;
  align-items: center;
  gap: 4px;
  background: #093a6e;
  font-size: ${({ theme }) => theme.desktop.sizes.md};
  box-shadow: 2px 4px 2px rgba(0, 0, 0, 0.2);
  border: none;
  color: white;
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
  &:hover {
    opacity: 0.8;
  }

  span {
    display: flex;
    align-items: center;
    line-height: 1;
  }

  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.mobile.sizes.md};
  }
`;

const ArticleSection = styled.section`
  width: 100%;
  margin: 32px auto 32px;
  padding: 0 32px;
  display: flex;
  flex-direction: column;
  gap: 32px;

  @media ${({ theme }) => theme.device.mobile} {
    padding: 0 16px;
  }
`;

const ArticleItem = styled.article`
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 32px;
  border-bottom: 1px solid ${({ theme }) => theme.colors.lineColor};
`;

const ArticleNo = styled.div`
  font-size: ${({ theme }) => theme.desktop.sizes.xl};
  font-weight: 700;
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

  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.mobile.sizes.md};
  }
`;
