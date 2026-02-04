import { create } from 'zustand/index';

interface CommentOpenSotre {
  open: boolean;
  setOpen: (open: boolean) => void;
}

export const useCommentStore = create<CommentOpenSotre>((set) => ({
  open: false,
  setOpen: (open) => set({ open }),
}));

export interface CommentReply {
  replyId: number;
  content: string;
  memberId: number;
  memberName: string;
  isOwner: boolean;
  registDate: string;
  delStatus: boolean;
  children: CommentReply[]; // 재귀
}

export interface CommentData {
  articleId: number;
  title: string;
  legislationId?: number;
  replies: CommentReply[];
}

interface CommentDataStore {
  comment: CommentData | null;
  setComment: (comment: CommentData | null) => void;
  allComment: boolean;
  setAllComment: (allComment: boolean) => void;
}

export const useCommentDataStore = create<CommentDataStore>((set) => ({
  comment: null,
  setComment: (comment) => set({ comment }),
  allComment: false,
  setAllComment: (allComment: boolean) => set({ allComment }),
}));

interface AllCommentCount {
  count: number;
  setCount: (count: number) => void;
}

export const useAllCommentCount = create<AllCommentCount>((set) => ({
  count: 0,
  setCount: (count: number) => set({ count }),
}));
