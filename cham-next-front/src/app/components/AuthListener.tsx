"use client";
import { useEffect } from "react";
import { useUserStore } from '@/store/user';
import { refreshToken } from "@/lib/axiosInstance";

export default function AuthListener() {
  const setUser = useUserStore((state) => state.setUser);

  useEffect(() => {
    const handler = (e: Event) => {
      const custom = e as CustomEvent;
      if (custom.detail?.user) {
        setUser(custom.detail.user);
      }
    };

    window.addEventListener("auth:refreshed", handler);

    // 최초 마운트 시 refresh 한 번 시도
    refreshToken().catch(() => {
      // 비로그인 상태
      console.log("비로그인 상태 체크");
    });

    return () => {
      window.removeEventListener("auth:refreshed", handler);
    };
  }, [setUser]);

  return null;
}
