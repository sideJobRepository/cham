import { atom } from 'recoil';

export const mapState = atom({
  key: 'mapState',
  default: false,
});

export const selectSearchState = atom({
  key: 'selectSearchState',
  default: {
    data: [],
    isLoading: true,
  },
});
