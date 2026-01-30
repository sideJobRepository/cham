import { useRequest } from '@/hooks/useRequest';
import { useNoticeListStore } from '@/store/notice';
import api from '@/lib/axiosInstance';
import { useRecordUserStore } from '@/store/user';
import { useYakumanStore } from '@/store/record';

export function useFetchRecordUser() {
  const { request } = useRequest();
  const setRecordUser = useRecordUserStore((state) => state.setRecordUser);

  const fetchRecordUser = () => {
    request(() => api.get(`/bgm-agit/mahjong-members`).then(res => res.data), setRecordUser,  {ignoreErrorRedirect: true});
  };

  return fetchRecordUser;
}

export function useFetchYakuman() {
  const { request } = useRequest();
  const setYakuman = useYakumanStore((state) => state.setYakuman);

  const fetchYakuman = () => {
    request(() => api.get(`/bgm-agit/yakumanType`).then(res => res.data), setYakuman,  {ignoreErrorRedirect: true});
  };

  return fetchYakuman;
}