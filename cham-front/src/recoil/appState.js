import { atom } from 'recoil';

const today = new Date();
const oneMonthAgo = new Date(new Date().getFullYear(), 0, 1);

export const userState = atom({
  key: 'userState',
  default: null,
});

export const mapState = atom({
  key: 'mapState',
  default: {
    mapData: null,
    mapLoading: true,
  },
});

export const detailState = atom({
  key: 'detailState',
  default: {
    mapDetailData: null,
    mapDetailLoading: true,
  },
});

export const selectSearchState = atom({
  key: 'selectSearchState',
  default: {
    selectData: [],
    selectLoading: true,
  },
});

export const mapCenterAddrState = atom({
  key: 'mapCenterAddrState',
  default: {},
});

//검색조건
export const mapSearchFilterState = atom({
  key: 'mapSearchFilterState',
  default: {
    cardOwnerPositionId: null,
    input: '',
    sortOrder: 1,
    sortValue: { value: 1, label: '최신순' },
  },
});

export const checkDataState = atom({
  key: 'checkDataState',
  default: null,
});

//관리자 유저정보
export const userListState = atom({
  key: 'userListState',
  default: {
    userData: null,
    userLoading: true,
  },
});

//테마
export const themeListState = atom({
  key: 'themeListState',
  default: {
    themeData: null,
    themeLoading: true,
  },
});
