import { useUserStore } from '@/store/user';
import { useRequest } from '@/hooks/useRequest';
import api from '@/lib/axiosInstance';
import { tokenStore } from '@/services/tokenStore';
import { useDialogUtil } from '@/utils/dialog';

export function useLoginPost() {
  const { request } = useRequest();
  const setUser = useUserStore((state) => state.setUser);
  const { alert, confirm } = useDialogUtil();

  const postUser = (code: string, name: string, onSuccess?: () => void) => {
    request(
      () =>
        api.post(`/cham/next/${name}-login`, { code }).then((res) => {
          const token = res.data.token as string;
          tokenStore.set(token);

          const user = res.data.user;

          return user;
        }),
      (user) => {
        setUser(user);

        onSuccess?.();
        alert('로그인에 성공하였습니다.');
        const channel = new BroadcastChannel('auth');
        channel.postMessage({ type: 'LOGIN' });
        channel.close();
      }
    );
  };

  return { postUser };
}
