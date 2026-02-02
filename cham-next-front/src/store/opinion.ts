import { create } from 'zustand/index';

interface OpinionStore {
  opinion: any | null;
  setOpinion: (opinion: any | null) => void;
}

export const useOpinionStore = create<OpinionStore>((set) => ({
  opinion: null,
  setOpinion: (opinion) => set({ opinion }),
}));
