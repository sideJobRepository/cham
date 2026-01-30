import { useRequest } from '@/hooks/useRequest';
import api from '@/lib/axiosInstance';
import { AxiosRequestHeaders } from 'axios';

interface InsertOptions<T> {
  url: string;
  headers?: AxiosRequestHeaders;
  body: T;
  onSuccess?: (data: T) => void;
  ignoreErrorRedirect?: boolean;
}


export function useInsertPost() {
  const { request } = useRequest();

  const insert = <T>({ url, body, onSuccess, ignoreErrorRedirect, headers }: InsertOptions<T>) => {
    request(
      () => api.post<T>(url, body, { headers }).then(res => res.data),
      data => onSuccess?.(data),
      { ignoreErrorRedirect }
    );
  };

  return { insert };
}

export function useUpdatePost() {
  const { request } = useRequest();

  const update = <T>({ url, body, onSuccess, ignoreErrorRedirect, headers }: InsertOptions<T>) => {
    request(
      () => api.put<T>(url, body, { headers }).then(res => res.data),
      data => onSuccess?.(data),
      { ignoreErrorRedirect }
    );
  };

  return { update };
}

export function useDeletePost() {
  const { request } = useRequest();

  const remove = <T>({
                       url,
                       onSuccess,
                       ignoreErrorRedirect,
                       headers,
                     }: Omit<InsertOptions<T>, 'body'>) => {
    request(
      () => api.delete<T>(url, { headers }).then(res => res.data),
      data => onSuccess?.(data),
      { ignoreErrorRedirect }
    );
  };

  return { remove };
}