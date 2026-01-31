'use client';
import { useEffect } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { useLoginPost } from '@/services/auth.service';


export default function RedirectPage() {

  const { postUser } = useLoginPost();

  const pathname = usePathname();
  const router = useRouter();

  useEffect(() => {
    const code = new URL(window.location.href).searchParams.get('code');

    const provider = pathname.split('/')[2];

    if (!code) {
      router.replace('/');
      return;
    }

    postUser(code, provider, () => {
      router.replace('/');
    });
  }, []);

  return null;
}
