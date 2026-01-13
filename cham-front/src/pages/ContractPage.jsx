import styled from 'styled-components';

export default function MainPage() {
  return <Wrapper>수의계약 페이지</Wrapper>;
}

const Wrapper = styled.div`
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px); /* 전체화면에서 Header 높이 제외 */

  @media ${({ theme }) => theme.device.tablet} {
     height: auto;
  }
`;
