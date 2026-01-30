'use client';

import React from 'react';
import styled from 'styled-components';
import ClientProviders from '../providers';
import Header from './Header';
import AuthListener from '@/app/components/AuthListener';

export default function ClientLayout({ children }: { children: React.ReactNode }) {
  console.log('ClientLayout styled ===', styled);
  return (
    <ClientProviders>
      {/*<AuthListener />*/}
      <Wrapper>
        <Inner>
          <LeftArea>
            <Header />
          </LeftArea>
          <MainArea>{children}</MainArea>
        </Inner>
      </Wrapper>
    </ClientProviders>
  );
}

const Wrapper = styled.div`
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  overflow-y: visible;
`;

const Inner = styled.div`
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
`;

const LeftArea = styled.header`
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  width: 100%;
  height: 80px;
  background-color: ${({ theme }) => theme.colors.whiteColor};
  border-bottom: 1px solid ${({ theme }) => theme.colors.lineColor};
  z-index: 3;
`;

const MainArea = styled.main`
  display: flex;
  z-index: 0;
  flex: 1;
  height: 100%;
  width: 100%;
  background-color: ${({ theme }) => theme.colors.whiteColor};
  margin-top: 80px;
  overflow-x: clip;
  //overflow-y: auto;
  //overflow-x: hidden;
`;
