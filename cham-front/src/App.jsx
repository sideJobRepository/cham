import './App.css'
import {ThemeProvider} from "styled-components";
import {theme} from "./styles/theme.js";
import {GlobalStyle} from "./styles/GlobalStyle.js";
import TopHeader from "./components/TopHeader.jsx";
import MainPage from "./pages/MainPage.jsx";
import Layout from "./components/Layout.jsx";

function App() {

  return (
    <ThemeProvider theme={theme}>
        <GlobalStyle />
        <Layout/>
    </ThemeProvider>
  )
}

export default App
