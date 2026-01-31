// src/styles/styled.d.ts
import 'styled-components';

declare module 'styled-components' {
  export interface DefaultTheme {
    device: {
      mobile: string;
      tablet: string;
      desktop: string;
    };
    colors: {
      whiteColor: string;
      blackColor: string;
      lineColor: string;
      border: string;
      grayColor: string;
      inputColor: string;
      writeBgColor: string;
      softColor: string;
      softColor2: string;

      sideBgColor: string;
      mainBgColor: string;
      mainPageBgColor: string;
      menuColor: string;
      subMenuColor: string;
      alertColor: string;
      loginColor: string;
      loginBtColor: string;
      fileBgColor: string;
      fileBorderColor: string;
      inputTitleBgColor: string;

      subTextBoxColor: string;
      activeMenuColor: string;
      bottomBg: string;
      subColor: string;
      purpleColor: string;
      blueColor: string;
      redColor: string;
      greenColor: string;
      basicColor: string;
      bronzeColor: string;
      yellowColor: string;
      noticeColor: string;
      navColor: string;
      labelGb: string;
      white: string;
      text: string;
      kakao: string;
      black: string;
      [key: string]: string;
    };
    desktop: {
      sizes: {
        titleSize: string;
        h1Size: string;
        h2Size: string;
        h3Size: string;
        h4Size: string;
        h5Size: string;
        xl: string;
        md: string;
        sm: string;
        xs: string;
        tree: string;
        tree2: string;
        tree3: string;
      };
    };
    mobile: {
      sizes: {
        titleSize: string;
        h1Size: string;
        h2Size: string;
        h3Size: string;
        h4Size: string;
        h5Size: string;
        xl: string;
        md: string;
        sm: string;
        xs: string;
        tree: string;
        tree2: string;
        tree3: string;
      };
    };
    weight: {
      bold: string;
      semiBold: string;
    };
  }
}
