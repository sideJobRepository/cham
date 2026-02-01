import { useRequest } from '@/hooks/useRequest';
import { NoticeFiles, useNoticeDetailStore, useNoticeListStore } from '@/store/notice';
import api from '@/lib/axiosInstance';
import { useCommentDataStore } from '@/store/comment';

export function useFetchCommentList() {
  const { request } = useRequest();
  const setCommentData = useCommentDataStore((state) => state.setComment);

  const fetchCommentData = (id: number) => {
    request(() => api.get(`/cham/article-reply/${id}`).then((res) => res.data), setCommentData, {
      ignoreErrorRedirect: true,
    });
  };

  return fetchCommentData;
}

export function useFetchNoticeDetailL() {
  const { request } = useRequest();
  const setDetailNotice = useNoticeDetailStore((state) => state.setDetailNotice);

  const fetchDetailNotice = (id: string) => {
    request(() => api.get(`/bgm-agit/kml-notice/${id}`).then((res) => res.data), setDetailNotice, {
      ignoreErrorRedirect: true,
    });
  };

  return fetchDetailNotice;
}

export function useNoticeDownloadFetch() {
  const { request } = useRequest();

  const fetchNoticeDownload = (file: NoticeFiles) => {
    const sliceId = file.fileUrl.split('/').pop()!;
    const downloadUrl =
      `${process.env.NEXT_PUBLIC_API_URL}` + `/bgm-agit/download/${file.fileFolder}/${sliceId}`;

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
        api.get(downloadUrl, { responseType: 'blob' }).then((res) => {
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
      { disableLoading: true }
    );
  };

  return fetchNoticeDownload;
}
