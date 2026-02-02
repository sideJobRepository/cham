import { create } from 'zustand/index';
import { Legislation } from '@/store/menu';

export interface OpinionData {
  articleId: number;
  concernCount: number;
  greatId: number;
  oppositionCount: number;
  selectedType: string;
  supportCount: number;
}

interface OpinionStore {
  opinion: OpinionData[] | null;
  setOpinion: (opinion: OpinionData[] | null) => void;
}

export const useOpinionStore = create<OpinionStore>((set) => ({
  opinion: null,
  setOpinion: (opinion) => set({ opinion }),
}));
