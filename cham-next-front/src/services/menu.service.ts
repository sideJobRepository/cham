import { useRequest } from '@/hooks/useRequest';
import { useEffect } from 'react';
import api from '@/lib/axiosInstance';
import { useMenuStore } from '@/store/menu';

export function useFetchMainMenu(enabled: boolean = true) {
  const setMainMenu = useMenuStore((state) => state.setMenu);
  const { request } = useRequest();

  useEffect(() => {
    if (!enabled) return;
    request(() => api.get('/cham/legislation').then((res) => res.data), setMainMenu, {
      ignoreErrorRedirect: true,
    });
  }, [enabled, request, setMainMenu]);
}
