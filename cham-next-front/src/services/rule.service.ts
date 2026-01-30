import { useRequest } from '@/hooks/useRequest';
import { useNoticeListStore } from '@/store/notice';
import api from '@/lib/axiosInstance';
import { useRuleStore } from '@/store/rule';

export function useFetchRule() {
  const { request } = useRequest();
  const setRule = useRuleStore((state) => state.setRule);

  const fetchRule = () => {
    request(() => api.get(`/bgm-agit/rule`).then(res => res.data), setRule,  {ignoreErrorRedirect: true});
  };

  return fetchRule;
}
