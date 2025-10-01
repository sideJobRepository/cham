import { confirmAlert } from 'react-confirm-alert';
import styled from 'styled-components';

function ConfirmUI({ onClose, title, message, onConfirm, onCancel, gb }) {
  return (
    <AlertWrapper>
      <Message $gb={gb}>{title}</Message>
      <MessageCont>{message}</MessageCont>
      <ButtonGroup>
        <CancelButton
          onClick={() => {
            onCancel?.();
            onClose();
          }}
        >
          {gb ? '다음에 참여할게요' : '둘러보기 계속'}
        </CancelButton>

        <ConfirmButton
          onClick={() => {
            onConfirm();
            onClose();
          }}
        >
          {gb ? '후원으로 응원하기' : '로그인하고 모든 기능 사용하기'}
        </ConfirmButton>
      </ButtonGroup>
    </AlertWrapper>
  );
}

export function showConfirmModal(p) {
  confirmAlert({
    customUI: ({ onClose }) => <ConfirmUI {...p} onClose={onClose} />,
  });
}

const AlertWrapper = styled.div`
  background-color: #ffffff;
  padding: 20px 24px;
  border-radius: 6px;
  width: 100%;
  min-width: 416px;
  min-height: 137px;
  text-align: center;
  z-index: 9999;
  border: 1px solid #093a6e;
  box-shadow: 0 0 16px rgba(0, 0, 0, 0.4);

  @media (max-width: 844px) {
    min-width: unset;
    max-width: calc(100vw - 40px);
  }
`;

const Message = styled.div`
  display: flex;
  gap: 12px;
  align-items: center;
  font-size: 16px;
  color: #093a6e;
  white-space: pre-line;
  font-weight: bolder;
`;

const MessageCont = styled.div`
  display: flex;
  padding: 8px 0 0 0;
  text-align: left;
  gap: 6px;
  color: #222;
  font-size: 14px;
`;

const ButtonGroup = styled.div`
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
  gap: 12px;
`;

const BaseButton = styled.button`
  flex: 1;
  padding: 6px 15px;
  font-size: 14px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
`;

const CancelButton = styled(BaseButton)`
  background-color: #66696d;
  color: #ffffff;
`;

const ConfirmButton = styled(BaseButton)`
  background-color: #3e95ff;
  color: #ffffff;
`;
