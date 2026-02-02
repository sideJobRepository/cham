import 'server-only';

import type { Article, MenuData } from '@/store/menu';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL;

export async function fetchMenuData(): Promise<MenuData | null> {
  if (!API_BASE_URL) return null;

  try {
    const response = await fetch(`${API_BASE_URL}/cham/legislation`, {
      next: { revalidate: 300 },
    });
    if (!response.ok) return null;
    return (await response.json()) as MenuData;
  } catch {
    return null;
  }
}

export async function fetchMenuDataOrThrow(): Promise<MenuData> {
  if (!API_BASE_URL) {
    throw new Error('API base URL is not configured.');
  }

  try {
    const response = await fetch(`${API_BASE_URL}/cham/legislation`, {
      next: { revalidate: 300 },
    });
    if (!response.ok) {
      throw new Error(`Failed to fetch legislation menu: ${response.status}`);
    }

    return (await response.json()) as MenuData;
  } catch {
    throw new Error('Failed to fetch legislation menu.');
  }
}

function uniqById(list: Article[]) {
  const map = new Map<number, Article>();
  for (const article of list) map.set(article.articleId, article);
  return [...map.values()];
}

export function getDefaultArticles(menuData: MenuData | null): Article[] {
  const firstLaw = menuData?.legislations?.[0];
  const firstPart = firstLaw?.parts?.[0];
  if (!firstPart) return [];

  const list =
    firstPart.chapters?.flatMap((ch) =>
      (ch.sections ?? []).flatMap((sec) => sec.articles ?? [])
    ) ?? [];

  return uniqById(list);
}
