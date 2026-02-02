import { useRequest } from '@/hooks/useRequest';
import api from '@/lib/axiosInstance';
import { useCommentDataStore } from '@/store/comment';

export function useFetchCommentList() {
  const { request } = useRequest();
  const setCommentData = useCommentDataStore((state) => state.setComment);

  const fetchCommentData = (id: number) => {
    request(() => api.get(`/cham/article-reply/${id}`).then((res) => res.data), setCommentData, {
      ignoreErrorRedirect: true,
    });
  };

  return fetchCommentData;
}
