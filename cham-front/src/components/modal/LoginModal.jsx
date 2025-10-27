import styled from 'styled-components';
import logo from '/logo.png';
import google from '/logo/google.png';
import naver from '/logo/naver.png';
import kakao from '/logo/kakao.png';

export default function LoginMoadl() {
  function login(name) {
    const CLIENT_ID = import.meta.env[`VITE_${name}_CLIENT_ID`];
    const REDIRECT_URL = import.meta.env[`VITE_${name}_REDIRECT_URL`];

    let authUrl;

    if (name === 'KAKAO') {
      authUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URL}&response_type=code`;
    } else if (name === 'NAVER') {
      const STATE = crypto.randomUUID();
      authUrl = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${CLIENT_ID}&redirect_uri=${encodeURIComponent(REDIRECT_URL)}&state=${STATE}`;
    } else {
      const googleScope = 'openid email profile';
      authUrl = `https://accounts.google.com/o/oauth2/auth?client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URL}&response_type=code&scope=${googleScope}`;
    }

    window.location.href = authUrl;
  }

  return (
    <LoginModalWrapper>
      <CenterModalBox>
        <img src={logo} alt="로고" />
        <h2>맛집지도 로그인</h2>
      </CenterModalBox>
      <BottomModalBox>
        <button onClick={() => login('GOOGLE')}>
          <img src={google} alt="구글 로그인 로고" />
          구글로 계속하기
        </button>
        <button onClick={() => login('KAKAO')}>
          <img src={kakao} alt="카카오 로그인 로고" />
          카카오로 계속하기
        </button>
        <button onClick={() => login('NAVER')}>
          <img src={naver} alt="네이버 로그인 로고" />
          네이버로 계속하기
        </button>
      </BottomModalBox>
    </LoginModalWrapper>
  );
}

const LoginModalWrapper = styled.div`
  display: flex;
  flex-direction: column;
  width: 460px;
  background-color: ${({ theme }) => theme.colors.primary};
  padding: 10px 30px;
  gap: 30px;

  @media ${({ theme }) => theme.device.mobile} {
    width: 100%;
  }
`;

const CenterModalBox = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  padding: 26px 0;
  align-items: center;

  img {
    height: 126px;
    width: auto;
  }

  h2 {
    font-size: ${({ theme }) => theme.sizes.bigLarge};
    color: #ffffff;
    font-weight: bold;
  }
`;

const BottomModalBox = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 40px;

  button {
    display: flex;
    align-items: center;
    max-width: 310px;
    width: 100%;
    padding: 12px 60px;
    gap: 28px;
    background-color: transparent;
    color: #ffffff;
    border: 1px solid rgba(225, 225, 225, 0.25);
    border-radius: 80px;
    font-size: 16px;
    cursor: pointer;
    font-weight: bolder;

    img {
      width: 20px;
      height: 20px;
    }
  }
`;
