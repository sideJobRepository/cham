import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { jwtDecode } from 'jwt-decode';
import { useSetRecoilState } from 'recoil';
import { userState } from '@/recoil/appState.js';
import { toast } from 'react-toastify';
import { tokenStore } from '@/utils/axiosInstance.js';

export default function RedirectPage() {
  const navigate = useNavigate();
  const setUser = useSetRecoilState(userState);

  useEffect(() => {
    const toastId = toast.loading('로그인 중 입니다.');

    const code = new URL(window.location.href).searchParams.get('code');
    if (!code) {
      return;
    }

    const pathname = window.location.pathname;
    const provider = pathname.split('/')[2];

    let url=null;

    if(provider === "KAKAO"){
      url = "kakao"
    }else if(provider === "NAVER"){
      url = "naver"
    }else {
      url = "google"
    }

    // 인가코드를 서버로 보내기
    axios
      .post(`/cham/${url}-login`, { code })
      .then(res => {
        console.log('res', res);

        tokenStore.set(res.data.token);
        setUser(res.data.user);

        // 토큰 저장 및 홈 이동 등
        navigate('/');
        toast.update(toastId, {
          render: '로그인에 성공하였습니다.',
          type: 'success',
          isLoading: false,
          autoClose: 1000,
        });
      })
      .catch(err => {
        toast.update(toastId, {
          render: '로그인에 실패하였습니다.',
          type: 'error',
          isLoading: false,
          autoClose: 3000,
        });
        console.error('로그인 실패', err);
        navigate('/');
      });
  }, []);

  return <></>;
}
