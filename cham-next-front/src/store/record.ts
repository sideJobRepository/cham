import { create } from 'zustand/index';

interface Yakuman{
  id: number;
  orders: number;
  yakumanName: string;
}

interface YakumanStore {
  yakuman: Yakuman[] | [];
  setYakuman: (yakuman: Yakuman[]) => void;
}

export const useYakumanStore = create<YakumanStore>((set) => ({
  yakuman: [],
  setYakuman: (yakuman) => set({yakuman})
}))