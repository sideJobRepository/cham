import { createGlobalStyle } from 'styled-components';

export const GlobalStyle = createGlobalStyle`
    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
    body { 
        font-family: 'Pretendard', sans-serif;
        overscroll-behavior-y: none;
        -webkit-overflow-scrolling: touch;
    }
    img { max-width: 100%; display: block; }
    a { text-decoration: none; color: inherit; }
    ul, ol { list-style: none; }
`;
