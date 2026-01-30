import { create } from 'zustand';

export interface File {
  id: number;
  fileName: string;
  fileUrl: string;
  fileFolder: string;
}

interface Rule {
  id: number;
  tournamentStatus: string;
  file: File;
}

interface RuleStore {
  rule: Rule[] | [];
  setRule: (rule: Rule[]) => void;
}


export const useRuleStore = create<RuleStore>((set) => ({
  rule: [],
  setRule: (rule) => set({rule}),
}))