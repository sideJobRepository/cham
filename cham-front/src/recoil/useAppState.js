import { useRecoilValue } from 'recoil';
import { mapState, selectSearchState } from './appState';

export const useSelectSearchState = () => {
  return useRecoilValue(selectSearchState);
};

export const useSearchMapState = () => {
  return useRecoilValue(mapState);
};
