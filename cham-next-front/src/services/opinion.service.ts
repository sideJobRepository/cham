import { useRequest } from '@/hooks/useRequest';
import { useCommentDataStore } from '@/store/comment';
import api from '@/lib/axiosInstance';
import { useOpinionStore } from '@/store/opinion';

export function useFetchOpinion() {
  const { request } = useRequest();
  const setOpinion = useOpinionStore((state) => state.setOpinion);

  const fetchOpinion = (id: number) => {
    request(() => api.get(`/cham/great/${id}`).then((res) => res.data), setOpinion, {
      ignoreErrorRedirect: true,
    });
  };

  return fetchOpinion;
}
