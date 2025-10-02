import styled from 'styled-components';
import { AiOutlinePlusCircle } from 'react-icons/ai';
import { useRecoilValue } from 'recoil';
import { mapSearchFilterState, userState } from '@/recoil/appState.js';
import { useEffect, useRef, useState } from 'react';
import axios from 'axios';
import { useMapSearch } from '@/recoil/fetchAppState.js';
import { toast } from 'react-toastify';
import basicLogo from '/basicLogo.png';
import Modal from '@/components/modal/Modal.jsx';
import DetailPage from '@/pages/DetailPage.jsx';

export default function ItemCard({ data }) {
  const searchCondition = useRecoilValue(mapSearchFilterState);
  const mapSearch = useMapSearch();
  const fileInputRef = useRef();
  const user = useRecoilValue(userState);
  const [open, setOpen] = useState(false);
  const [detailParams, setDetailParams] = useState(null);

  const [catetgory, setCategory] = useState('');

  const handlePlusClick = e => {
    fileInputRef.current.click();
  };

  // 이미지 업로드
  const handleFileChange = async e => {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('cardUseImageUrl', file);
    formData.append('cardUseAddrId', data.cardUseAddrId);

    const toastId = toast.loading('이미지 업로드 중 입니다.');

    try {
      await axios.post('/cham/cardUseAddrImage', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });

      mapSearch(searchCondition);
      toast.update(toastId, {
        render: '이미지 업로드가 완료됐습니다.',
        type: 'success',
        isLoading: false,
        autoClose: 1000,
      });
    } catch (err) {
      toast.update(toastId, {
        render: '이미지 업로드에 실패하였습니다.',
        type: 'error',
        isLoading: false,
        autoClose: 3000,
      });
      console.error('업로드 실패', err);
    }
  };

  // 디테일 열기 → 모달
  const handleClick = data => {
    const params = {
      addrDetail: data.addrDetail,
      detail: true,
      catLabel: data.categoryLabel
        ?.split('>')
        .map(s => s.trim())
        ?.slice(0, 3)
        ?.join(' > '),
    };
    setDetailParams(params);
    setOpen(true);
  };

  useEffect(() => {
    if (data?.categoryLabel === '기타') {
      setCategory('기타');
    } else {
      setCategory(data?.categoryLabel?.split('>').map(s => s.trim())[1]);
    }
  }, [data]);

  return (
    <>
      <Card
        onClick={() => {
          handleClick(data);
        }}
      >
        <ImageWrapper>
          {data?.cardUseImageUrl ? (
            <img src={data?.cardUseImageUrl} alt="식당 대표 이미지" />
          ) : (
            <img src={basicLogo} alt="기본 로고 이미지" />
          )}
          <ImageText>
            {data?.cardUseRegion} {data.cardUseUser} {data.visitMember}
          </ImageText>
          {user?.role === 'ADMIN' ? (
            <PlusButtonWrapper
              onClick={e => {
                e.stopPropagation();
                handlePlusClick();
              }}
            >
              <PlusButton size={30} />
              <input
                type="file"
                accept="image/*"
                style={{ display: 'none' }}
                ref={fileInputRef}
                onChange={handleFileChange}
              />
            </PlusButtonWrapper>
          ) : null}
        </ImageWrapper>
        <CardBody>
          <Title>
            {data.addrName} <span>{catetgory}</span>
          </Title>
          <Stats>방문횟수 {data.visits}</Stats>
          <Price>총 {data.totalSum.toLocaleString()}원</Price>
          <Menu>{data.visitMember} 방문</Menu>
          <Address>{data.addrDetail}</Address>
        </CardBody>
      </Card>

      <Modal open={open} onClose={() => setOpen(false)} title="상세보기">
        <DetailPage initialParams={detailParams} />
      </Modal>
    </>
  );
}

const Card = styled.article`
  overflow: hidden;
  width: 100%;
  padding: 20px 0;
  box-sizing: border-box;
  cursor: pointer;
  border-bottom: 1px solid ${({ theme }) => theme.colors.border};

  img {
    width: 100%;
    height: 180px;
    border-radius: 8px;
    object-fit: cover;
  }

  &:last-child {
    border-bottom: none;
  }
`;

const CardBody = styled.div`
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  position: relative;
`;

const Title = styled.span`
  font-size: ${({ theme }) => theme.sizes.large};
  font-weight: bold;
  color: ${({ theme }) => theme.colors.primary};
  margin: 0;

  > span {
    color: ${({ theme }) => theme.colors.liteGray};
    font-size: ${({ theme }) => theme.sizes.medium};
  }
`;

const Stats = styled.div`
  display: flex;
  font-size: ${({ theme }) => theme.sizes.medium};
  color: ${({ theme }) => theme.colors.liteGray};
`;

const Menu = styled.p`
  font-size: ${({ theme }) => theme.sizes.medium};
  margin-top: 12px;
  color: ${({ theme }) => theme.colors.liteGray};
  font-weight: bold;
`;

const Address = styled.p`
  font-size: ${({ theme }) => theme.sizes.medium};
  color: ${({ theme }) => theme.colors.liteGray};
`;

const Price = styled.p`
  font-size: ${({ theme }) => theme.sizes.large};
  font-weight: bold;
  color: black;
  margin-top: 12px;
`;

const EmptyImage = styled.div`
  width: 100%;
  height: 180px;
  border-radius: 8px;
  background-color: ${({ theme }) => theme.colors.liteGray};
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;

  span {
    color: white;
  }
`;

const ImageWrapper = styled.div`
  position: relative;
  width: 100%;

  img {
    width: 100%;
    height: 100%;
    border-radius: 8px;
    object-fit: cover;
  }
`;

const PlusButtonWrapper = styled.div`
  position: absolute;
  top: 44%;
  left: 44%;
  cursor: pointer;

  &:hover svg {
    transform: scale(1.2);
  }
`;
const PlusButton = styled(AiOutlinePlusCircle)`
  cursor: pointer;
  pointer-events: auto;
  color: white;

  &:hover {
    transform: scale(1.2);
  }
`;

const ImageText = styled.p`
  position: absolute;
  bottom: 8px;
  left: 8px;
  color: white;
  font-size: 13px;
  background: rgba(0, 0, 0, 0.5);
  padding: 4px 8px;
  border-radius: 4px;
`;
