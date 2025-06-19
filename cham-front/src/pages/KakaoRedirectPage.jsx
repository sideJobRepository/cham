import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { jwtDecode } from 'jwt-decode';
import { useSetRecoilState } from 'recoil';
import { userState } from '@/recoil/appState.js';

export default function KakaoRedirectPage() {
  const navigate = useNavigate();
  const setUser = useSetRecoilState(userState);

  useEffect(() => {
    const code = new URL(window.location.href).searchParams.get('code');
    if (!code) {
      alert('카카오 로그인 실패: 인가코드 없음');
      return;
    }

    // 인가코드를 서버로 보내기
    axios
      .post('/cham/kakao-login', { code })
      .then(res => {
        const decoded = jwtDecode(res.data.token);

        localStorage.setItem('user', JSON.stringify(decoded));
        console.log('decoded', decoded);
        localStorage.setItem('token', res.data.token);
        setUser(decoded);
        // 토큰 저장 및 홈 이동 등
        navigate('/');
      })
      .catch(err => {
        console.error('로그인 실패', err);
      });
  }, []);

  return <></>;
}
