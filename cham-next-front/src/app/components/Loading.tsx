'use client';

import styled, { keyframes } from 'styled-components';
import ReactDOM from 'react-dom';
import { useLoadingStore } from '@/store/loading';

export default function Loading() {
  const loading = useLoadingStore((state) => state.loading);

  if (!loading) return null;

  return ReactDOM.createPortal(
    <Overlay>
      <Spinner />
    </Overlay>,
    document.body
  );
}

const spin = keyframes`
    to {
        transform: rotate(360deg);
    }
`;

const Spinner = styled.div`
  width: 56px;
  height: 56px;
  border: 4px solid rgba(9, 58, 110, 0.2);
  border-top-color: #093a6e;
  border-radius: 50%;
  animation: ${spin} 0.8s linear infinite;
`;

const Overlay = styled.div`
  position: fixed;
  inset: 0;
  background-color: rgba(255, 255, 255, 0.6);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
`;
