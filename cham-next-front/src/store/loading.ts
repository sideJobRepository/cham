import { create } from 'zustand';

interface loadingStore {
  loading: boolean;
  setLoading: (loading: boolean) => void;
  clearLoading: () => void;
}

export const useLoadingStore = create<loadingStore>(
  (set) => ({
    loading: false,
    setLoading: (loading) => set({loading}),
    clearLoading: () => set({loading: false}),
  })
)