import { useEffect } from 'react';
import * as ReactDOM from 'react-dom';
import styled from 'styled-components';
import { FaTimes } from 'react-icons/fa';

export default function Modal({ open, onClose, title, children, color, textColor }) {
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
        <Header $color={color} $textColor={textColor}>
          <strong>{title}</strong>
          <FaTimes onClick={onClose} />
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
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: auto;
  margin: 0 20px;

  svg {
    cursor: pointer;
  }
`;

const Header = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 50px;
  padding: 0 20px;
  color: ${({ $textColor }) => $textColor};
  background-color: ${({ $color }) => $color};

  svg {
    width: 20px;
    height: 20px;
  }
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
