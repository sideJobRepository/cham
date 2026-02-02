'use client';

import { useEffect } from 'react';
import styled from 'styled-components';

type ErrorPageProps = {
  error: Error & { digest?: string };
  reset: () => void;
};

export default function ErrorPage({ error, reset }: ErrorPageProps) {
  useEffect(() => {
    console.error('App route error:', error);
  }, [error]);

  return (
    <Wrapper>
      <Card>
        <Title>페이지를 불러오지 못했습니다.</Title>
        <Desc>잠시 후 다시 시도해주세요. 문제가 계속되면 관리자에게 문의해주세요.</Desc>
        <RetryButton type="button" onClick={reset}>
          다시 시도
        </RetryButton>
      </Card>
    </Wrapper>
  );
}

const Wrapper = styled.section`
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
`;

const Card = styled.div`
  width: 100%;
  max-width: 480px;
  background: #f8fbff;
  border: 1px solid #d4e3f7;
  border-radius: 10px;
  padding: 28px 24px;
  text-align: center;
  box-shadow: 0 6px 20px rgba(9, 58, 110, 0.08);
`;

const Title = styled.h3`
  font-weight: 800;
  color: #0f2740;
  margin: 0 0 10px;
  font-size: 20px;
`;

const Desc = styled.p`
  font-size: 14px;
  color: #35526d;
  line-height: 1.6;
  margin: 0 0 18px;
`;

const RetryButton = styled.button`
  border: none;
  background: #093a6e;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  border-radius: 6px;
  padding: 10px 16px;
  cursor: pointer;

  &:hover {
    opacity: 0.9;
  }
`;
