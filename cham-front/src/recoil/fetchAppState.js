import { useRecoilValue, useSetRecoilState } from 'recoil';
import api from '@/utils/axiosInstance.js';
import { selectSearchState, mapState, detailState, checkDataState, userState } from './appState.js';
import { toast } from 'react-toastify';

export function useFetchSelectSearch() {
  const setState = useSetRecoilState(selectSearchState);

  return async () => {
    try {
      const res = await api.get('/cham/position');
      setState({
        selectData: res.data,
        selectLoading: false,
      });
    } catch (e) {
      console.error('초기 요청 실패:', e);
      setState({
        selectData: [],
        selectLoading: false,
      });
    }
  };
}

//메인데이터
export function useMapSearch() {
  const setState = useSetRecoilState(mapState);

  return async (params = {}) => {
    const toastId = toast.loading('요청하신 정보를 불러오는 중 입니다.');

    console.log('params', params);

    try {
      let url = `/cham/cardUse`;
      if (params?.cardOwnerPositionId && params?.input) {
        url = `/cham/cardUse?cardOwnerPositionId=${params?.cardOwnerPositionId}&input=${params?.input}`;
      } else if (params?.cardOwnerPositionId) {
        url = `/cham/cardUse?cardOwnerPositionId=${params?.cardOwnerPositionId}`;
      } else if (params?.input) {
        url = `/cham/cardUse?input=${params?.input}`;
      }
      const res = await api.get(url);

      console.log('detail', params);
      console.log('res', res);

      setState({
        mapData: res.data,
        mapLoading: false,
      });

      toast.dismiss(toastId);
    } catch (e) {
      toast.update(toastId, {
        render: '검색에 실패하였습니다.',
        type: 'error',
        isLoading: false,
        autoClose: 3000,
      });
      console.error('검색 실패:', e);
      setState({
        data: [],
        mapLoading: false,
      });
    }
  };
}

//디테일데이터
export function useFetchDetailData() {
  const setDetailState = useSetRecoilState(detailState);

  return async addr => {
    const toastId = toast.loading('상세 정보를 불러오는 중 입니다.');
    try {
      console.log('addr', addr);
      const res = await api.get(`/cham/cardUseDetail?addrDetail=${addr}`);
      setDetailState({
        mapDetailData: res.data,
        mapDetailLoading: false,
      });

      toast.dismiss(toastId);
    } catch (e) {
      console.error('디테일 요청 실패:', e);
      toast.update(toastId, {
        render: '디테일 조회에 실패하였습니다.',
        type: 'error',
        isLoading: false,
        autoClose: 3000,
      });
      setDetailState({
        mapDetailData: [],
        mapDetailLoading: false,
      });
    }
  };
}

//가봤어요, 궁금해요
export function useFetchCard() {
  const setState = useSetRecoilState(checkDataState);

  return async id => {
    try {
      const res = await api.get(`/cham/check?addrId=${id}`);
      console.log('red', res);
      setState(res.data);
    } catch (e) {
      console.error('초기 요청 실패:', e);
      setState(null);
    }
  };
}
