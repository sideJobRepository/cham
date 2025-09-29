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
            </Route>
          </Routes>
        </BrowserRouter>
      </RecoilRoot>
    </ThemeProvider>
  );
}

export default App;
