import { create } from 'zustand/index';
import { MenuData } from '@/store/menu';

interface SearchStore {
  search: MenuData | null;
  setSearch: (search: MenuData) => void;
  keyword: string;
  setKeyword: (keyword: string) => void;
}

export const useSearchDataStore = create<SearchStore>((set) => ({
  search: null,
  setSearch: (search) => set({ search }),
  keyword: '',
  setKeyword: (keyword) => set({ keyword }),
}));
