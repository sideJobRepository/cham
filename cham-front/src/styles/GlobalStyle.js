import { createGlobalStyle } from 'styled-components';

export const GlobalStyle = createGlobalStyle`
    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
    body { 
        font-family: 'Pretendard', sans-serif; 
        background: ${({ theme }) => theme.colors.bg}; 
        color: ${({ theme }) => theme.colors.text};
    }
    img { max-width: 100%; display: block; }
    a { text-decoration: none; color: inherit; }
    ul, ol { list-style: none; }
`;
