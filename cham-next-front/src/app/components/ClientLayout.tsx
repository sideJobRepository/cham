'use client';

import React from 'react';
import styled from 'styled-components';
import ClientProviders from '../providers';
import Header from './Header';
import type { MenuData } from '@/store/menu';
import AuthListener from '@/app/components/AuthListener';
import Loading from '@/app/components/Loading';

type ClientLayoutProps = {
  children: React.ReactNode;
  initialMenuData?: MenuData | null;
};

export default function ClientLayout({ children, initialMenuData }: ClientLayoutProps) {
  console.log('ClientLayout styled ===', styled);
  return (
    <ClientProviders>
      <AuthListener />
      <Loading />
      <Wrapper>
        <Inner>
          <LeftArea>
            <Header initialMenuData={initialMenuData ?? null} />
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
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
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
  @media ${({ theme }) => theme.device.desktop} {
    margin-left: 300px;
    width: calc(100% - 300px);
  }
`;
