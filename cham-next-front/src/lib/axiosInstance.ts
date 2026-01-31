// utils/axiosInstance.ts
import axios, { AxiosError, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios';
import { tokenStore } from '@/services/tokenStore';

interface AuthAxiosRequestConfig extends InternalAxiosRequestConfig {
  __hadAuth?: boolean;
}

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL, //env의 API URL
  timeout: 15000,
  withCredentials: true,
});

let refreshing: Promise<string | null> | null = null;

//토큰 재발급
export async function refreshToken(): Promise<string | null> {
  try {
    const { data } = await axios.post('/cham/refresh', null, {
      baseURL: api.defaults.baseURL,
      withCredentials: true,
    });
    const newToken = data?.token ?? null;

    if (!newToken) {
      localStorage.removeItem('login');
    }

    tokenStore.set(newToken);
    window.dispatchEvent(new CustomEvent('auth:refreshed', { detail: { user: data?.user } }));

    return newToken;
  } catch (e) {
    console.error(e);
    localStorage.removeItem('login');
    return null;
  } finally {
    refreshing = null; // 락 해제
  }
}

api.interceptors.request.use(async (config: AuthAxiosRequestConfig) => {
  if (config.url?.includes('/cham/refresh')) return config; //api 수정 예정

  let token = tokenStore.get();
  if (!token) {
    if (!refreshing) refreshing = refreshToken(); // 첫 호출만 실제 실행
    token = await refreshing; // 모든 요칭이 같은 Promise를 기다림
  }

  config.__hadAuth = !!token;
  if (token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let isRefreshing = false;
let waiters: Array<(t: string) => void> = [];
const addWaiter = (cb: (t: string) => void) => waiters.push(cb);
const notifyAll = (t: string) => {
  waiters.forEach((cb) => cb(t));
  waiters = [];
};

api.interceptors.response.use(
  (r) => r,
  async (error: AxiosError) => {
    const original = error.config as AxiosRequestConfig & {
      __isRetryRequest?: boolean;
      __hadAuth?: boolean;
    };

    if (!error.response) return Promise.reject(error);
    const status = error.response.status;

    // refresh 자체 실패면 중단
    if (original?.url?.includes('/cham/refresh')) {
      //api 수정 예정
      return Promise.reject(error);
    }

    // 401만 처리 + 이미 재시도했거나 애초에 인증 없이 간 요청이면 패스
    if (status !== 401 || original?.__isRetryRequest || !original?.__hadAuth) {
      return Promise.reject(error);
    }

    // 1) 현재 요청을 먼저 재시도 큐에 등록
    const retryPromise = new Promise((resolve, reject) => {
      addWaiter((token: string) => {
        try {
          original.__isRetryRequest = true;
          original.headers = original.headers ?? {};
          original.headers.Authorization = `Bearer ${token}`;
          resolve(api(original)); // 원요청 재호출
        } catch (e) {
          reject(e);
        }
      });
    });

    // 2) 그리고 나서 refresh를 시작(이미 진행 중이면 건너뜀). await 금지
    if (!isRefreshing) {
      isRefreshing = true;
      axios
        .post('/cham/refresh', null, {
          //api 구조 변경 필요
          baseURL: api.defaults.baseURL,
          withCredentials: true,
        })
        .then(({ data }) => {
          const newToken = data?.token;
          if (!newToken) throw new Error('No access token from refresh');
          tokenStore.set(newToken);
          notifyAll(newToken); // 등록된 모든 대기 요청 재시도
        })
        .catch((e) => {
          waiters = []; // 실패 시 대기열 비움
          // 토큰 삭제
          tokenStore.clear();
        })
        .finally(() => {
          isRefreshing = false;
        });
    }

    // 3) 내 요청은 큐에서 깨울 때까지 기다렸다가 재시도
    return retryPromise;
  }
);

export default api;
