import type { Metadata } from 'next';
import './globals.css';
import ClientLayout from '@/app/components/ClientLayout';
import StyledComponentsRegistry from '@/app/registry';
import { DialogProvider } from '@/app/components/DialogProvider';
import { fetchMenuDataOrThrow } from '@/lib/server/menu';

export const metadata: Metadata = {
  metadataBase: new URL('https://cham-monimap.com'),
  title: '충남대전통합 특별법(안) 공개 시민의 의견을 말해주세요.',
  description: '대전참여자치시민연대 법령 의견수렴 서비스',
  openGraph: {
    title: '충남대전통합 특별법(안) 공개 시민의 의견을 말해주세요.',
    description:
      '공개된 여당의 광역행정통합 법안을 직접 확인하고, 각 조항별로 의견을 더해주세요. 여러분이 남긴 의견을 모아 전달하겠습니다.',
    url: 'https://cham-monimap.com/feedback',
    images: [
      {
        url: 'https://cham-monimap.com/feedback/og.png',
        width: 1200,
        height: 630,
        alt: '대전참여자치시민연대',
      },
    ],
  },
  twitter: {
    card: 'summary_large_image',
    title: '충남대전통합 특별법(안) 공개 시민의 의견을 말해주세요.',
    description:
      '공개된 여당의 광역행정통합 법안을 직접 확인하고, 각 조항별로 의견을 더해주세요. 여러분이 남긴 의견을 모아 전달하겠습니다.',
    images: ['https://cham-monimap.com/feedback/og.png'],
  },
};

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const menuData = await fetchMenuDataOrThrow();

  return (
    <html lang="ko">
      <body>
        <StyledComponentsRegistry>
          <DialogProvider>
            <ClientLayout initialMenuData={menuData}>{children}</ClientLayout>
          </DialogProvider>
        </StyledComponentsRegistry>
      </body>
    </html>
  );
}
