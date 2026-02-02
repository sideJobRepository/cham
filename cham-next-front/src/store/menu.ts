import { create } from 'zustand';

/* ===== Article ===== */
export interface Article {
  articleId: number;
  articleNo: string;
  articleTitle: string;
  content: string;
  categoryMain: string;
  categorySub: string;
  replyCount: number;
}

/* ===== Section ===== */
export interface Section {
  section: string | null;
  articles: Article[];
}

/* ===== Chapter (추가) ===== */
export interface Chapter {
  chapter: string | null;
  sections: Section[];
}

/* ===== Part (수정) ===== */
export interface Part {
  part: string;
  chapters: Chapter[];
}

/* ===== Legislation ===== */
export interface Legislation {
  id: number;
  title: string;
  billVersion: string;
  parts: Part[];
}

/* ===== MenuData ===== */
export interface MenuData {
  legislations: Legislation[];
}

/* ===== Store ===== */
interface MenuStore {
  menu: MenuData | null;
  setMenu: (menu: MenuData) => void;
  clearMenu: () => void;
}

export const useMenuStore = create<MenuStore>((set) => ({
  menu: null,
  setMenu: (menu) => set({ menu }),
  clearMenu: () => set({ menu: null }),
}));
