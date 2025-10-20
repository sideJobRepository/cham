import { useRecoilValue } from 'recoil';
import { detailState, mapState, selectSearchState, userListState } from './appState';

export const useSelectSearchState = () => {
  return useRecoilValue(selectSearchState);
};

export const useSearchMapState = () => {
  return useRecoilValue(mapState);
};

export const useDetailMapState = () => {
  return useRecoilValue(detailState);
};

export const useUserListState = () => {
  return useRecoilValue(userListState);
};
