import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { jwtDecode } from 'jwt-decode';

export default function KakaoRedirectPage() {
  const navigate = useNavigate();

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
        console.log('서버로부터 받은 사용자 정보:', decoded);
        localStorage.setItem('user', decoded);
        localStorage.setItem('token', res.data.token);
        // 토큰 저장 및 홈 이동 등
        navigate('/');
      })
      .catch(err => {
        console.error('로그인 실패', err);
      });
  }, []);

  return <div>로그인 처리 중입니다...</div>;
}
