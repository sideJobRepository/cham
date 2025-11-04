import styled from 'styled-components';
import Modal from '@/components/modal/Modal.jsx';
import { ReactComponent as PrevIcon } from '../../assets/prev.svg?react';
import { ReactComponent as NextIcon } from '../../assets/next.svg?react';

export default function ImageModal({ open, urls, index, setIndex, onClose }) {
  if (!open) return null;

  return (
    <Modal open={open} onClose={onClose} title="" color="#ffffff">
      <Wrapper>
        <StyledImg src={urls[index]} alt="" />

        {urls.length > 1 && (
          <>
            {index !== 0 && (
              <ArrowLeft onClick={() => setIndex(prev => Math.max(prev - 1, 0))}>
                <PrevIcon />
              </ArrowLeft>
            )}

            {index !== urls.length - 1 && (
              <ArrowRight onClick={() => setIndex(prev => Math.min(prev + 1, urls.length - 1))}>
                <NextIcon />
              </ArrowRight>
            )}
          </>
        )}
      </Wrapper>
    </Modal>
  );
}

const Wrapper = styled.div`
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
`;

const StyledImg = styled.img`
  max-width: 90vw;
  max-height: 80vh;
  object-fit: contain;
`;

const ArrowBase = styled.button`
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.2);
  border: none;
  color: #fff;
  width: 32px;
  height: 32px;
  cursor: pointer;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;

  svg {
    width: 40px;
    height: 40px;
  }

  &:hover {
    background: rgba(0, 0, 0, 0.4);
  }
`;

const ArrowLeft = styled(ArrowBase)`
  left: 10px;
`;

const ArrowRight = styled(ArrowBase)`
  right: 10px;
`;
