import { create } from 'zustand';

export interface NoticeItem {
  id: number;
  title: string;
  cont: string;
  registDate: string;
}

export interface NoticePage {
  content: NoticeItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface NoticeFiles {
  id: number;
  fileName: string;
  fileUrl: string;
  status: string;
  fileFolder: string;
}

interface DetailNoticeStore {
  cont: string;
  files: NoticeFiles[]
  id: number;
  title: string;
  registDate: string;
}

interface NoticeStore {
  notice: NoticePage | null
  setNotice: (notice: NoticePage) => void;
}


export const useNoticeListStore = create<NoticeStore>((set) => ({
  notice: null,
  setNotice: (notice) => set({notice}),
}))

interface NoticeDetailStore {
  noticeDetail: DetailNoticeStore | null
  setDetailNotice: (noticeDetail: DetailNoticeStore) => void;
  clearDetail: () => void;
}

export const useNoticeDetailStore = create<NoticeDetailStore>((set) => ({
  noticeDetail: null,
  setDetailNotice: (noticeDetail) => set({noticeDetail}),
  clearDetail: () => set({noticeDetail: null}),
}))