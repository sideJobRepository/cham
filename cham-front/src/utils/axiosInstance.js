import axios from 'axios';

let accessToken = null;

export const tokenStore = {
  get() {
    return accessToken;
  },
  set(token) {
    accessToken = token;
  },
  clear() {
    accessToken = null;
  },
};

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL, // 환경변수로 설정
  timeout: 600000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

//로그인 로직 수정

let refreshing = null;

async function refreshToken() {
  try {
    const { data } = await axios.post('/cham/refresh', null, {
      baseURL: api.defaults.baseURL,
      withCredentials: true,
    });
    const newToken = data?.token ?? null;
    tokenStore.set(newToken);
    window.dispatchEvent(new CustomEvent('auth:refreshed', { detail: { user: data?.user } }));
    return newToken;
  } catch (e) {
    console.error(e);
    return null;
  } finally {
    refreshing = null; // 락 해제
  }
}

api.interceptors.request.use(async config => {
  if (config.url?.includes('/cham/refresh')) return config;

  let token = tokenStore.get();
  if (!token) {
    if (!refreshing) refreshing = refreshToken(); // 첫 호출만 실제 실행
    token = await refreshing; // 모두 같은 Promise를 기다림
  }

  config.__hadAuth = !!token;
  if (token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let isRefreshing = false;
let waiters = [];
const addWaiter = cb => waiters.push(cb);
const notifyAll = t => {
  waiters.forEach(cb => cb(t));
  waiters = [];
};

api.interceptors.response.use(
  r => r,
  async error => {
    const original =
      error.config &
      {
        __isRetryRequest: false,
        __hadAuth: false,
      };

    if (!error.response) return Promise.reject(error);
    const status = error.response.status;

    // refresh 자체 실패면 중단
    if (original?.url?.includes('/cham/refresh')) {
      return Promise.reject(error);
    }

    // 401만 처리 + 이미 재시도했거나 애초에 인증 없이 간 요청이면 패스
    if (status !== 401 || original?.__isRetryRequest || !original?.__hadAuth) {
      return Promise.reject(error);
    }

    // 1) 현재 요청을 먼저 재시도 큐에 등록
    const retryPromise = new Promise((resolve, reject) => {
      addWaiter(token => {
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

    // 2) 그리고 나서 refresh를 시작(이미 진행 중이면 건너뜀). 절대 await 하지 마세요.
    if (!isRefreshing) {
      isRefreshing = true;
      axios
        .post('/cham/refresh', null, {
          baseURL: api.defaults.baseURL,
          withCredentials: true,
        })
        .then(({ data }) => {
          const newToken = data?.token;
          if (!newToken) throw new Error('No access token from refresh');
          tokenStore.set(newToken);
          notifyAll(newToken); // ← 등록된 모든 대기 요청 재시도
        })
        .catch(e => {
          waiters = []; // 실패 시 대기열 비움
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
