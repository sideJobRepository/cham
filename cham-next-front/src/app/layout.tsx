import type { Metadata } from 'next';
import './globals.css';
import ClientLayout from '@/app/components/ClientLayout';
import StyledComponentsRegistry from '@/app/registry';
import { DialogProvider } from '@/app/components/DialogProvider';

export const metadata: Metadata = {
  title: '대전참여자치시민연대',
  description: '법령',
  openGraph: {
    title: '대전참여자치시민연대',
    description: '법령',
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
