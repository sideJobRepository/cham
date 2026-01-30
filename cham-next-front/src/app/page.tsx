'use client';

import Image from 'next/image';
import styles from './page.module.css';
import { Wrapper } from '@/styles/Wrapper.styles';
import { motion } from 'framer-motion';
import styled from 'styled-components';
import { ArrowRight, HandPointing } from 'phosphor-react';

export default function Home() {
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
              <Button $bg="#093A6E" $color="#fff">
                키워드검색 넣을거 <ArrowRight weight="bold" />
              </Button>
              <Button $bg="#093A6E" $color="#fff">
                의견 보기 <ArrowRight weight="bold" />
              </Button>
            </ButtonBox>
          </HeroContent>
        </motion.div>
      </Hero>
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
    letter-spacing: 0.1rem;

    @media ${({ theme }) => theme.device.mobile} {
      font-size: ${({ theme }) => theme.mobile.sizes.h4Size};
    }
  }
`;

const ButtonBox = styled.div`
  display: flex;
  gap: 8px;
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

  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.mobile.sizes.xl};
  }
`;
