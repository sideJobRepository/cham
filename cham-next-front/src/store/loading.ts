import { create } from 'zustand';

interface loadingStore {
  loading: boolean;
  pendingCount: number;
  setLoading: (loading: boolean) => void;
  clearLoading: () => void;
}

export const useLoadingStore = create<loadingStore>(
  (set) => ({
    loading: false,
    pendingCount: 0,
    setLoading: (loading) =>
      set((state) => {
        const nextCount = loading
          ? state.pendingCount + 1
          : Math.max(0, state.pendingCount - 1);
        return {
          pendingCount: nextCount,
          loading: nextCount > 0,
        };
      }),
    clearLoading: () => set({ loading: false, pendingCount: 0 }),
  })
)
