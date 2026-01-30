import type { Metadata } from 'next';
import './globals.css';
import ClientLayout from '@/app/components/ClientLayout';
import StyledComponentsRegistry from '@/app/registry';
import { DialogProvider } from '@/app/components/DialogProvider';

export const metadata: Metadata = {
  title: '기억함',
  description: '인터넷 추모 공간',
  openGraph: {
    title: '기억함',
    description: '인터넷 추모 공간',
    url: '',
    images: [
      {
        url: '',
        width: 1200,
        height: 630,
        alt: '',
      },
    ],
  },
};
export default function RootLayout({
                                     children,
                                   }: Readonly<{
  children: React.ReactNode;
}>) {
  return (
      <html lang="ko">
      <body>
      <StyledComponentsRegistry>
        <DialogProvider>
          <ClientLayout>{children}</ClientLayout>
        </DialogProvider>
      </StyledComponentsRegistry>
      </body>
      </html>
  );
}
