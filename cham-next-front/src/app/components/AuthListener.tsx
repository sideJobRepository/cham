'use client';
import { useEffect } from 'react';
import { useUserStore } from '@/store/user';
import { refreshToken } from '@/lib/axiosInstance';

export default function AuthListener() {
  const setUser = useUserStore((state) => state.setUser);

  useEffect(() => {
    /* 같은 탭용 */
    const handler = (e: Event) => {
      const custom = e as CustomEvent;
      if (custom.detail?.user) {
        setUser(custom.detail.user);
      }
    };

    window.addEventListener('auth:refreshed', handler);

    /* 다른 탭용 (필수 추가) */
    const channel = new BroadcastChannel('auth');

    channel.onmessage = (e) => {
      if (e.data?.type === 'LOGOUT') {
        setUser(null);
      }

      if (e.data?.type === 'LOGIN') {
        refreshToken().catch(() => setUser(null));
      }
    };

    /* 최초 1회 */
    refreshToken().catch(() => {
      console.log('비로그인 상태 체크');
    });

    return () => {
      window.removeEventListener('auth:refreshed', handler);
      channel.close();
    };
  }, [setUser]);

  return null;
}
