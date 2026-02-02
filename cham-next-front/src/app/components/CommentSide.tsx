'use client';
import { PencilSimpleLine, X, FileText, TrashSimple, Check } from 'phosphor-react';
import React, { useEffect, useState } from 'react';
import styled from 'styled-components';
import { useCommentDataStore, useCommentStore } from '@/store/comment';
import { usePathname, useRouter } from 'next/navigation';
import { useDeletePost, useInsertPost, useUpdatePost } from '@/services/main.service';
import { useDialogUtil } from '@/utils/dialog';
import { useFetchCommentList } from '@/services/comment.service';
import { useUserStore } from '@/store/user';

type InsertBody = {
  articleId: number;
  parentReplyId: number | null;
  content: string;
};

type UpdateBody = {
  replyId: number;
  editContent: string;
};

export default function CommentSide() {
  const router = useRouter();
  const pathname = usePathname();

  const { insert } = useInsertPost();
  const { update } = useUpdatePost();
  const { remove } = useDeletePost();

  const { alert, confirm } = useDialogUtil();

  //유저
  const user = useUserStore((state) => state.user);

  //댓글 조회
  const fetchCommentList = useFetchCommentList();

  //comment
  const commentOpen = useCommentStore((state) => state.open);
  const setCommentOpen = useCommentStore((state) => state.setOpen);
  const commentData = useCommentDataStore((state) => state.comment);
  console.log('commentData', commentData);

  const [content, setContent] = useState('');

  //수정
  const [editContent, setEditContent] = useState('');
  const [editingReplyId, setEditingReplyId] = useState<string | null>(null);

  const handleEditClick = (reply: any) => {
    setEditingReplyId(reply.replyId.toString());
    setEditContent(reply.content);
  };

  const buildInsertBody = (): InsertBody => ({
    articleId: commentData?.articleId!,
    parentReplyId: null,
    content,
  });

  const buildUpdateBody = (replyId: number): { replyId: number; content: string } => ({
    replyId,
    content: editContent,
  });

  const handleSubmit = async (replyId?: number) => {
    if (!user) {
      const ok = await confirm('로그인 후 작성 가능합니다.', '로그인 페이지로 이동하시겠습니까?');
      if (!ok) return;
      router.push('/login');
      return;
    }

    if (!replyId && !content.trim()) {
      alert('의견을 입력해주세요.');
      return;
    } else if (replyId && !editContent.trim()) {
      alert('수정할 의견을 입력해주세요.');
      return;
    }

    const isEdit = Boolean(replyId);

    const ok2 = await confirm(isEdit ? '의견을 수정하시겠습니까?' : '의견을 저장하시겠습니까?');
    if (!ok2) return;

    const requestFn = isEdit ? update : insert;
    const body = isEdit ? buildUpdateBody(replyId!) : buildInsertBody();

    requestFn({
      url: '/cham/article-reply',
      body,
      ignoreErrorRedirect: true,
      onSuccess: () => {
        if (replyId) {
          alert('의견이 수정되었습니다.');
          setEditContent('');
          setEditingReplyId(null);
        } else {
          alert('의견이 저장되었습니다.');
          setContent('');
        }

        fetchCommentList(commentData?.articleId!);
      },
    });
  };

  //삭제
  const deleteData = async (replyId: number) => {
    const ok = await confirm('의견을 삭제하시겠습니까?');
    if (!ok) return;

    remove({
      url: `/cham/article-reply/${replyId}`,
      ignoreErrorRedirect: true,
      onSuccess: async () => {
        alert('의견이 삭제되었습니다.');
        fetchCommentList(commentData?.articleId!);
      },
    });
  };

  //이름 *
  const maskNameMiddle = (name?: string | null) => {
    const n = (name ?? '').trim();
    if (!n) return '';

    // 1글자면 그대로
    if (n.length === 1) return n;

    // 2글자면 뒤만 마스킹: 박지 -> 박*
    if (n.length === 2) return `${n[0]}*`;

    // 3글자 이상: 가운데(들) 마스킹: 박지수 -> 박*수 / 홍길동 -> 홍*동 / 김철수민 -> 김**민
    const first = n[0];
    const last = n[n.length - 1];
    const stars = '*'.repeat(n.length - 2);
    return `${first}${stars}${last}`;
  };

  //로그인시 이동 처리
  useEffect(() => {
    if (pathname === '/login') setCommentOpen(false);
  }, [pathname]);

  useEffect(() => {
    if (!commentOpen) {
      setEditingReplyId(null);
      setEditContent('');
      setContent('');
    }
  }, [commentOpen]);
  return (
    <CommentMenu $open={commentOpen}>
      <CommentTopBox>
        <h4>{commentData?.title}</h4>
        <X onClick={() => setCommentOpen(false)} />
      </CommentTopBox>

      {commentData?.replies.length === 0 && <NoSearch>등록된 의견이 없습니다.</NoSearch>}

      <ul>
        {commentData?.replies.map((reply) => (
          <CommentItem key={reply.replyId}>
            <CommentHeader>
              <strong>{maskNameMiddle(reply.memberName)}</strong>
              <span>{reply.registDate}</span>
            </CommentHeader>

            {editingReplyId === reply.replyId.toString() ? (
              <>
                <textarea
                  value={editContent}
                  onChange={(e) => setEditContent(e.target.value)}
                  autoFocus
                />
              </>
            ) : (
              <CommentContent>{reply.content}</CommentContent>
            )}

            {reply.isOwner &&
              (editingReplyId === reply.replyId.toString() ? (
                <EditBox>
                  <EditButton color="#4A90E2">
                    <Check onClick={() => handleSubmit(reply.replyId)} weight="bold" />
                  </EditButton>
                  <EditButton color="#757575">
                    <X
                      weight="bold"
                      onClick={() => {
                        setEditingReplyId(null);
                        setEditContent('');
                      }}
                    />
                  </EditButton>
                </EditBox>
              ) : (
                <EditBox>
                  <EditButton color="#415B9C">
                    <FileText onClick={() => handleEditClick(reply)} weight="bold" />
                  </EditButton>
                  <EditButton onClick={() => deleteData(reply.replyId)} color="#D9625E">
                    <TrashSimple weight="bold" />
                  </EditButton>
                </EditBox>
              ))}
          </CommentItem>
        ))}
      </ul>
      <TextBox>
        <ButtonBox>
          <h5>∙ 의견 남기기</h5>
          <Button onClick={() => handleSubmit()}>
            <PencilSimpleLine weight="bold" />
          </Button>
        </ButtonBox>
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="의견을 입력하세요."
        />
      </TextBox>
    </CommentMenu>
  );
}

const CommentMenu = styled.div<{ $open: boolean }>`
  position: fixed;
  top: 80px;
  right: 0;
  display: flex;
  flex-direction: column;
  overflow: auto;
  width: 50%;
  max-width: 300px;
  height: calc(100vh - 80px);
  // border-top: 1px solid ${({ theme }) => theme.colors.lineColor};
  background-color: ${({ theme }) => theme.colors.softColor};
  transform: ${({ $open }) => ($open ? 'translateX(0)' : 'translateX(100%)')};
  opacity: ${({ $open }) => ($open ? 0.96 : 0)};
  pointer-events: ${({ $open }) => ($open ? 'auto' : 'none')};
  transition:
    transform 0.3s ease,
    opacity 0.3s ease;
  align-items: center;

  ul {
    background-color: ${({ theme }) => theme.colors.whiteColor};
    border-radius: 4px;
    flex: 1;
    width: 90%;
    overflow-y: auto;
    padding: 16px 12px;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  textarea {
    resize: none;
    height: 100px;
    padding: 8px 12px;
  }
`;

const CommentTopBox = styled.div`
  display: flex;
  width: 100%;
  background-color: ${({ theme }) => theme.colors.blueColor};
  text-align: center;
  padding: 12px 24px;
  justify-content: space-between;
  color: ${({ theme }) => theme.colors.whiteColor};
  align-items: center;
  font-size: ${({ theme }) => theme.desktop.sizes.h4Size};
  font-weight: 600;
  margin-bottom: 12px;

  svg {
    text-align: right;
    cursor: pointer;
  }

  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.mobile.sizes.h4Size};
  }

  h4 {
    width: 80%;
    text-align: left;
  }
`;

const TextBox = styled.div`
  display: flex;
  width: 100%;
  flex-direction: column;
  padding: 24px 16px;
  background-color: ${({ theme }) => theme.colors.softColor2};
  margin-top: 12px;
`;
const ButtonBox = styled.div`
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  justify-content: space-between;
  align-items: center;

  > h5 {
    color: ${({ theme }) => theme.colors.blueColor};
    font-size: ${({ theme }) => theme.desktop.sizes.h5Size};
    font-weight: 800;

    @media ${({ theme }) => theme.device.mobile} {
      font-size: ${({ theme }) => theme.mobile.sizes.h5Size};
    }
  }
`;

const Button = styled.button`
  display: flex;
  align-items: center;
  padding: 6px;
  background-color: ${({ theme }) => theme.colors.writeBgColor};
  color: ${({ theme }) => theme.colors.whiteColor};
  font-size: ${({ theme }) => theme.desktop.sizes.sm};
  border: none;
  border-radius: 999px;
  cursor: pointer;
  box-shadow: 2px 4px 2px rgba(0, 0, 0, 0.2);

  &:hover {
    opacity: 0.8;
  }

  svg {
    width: 16px;
    height: 16px;
  }
`;

const CommentItem = styled.li`
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid ${({ theme }) => theme.colors.lineColor};
`;

const CommentHeader = styled.div`
  display: flex;
  justify-content: space-between;
  font-size: ${({ theme }) => theme.desktop.sizes.sm};
  color: ${({ theme }) => theme.colors.inputColor};

  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.mobile.sizes.sm};
  }

  strong {
    color: ${({ theme }) => theme.colors.blackColor};
    font-weight: 700;

    font-size: ${({ theme }) => theme.desktop.sizes.md};
    color: ${({ theme }) => theme.colors.inputColor};

    @media ${({ theme }) => theme.device.mobile} {
      font-size: ${({ theme }) => theme.mobile.sizes.md};
    }
  }
`;

const CommentContent = styled.p`
  white-space: pre-line;
  font-size: ${({ theme }) => theme.desktop.sizes.xl};
  color: ${({ theme }) => theme.colors.inputColor};

  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.mobile.sizes.xl};
  }
`;

const EditBox = styled.div`
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex: 1;
`;

const EditButton = styled.button<{ color: string }>`
  display: flex;
  align-items: center;
  padding: 4px;
  background-color: ${({ color }) => color};
  color: ${({ theme }) => theme.colors.white};
  font-size: ${({ theme }) => theme.desktop.sizes.md};
  border: none;
  border-radius: 999px;
  cursor: pointer;
  white-space: nowrap;
  box-shadow: 2px 4px 2px rgba(0, 0, 0, 0.2);
  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.mobile.sizes.md};
  }

  svg {
    width: 12px;
    height: 12px;
  }

  &:hover {
    opacity: 0.8;
  }
`;

const NoSearch = styled.h5`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: ${({ theme }) => theme.desktop.sizes.h5Size};
  color: ${({ theme }) => theme.colors.blackColor};
  font-weight: 600;
  padding: 24px 4px;

  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.mobile.sizes.h5Size};
  }
`;
