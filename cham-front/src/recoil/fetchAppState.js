import { useRecoilValue, useSetRecoilState } from 'recoil';
import api from '@/utils/axiosInstance.js';
import {
  selectSearchState,
  mapState,
  detailState,
  checkDataState,
  userListState,
  themeListState,
  loadingState,
} from './appState.js';
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
  const setLoading = useSetRecoilState(loadingState);

  return async (params = {}) => {
    setLoading(true);

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

      setState({
        mapData: res.data,
        mapLoading: false,
      });
    } catch (e) {
      toast.error('지도 조회에 실패하였습니다.');
      console.error('검색 실패:', e);
      setState({
        data: [],
        mapLoading: false,
      });
    } finally {
      setLoading(false);
    }
  };
}

//디테일데이터
export function useFetchDetailData() {
  const setDetailState = useSetRecoilState(detailState);
  const setLoading = useSetRecoilState(loadingState);

  return async addr => {
    setLoading(true);
    try {
      const res = await api.get(`/cham/cardUseDetail?addrDetail=${addr}`);
      setDetailState({
        mapDetailData: res.data.details,
        mapDetailLoading: false,
      });
    } catch (e) {
      console.error('디테일 요청 실패:', e);
      toast.error('디테일 조회에 실패하였습니다.');
      setDetailState({
        mapDetailData: [],
        mapDetailLoading: false,
      });
    } finally {
      setLoading(false);
    }
  };
}

//가봤어요, 궁금해요
export function useFetchCard() {
  const setState = useSetRecoilState(checkDataState);

  return async id => {
    try {
      const res = await api.get(`/cham/check?addrId=${id}`);

      setState(res.data);
    } catch (e) {
      console.error('초기 요청 실패:', e);
      setState(null);
    }
  };
}

//관리자 유저 정보
export function useFetchUserList() {
  const setState = useSetRecoilState(userListState);
  const setLoading = useSetRecoilState(loadingState);

  return async (page = {}) => {
    setLoading(true);

    try {
      const res = await api.get(`/cham/role?page=${page}&size=${5}`);

      setState({
        userData: res.data,
        userLoading: false,
      });
    } catch (e) {
      toast.error('유저 조회에 실패하였습니다.');
      console.error('검색 실패:', e);
      setState({
        userData: [],
        userLoading: false,
      });
    } finally {
      setLoading(false);
    }
  };
}

//관리자 테마 정보
export function useFetchThemeList() {
  const setState = useSetRecoilState(themeListState);
  const setLoading = useSetRecoilState(loadingState);

  return async () => {
    setLoading(true);

    try {
      const res = await api.get(`/cham/theme`);

      setState({
        themeData: res.data,
        themeLoading: false,
      });
    } catch (e) {
      toast.error('테마 조회에 실패하였습니다.');
      console.error('검색 실패:', e);
      setState({
        themeData: [],
        themeLoading: false,
      });
    } finally {
      setLoading(false);
    }
  };
}
