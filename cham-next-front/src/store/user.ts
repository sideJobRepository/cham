import { create } from 'zustand';

interface User {
  id: string;
  email: string;
  name: string;
  roles: string[];
}

interface UserStore {
  user: User | null;
  setUser: (user: User) => void;
  clearUser: () => void;
}

export const useUserStore = create<UserStore>((set) => ({
  user: null,
  setUser: (user) => set({ user }),
  clearUser: () => set({ user: null }),
}));

interface RecordUser {
  id: number;
  nickName: string;
}

interface RecordUserStore {
  recordUser: RecordUser[] | [];
  setRecordUser: (recordUser: RecordUser[]) => void;
}

export const useRecordUserStore = create<RecordUserStore>((set) => ({
  recordUser: [],
  setRecordUser: (recordUser) => set({recordUser})
}))