'use client';

import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import styled from 'styled-components';
import { withBasePath } from '@/lib/path';

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

const Wrapper = styled.div`
  display: flex;
  max-width: 1500px;
  min-width: 1280px;
  min-height: 600px;
  height: 100%;
  margin: auto;
  flex-direction: column;
  gap: 36px;

  @media ${({ theme }) => theme.device.tablet} {
    width: 100vw;
    max-width: 100%;
    min-width: 100%;
    min-height: unset;
  }
`;

const Title = styled(motion.div)`
  display: flex;
  flex-direction: column;
  width: 90%;
  max-width: 800px;
  align-self: center;
  text-align: center;
  gap: 8px;
  margin-bottom: 24px;

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
  max-width: 600px;
  padding: 24px 0;
  margin: auto;
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
  max-width: 310px;
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
