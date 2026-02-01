// @/store/aricle
import { create } from 'zustand';
import type { Article } from '@/store/menu';

interface ArticleState {
  articles: Article[]; // ✅ 배열 하나만
  setArticles: (list: Article[]) => void;
  clearArticles: () => void;
}

export const useArticleStore = create<ArticleState>((set) => ({
  articles: [],
  setArticles: (list) => set({ articles: list }),
  clearArticles: () => set({ articles: [] }),
}));
