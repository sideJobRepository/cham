import { useEffect } from 'react';
import * as ReactDOM from 'react-dom';
import styled from 'styled-components';

export default function Modal({ open, onClose, title, children }) {
  useEffect(() => {
    if (!open) return;
    const onKey = e => e.key === 'Escape' && onClose?.();
    document.addEventListener('keydown', onKey);
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.style.overflow = prev;
    };
  }, [open, onClose]);

  if (!open) return null;

  return ReactDOM.createPortal(
    <Backdrop onClick={onClose}>
      <Dialog onClick={e => e.stopPropagation()}>
        <Header>
          <strong>{title}</strong>
          <CloseBtn onClick={onClose}>×</CloseBtn>
        </Header>
        <Body>{children}</Body>
      </Dialog>
    </Backdrop>,
    document.body
  );
}

const Backdrop = styled.div`
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
`;

const Dialog = styled.div`
  width: 100%;
  max-width: 90%;
  height: 90%;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
`;

const Header = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-bottom: 1px solid #eee;
`;

const CloseBtn = styled.button`
  border: 0;
  background: transparent;
  font-size: 22px;
  cursor: pointer;
`;

const Body = styled.div`
  flex: 1;
  overflow: hidden; /* iframe 채우기 */
`;
