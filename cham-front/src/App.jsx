import './App.css';
import { ThemeProvider } from 'styled-components';
import { theme } from './styles/theme.js';
import { GlobalStyle } from './styles/GlobalStyle.js';
import MainPage from './pages/MainPage.jsx';
import ContractPage from './pages/ContractPage.jsx';
import Layout from './components/Layout.jsx';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import DetailPage from './pages/DetailPage.jsx';
import { RecoilRoot } from 'recoil';
import RedirectPage from '@/pages/RedirectPage.jsx';
import AdminUploadPage from '@/pages/AdminUploadPage.jsx';
import { ToastContainer } from 'react-toastify';

function App() {
  return (
    <ThemeProvider theme={theme}>
      <GlobalStyle />
      <RecoilRoot>
        <ToastContainer position="top-center" autoClose={3000} />
        <BrowserRouter>
          <Routes>
            <Route path="/oauth/:provider/callback" element={<RedirectPage />} />
            <Route path="/" element={<Layout />}>
              <Route index element={<MainPage />} />
              <Route path="/contract" element={<ContractPage />} />
              <Route path="/detail" element={<DetailPage />} />
              {/* 관리자 공개관리. 화면에서도 서버에서도 ROLE_ADMIN 만 통과한다 */}
              <Route path="/admin/uploads" element={<AdminUploadPage />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </RecoilRoot>
    </ThemeProvider>
  );
}

export default App;
