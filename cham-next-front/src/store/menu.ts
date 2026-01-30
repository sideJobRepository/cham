import { create } from 'zustand';

interface SubItem {
  menuId: string;
  subMenuId: number;
  name: string;
  link: string;
}

interface MenuItem {
  menuId: number;
  name: string;
  link: string;
  subMenu: SubItem[];
}

interface MenuStore {
  menu: MenuItem[] | null;
  setMenu: (menu: MenuItem[]) => void;
  clearMenu: () => void;
}

export const useMenuStore = create<MenuStore>((set) => ({
  menu: null,
  setMenu: (menu) => set({ menu }),
  clearMenu: () => set({ menu: null }),
}));
