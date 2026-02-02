import { useRequest } from '@/hooks/useRequest';
import api from '@/lib/axiosInstance';
import { useSearchDataStore } from '@/store/search';

export function useFetchSerach() {
  const { request } = useRequest();
  const setSearch = useSearchDataStore((state) => state.setSearch);

  const fetchSearch = (searchKeyword: string) => {
    request(
      () =>
        api
          .get(`/cham/legislation/search`, {
            params: { keyword: searchKeyword },
          })
          .then((res) => res.data),
      setSearch,
      {
        ignoreErrorRedirect: true,
      }
    );
  };

  return fetchSearch;
}
