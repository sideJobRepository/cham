import { atom } from 'recoil';

export const mapState = atom({
  key: 'mapState',
  default: {
    mapData: null,
    mapLoading: true,
  },
});

export const selectSearchState = atom({
  key: 'selectSearchState',
  default: {
    selectData: [],
    selectLoading: true,
  },
});
