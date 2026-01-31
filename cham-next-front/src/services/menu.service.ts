import { useRequest } from '@/hooks/useRequest';
import { useEffect } from 'react';
import api from '@/lib/axiosInstance';
import { useMenuStore } from '@/store/menu';

export function useFetchMainMenu() {
  const setMainMenu = useMenuStore((state) => state.setMenu);
  const { request } = useRequest();

  useEffect(() => {
    request(() => api.get('/cham/legislation').then((res) => res.data), setMainMenu, {
      ignoreErrorRedirect: true,
    });
  }, []);
}
