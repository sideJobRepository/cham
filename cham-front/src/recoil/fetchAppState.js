import { useSetRecoilState } from 'recoil';
import api from '@/utils/axiosInstance.js';
import { selectSearchState } from './appState.js';

export function useFetchSelectSearch() {
  const setState = useSetRecoilState(selectSearchState);

  return async () => {
    try {
      const res = await api.get('/cham/position');
      setState({
        data: res.data,
        isLoading: false,
      });
    } catch (e) {
      console.error('초기 요청 실패:', e);
      setState({
        data: [],
        isLoading: false,
      });
    }
  };
}
