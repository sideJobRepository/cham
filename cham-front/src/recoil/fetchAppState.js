import { useSetRecoilState } from 'recoil';
import api from '@/utils/axiosInstance.js';
import { selectSearchState, mapState, detailState } from './appState.js';
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

export function useMapSearch() {
  const setState = useSetRecoilState(mapState);
  const setDetailState = useSetRecoilState(detailState);

  return async (params = {}) => {
    const toastId = toast.loading('요청하신 정보를 불러오는 중 입니다.');
    try {
      const res = await api.post('/cham/cardUse', params);
      if (params?.detail) {
        setDetailState({
          mapDetailData: res.data,
          mapDetailLoading: false,
        });
      } else {
        setState({
          mapData: res.data,
          mapLoading: false,
        });
      }
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
