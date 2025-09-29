import Modal from "@/components/modal/Modal.jsx";
import styled from "styled-components";
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
            <h2>간편 로그인</h2>
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
    )

}


const LoginModalWrapper = styled.div`
  display: flex;
  flex-direction: column;
  width: 460px;
  height: 500px;
  padding: 10px 20px;
  gap: 30px;

  @media ${({ theme }) => theme.device.mobile} {
    width: 90%;
    height: 533px;
  }
`;

const CenterModalBox = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  padding: 26px 0;
  align-items: center;

  img {
      height: 100px;
      width: auto;
  }

  h2 {
    font-size: ${({ theme }) => theme.sizes.large};
    color: ${({ theme }) => theme.colors.primary};
    font-weight: bold;
  }
`;

const BottomModalBox = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;

  button {
    display: flex;
    align-items: center;
    max-width: 310px;
    width: 100%;
    padding: 12px 60px;
    gap: 34px;
    background-color: transparent;
    color: ${({ theme }) => theme.colors.primary};
    border: 2px solid ${({ theme }) => theme.colors.primary};
    border-radius: 80px;
    font-size: ${({ theme }) => theme.sizes.medium};
    cursor: pointer;
      font-weight: bolder;
      
    img {
      width: 20px;
      height: 20px;
    }
  }
`;