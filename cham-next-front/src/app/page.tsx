import HomeClient from '@/app/components/HomeClient';
import { fetchMenuDataOrThrow, getDefaultArticles } from '@/lib/server/menu';

export default async function Page() {
  const menuData = await fetchMenuDataOrThrow();
  const initialArticles = getDefaultArticles(menuData);

  return <HomeClient initialArticles={initialArticles} />;
}
