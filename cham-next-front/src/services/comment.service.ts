import { useRequest } from '@/hooks/useRequest';
import api from '@/lib/axiosInstance';
import { useAllCommentCount, useCommentDataStore } from '@/store/comment';

export function useFetchCommentList() {
  const { request } = useRequest();
  const { setComment, setAllComment } = useCommentDataStore();

  const fetchCommentData = (id: number) => {
    request(
      () => api.get(`/cham/article-reply/${id}`).then((res) => res.data),
      (data) => {
        setComment(data);
        setAllComment(false);
      },
      {
        ignoreErrorRedirect: true,
      }
    );
  };

  return fetchCommentData;
}

export function useFetchAllCommentList() {
  const { request } = useRequest();
  const { setComment, setAllComment } = useCommentDataStore();

  const fetchAllCommentData = (id: number) => {
    request(
      () => api.get(`/cham/legislation/${id}`).then((res) => res.data),
      (data) => {
        setComment(data);
        setAllComment(true);
      },
      {
        ignoreErrorRedirect: true,
      }
    );
  };

  return fetchAllCommentData;
}

export function useFetchAllCountCommentList() {
  const { request } = useRequest();
  const setCount = useAllCommentCount((state) => state.setCount);

  const fetchAllCommentCount = (id: number) => {
    request(() => api.get(`/cham/legislation/count/${id}`).then((res) => res.data), setCount, {
      ignoreErrorRedirect: true,
    });
  };

  return fetchAllCommentCount;
}
