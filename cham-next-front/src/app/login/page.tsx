'use client';
import { Wrapper } from '@/styles/Wrapper.styles';
import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import styled from 'styled-components';
import { withBasePath } from '@/lib/path';
import { MagnifyingGlass } from 'phosphor-react';

export default function Login() {
  const [mounted, setMounted] = useState(false);

  function login(name: string) {
    let CLIENT_ID;
    let REDIRECT_URL;

    let authUrl;

    if (name === 'KAKAO') {
      CLIENT_ID = process.env.NEXT_PUBLIC_KAKAO_CLIENT_ID!;
      REDIRECT_URL = process.env.NEXT_PUBLIC_KAKAO_REDIRECT_URL!;
      authUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URL}&response_type=code`;
    } else if (name === 'NAVER') {
      CLIENT_ID = process.env.NEXT_PUBLIC_NAVER_CLIENT_ID!;
      REDIRECT_URL = process.env.NEXT_PUBLIC_NAVER_REDIRECT_URL!;
      const STATE = crypto.randomUUID();
      authUrl = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${CLIENT_ID}&redirect_uri=${encodeURIComponent(REDIRECT_URL)}&state=${STATE}`;
    }

    if (authUrl) window.location.href = authUrl;
  }

  useEffect(() => setMounted(true), []);

  if (!mounted) return null;
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
          </HeroContent>
        </motion.div>
      </Hero>
      <Title
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8, ease: 'easeOut' }}
      >
        <h1>로그인</h1>
        <span>로그인 후 다양한 의견을 자유롭게 남겨보세요.</span>
      </Title>
      <LoginBox
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.8, duration: 0.8, ease: 'easeOut' }}
      >
        <Top>
          <img src={withBasePath('/phone.png')} alt="로고" />
        </Top>
        <Bottom>
          <Button onClick={() => login('KAKAO')} $bgColor="#f3d911" $color="#2f250c">
            <img src={withBasePath('/kakao.png')} alt="카카오 로그인 로고" />
            카카오로 계속하기
          </Button>
          <Button onClick={() => login('NAVER')} $bgColor="#03a74d" $color="#ffffff">
            <img src={withBasePath('/naver.png')} alt="네이버 로그인 로고" />
            네이버로 계속하기
          </Button>
        </Bottom>
      </LoginBox>
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

const Title = styled(motion.div)`
  display: flex;
  flex-direction: column;
  margin-top: 64px;
  width: 90%;
  max-width: 800px;
  align-self: center;
  text-align: center;
  gap: 8px;
  margin-bottom: 36px;

  h1 {
    font-size: ${({ theme }) => theme.desktop.sizes.h1Size};
    font-weight: 800;
    @media ${({ theme }) => theme.device.mobile} {
      font-size: ${({ theme }) => theme.mobile.sizes.h2Size};
    }
  }

  span {
    font-size: ${({ theme }) => theme.desktop.sizes.md};
    font-weight: 600;
    color: ${({ theme }) => theme.colors.grayColor};

    @media ${({ theme }) => theme.device.mobile} {
      font-size: ${({ theme }) => theme.desktop.sizes.md};
    }
  }
`;

const LoginBox = styled(motion.div)`
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: 80%;
  height: 420px;
  gap: 36px;
  max-width: 480px;
  padding: 24px 0;
  margin: 0 auto 64px;
  border: 8px solid #f3f3f3;
  background-color: #f3f3f3;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.24);
`;

const Top = styled.section`
  display: flex;
  width: 100%;
  height: 70%;
  margin: 0;
  align-items: flex-start;
  justify-content: center;
  overflow: hidden;

  img {
    width: auto;
    height: 100%;
    object-fit: cover;
    object-position: top;
    display: block;
  }
`;
const Bottom = styled.section`
  width: 100%;
  height: 30%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 0 12px;
`;

const Button = styled.button<{ $color: string; $bgColor: string }>`
  display: flex;
  align-items: center;
  max-width: 340px;
  height: 52px;
  justify-content: center;
  width: 100%;
  gap: 16px;
  border: none;

  background-color: ${({ $bgColor }) => $bgColor};
  color: ${({ $color }) => $color};
  font-size: ${({ theme }) => theme.desktop.sizes.md};
  font-weight: 500;
  cursor: pointer;

  img {
    width: 22px;
    height: 22px;
  }
`;
