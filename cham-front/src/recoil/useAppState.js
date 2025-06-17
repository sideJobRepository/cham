import { useRecoilValue } from 'recoil';
import { selectSearchState } from './appState';

export const useSelectSearchState = () => {
  return useRecoilValue(selectSearchState);
};
