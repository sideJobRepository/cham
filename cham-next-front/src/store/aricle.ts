import { create } from 'zustand/index';
import { Article, MenuData } from '@/store/menu';

interface ArticleStore {
  article: Article | null;
  setArticle: (article: Article) => void;
  clearMenu: () => void;
}

export const useArticleStore = create<ArticleStore>((set) => ({
  article: null,
  setArticle: (article) => set({ article }),
  clearMenu: () => set({ article: null }),
}));
