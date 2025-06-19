import { useSetRecoilState } from 'recoil';
import api from '@/utils/axiosInstance.js';
import { selectSearchState, mapState, detailState } from './appState.js';

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
    } catch (e) {
      console.error('초기 요청 실패:', e);
      setState({
        data: [],
        mapLoading: false,
      });
    }
  };
}
