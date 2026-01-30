import { useRequest } from '@/hooks/useRequest';
import { NoticeFiles, useNoticeDetailStore, useNoticeListStore } from '@/store/notice';
import api from '@/lib/axiosInstance';

export type params = {
  page?: number;
  titleAndCont?: string;
};

export function useFetchNoticeList() {
  const { request } = useRequest();
  const setNotice = useNoticeListStore((state) => state.setNotice);

  const fetchNotice = (params: params) => {
    request(() => api.get(`/bgm-agit/kml-notice?size=5`, { params }).then(res => res.data), setNotice,  {ignoreErrorRedirect: true});
  };

  return fetchNotice;
}

export function useFetchNoticeDetailL() {
  const { request } = useRequest();
  const setDetailNotice = useNoticeDetailStore((state) => state.setDetailNotice);

  const fetchDetailNotice = (id: string) => {
    request(() => api.get(`/bgm-agit/kml-notice/${id}`).then(res => res.data), setDetailNotice, {ignoreErrorRedirect: true});
  };

  return fetchDetailNotice;
}

export function useNoticeDownloadFetch() {
  const { request } = useRequest();

  const fetchNoticeDownload = (file: NoticeFiles) => {
    const sliceId = file.fileUrl.split('/').pop()!;
    const downloadUrl =
      `${process.env.NEXT_PUBLIC_API_URL}` +
      `/bgm-agit/download/${file.fileFolder}/${sliceId}`;

    const isIOS =
      /iP(hone|od|ad)/.test(navigator.userAgent) ||
      (navigator.userAgent.includes('Macintosh') && 'ontouchend' in document);

    // iOS는 Blob 절대 금지
    if (isIOS) {
      window.open(downloadUrl, '_blank');
      return;
    }

    // PC / Android만 Blob 다운로드
    request(
      () =>
        api.get(downloadUrl, { responseType: 'blob' }).then(res => {
          const blob = new Blob([res.data], {
            type: res.headers['content-type'],
          });

          let fileName = 'download.bin';
          const disposition = res.headers['content-disposition'];
          if (disposition) {
            const rfc = disposition.match(/filename\*=UTF-8''(.+)/);
            if (rfc?.[1]) fileName = decodeURIComponent(rfc[1]);
          }

          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = fileName;
          a.click();
          URL.revokeObjectURL(url);
        }),
      undefined,
      { disableLoading: true },
    );
  };

  return fetchNoticeDownload;
}
