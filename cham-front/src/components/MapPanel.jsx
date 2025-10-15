import { useEffect, useState } from 'react';
import styled, { useTheme } from 'styled-components';
import { useSearchMapState } from '@/recoil/useAppState.js';
import { useRecoilValue, useSetRecoilState } from 'recoil';
import { mapCenterAddrState, mapSearchFilterState, userState } from '@/recoil/appState.js';
import Modal from '@/components/modal/Modal.jsx';
import DetailPage from '@/pages/DetailPage.jsx';
import { FiSearch } from 'react-icons/fi';

/** ====== 줌 기준 (카카오: 값이 작을수록 더 확대됨) ====== */
const DETAIL_MAX_LEVEL = 5; // ≤5  : 상세(가맹점별)
const DONG_MAX_LEVEL_MIN = 6; // 6~8 : 동 집계
const DONG_MAX_LEVEL_MAX = 8;
const GU_LEVEL_MIN = 9; // 9~10: 구 집계
const GU_LEVEL_MAX = 10;
const SIDO_LEVEL_MIN = 11; // ≥11 : 시/도 집계

/** z-index 규칙 */
const Z_BASE = 1;
const Z_HOVER = 50000; // 일반 hover
const Z_HIT = 100000; // 검색 히트(고정 최상위)

/** 행정구역 추정용 접미사 */
const REGION_SUFFIX = /(특별시|광역시|도|시|군|구|동|읍|면)$/;

/** DB 좌표 파싱 (x=경도, y=위도) */
function toLatLng(x, y) {
  const lng = parseFloat(x);
  const lat = parseFloat(y);
  if (Number.isFinite(lat) && Number.isFinite(lng)) return { lat, lng };
  return null;
}

/** 현재 지도 bounds 내에 있는지 */
function inView(lat, lng, map) {
  const b = map.getBounds();
  const sw = b.getSouthWest();
  const ne = b.getNorthEast();
  return lat >= sw.getLat() && lat <= ne.getLat() && lng >= sw.getLng() && lng <= ne.getLng();
}

/** 카테고리 표시(두 번째 단계 우선, 없으면 첫 단계) */
function getCatLabel(categoryName) {
  if (!categoryName) return '기타';
  const parts = String(categoryName)
    .split('>')
    .map(s => s.trim())
    .filter(Boolean);
  if (parts.length >= 2) return parts[1];
  if (parts.length >= 1) return parts[0];
  return '기타';
}

/** 여러 점 bounds 맞추기(패딩 포함) + 줌 레벨 보정 */
function fitMapToPoints(map, points, paddingPx = 60) {
  const bounds = new window.kakao.maps.LatLngBounds();
  points.forEach(p => bounds.extend(new window.kakao.maps.LatLng(p.lat, p.lng)));
  console.log('[SEARCH] fitMapToPoints points=', points);
  map.setBounds(bounds, paddingPx, paddingPx, paddingPx, paddingPx);

  // 과도 확대/축소 보정 (필요시 조정)
  const level = map.getLevel();
  if (points.length > 1 && level < 5) map.setLevel(5); // 너무 디테일로 가지 않게
  if (level > 12) map.setLevel(12); // 전국 단위 과도 축소 방지
}

/** 한 점으로 이동(디테일 모드) */
function goDetail(map, lat, lng) {
  const center = new window.kakao.maps.LatLng(lat, lng);
  console.log('[SEARCH] goDetail ->', { lat, lng, level: Math.min(DETAIL_MAX_LEVEL, 4) });
  map.setCenter(center);
  map.setLevel(Math.min(DETAIL_MAX_LEVEL, 4));
}

/** 검색어가 행정구역일 가능성 추정 */
function looksLikeRegion(keyword) {
  const kw = keyword.trim();
  if (!kw) return false;
  if (REGION_SUFFIX.test(kw)) return true;
  return kw.split(/\s+/).some(tok => REGION_SUFFIX.test(tok));
}

/** 중복 좌표(같은 건물 내 분점 등) 제거 */
function dedupPoints(points) {
  const seen = new Set();
  const out = [];
  for (const p of points) {
    const key = `${p.lat.toFixed(7)},${p.lng.toFixed(7)}`;
    if (!seen.has(key)) {
      seen.add(key);
      out.push(p);
    }
  }
  return out;
}

/** ===== 하이라이트/임시핀 유틸 ===== */

/** 하버사인 거리(m) */
function distanceM(lat1, lng1, lat2, lng2) {
  const R = 6371000;
  const toRad = d => (d * Math.PI) / 180;
  const dLat = toRad(lat2 - lat1);
  const dLng = toRad(lat2 - lng1);
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2;
  return 2 * R * Math.asin(Math.sqrt(a));
}

// 검색 임시 핀 보관소
if (!globalThis._searchOverlays) globalThis._searchOverlays = [];

/** 임시 핀 생성(검색 좌표용 - 텍스트 없는 뾰족핀) - 초록색 */
function createTempPin(lat, lng) {
  const map = window.mapInstance;

  const wrap = document.createElement('div');
  wrap.style.cssText = `
    transform: translate(-50%, -100%);
    display:flex;flex-direction:column;align-items:center;gap:0;
    pointer-events:auto;
  `;

  const head = document.createElement('div'); // 초록 동그라미
  head.style.cssText = `
  width:20px;
  height:20px;
  border-radius:50%;
  background:#f57c00; 
  border:3px solid white; 
`;

  const tip = document.createElement('div');
  tip.style.cssText = `
  width:0;
  height:0;
  margin-top:1px;
  border-left:6px solid transparent;
  border-right:6px solid transparent;
  border-top:10px solid #f57c00;
  position:relative;
`;

  wrap.appendChild(head);
  wrap.appendChild(tip);

  const ov = new window.kakao.maps.CustomOverlay({
    position: new window.kakao.maps.LatLng(lat, lng),
    content: wrap,
    yAnchor: 1,
    zIndex: Z_HIT,
  });
  ov.setMap(map);
  globalThis._searchOverlays.push(ov);
  console.log('[SEARCH] temp pin created ->', { lat, lng });
}

/** 임시 핀 제거 */
function clearSearchPins() {
  (globalThis._searchOverlays || []).forEach(ov => ov.setMap(null));
  globalThis._searchOverlays = [];
  console.log('[SEARCH] temp pins cleared');
}

/** 가까운 디테일 오버레이 찾기 */
function findNearestDetail(lat, lng) {
  if (!globalThis._detailOverlays) return null;
  let best = null;
  let bestD = Infinity;

  globalThis._detailOverlays.forEach(ov => {
    const pos = ov.getPosition();
    const d = distanceM(lat, lng, pos.getLat(), pos.getLng());
    if (d < bestD) {
      bestD = d;
      best = ov;
    }
  });
  if (!best) return null;
  return { overlay: best, distance: bestD };
}

/** 디테일 오버레이 펄스 하이라이트 (링도 초록톤으로) */
function pulseOverlay(overlay) {
  const content = overlay.getContent();
  if (!(content instanceof HTMLElement)) return;
  content.style.position = 'relative';

  // keyframes 한 번만 주입
  if (!document.getElementById('search-pulse-style')) {
    const style = document.createElement('style');
    style.id = 'search-pulse-style';
    style.innerHTML = `
      @keyframes search-pulse {
        0% { opacity:.7; transform: scale(1); }
        100% { opacity:0; transform: scale(1.6); }
      }
    `;
    document.head.appendChild(style);
  }

  const ring = document.createElement('div');
  ring.style.cssText = `
    position:absolute; inset:-10px; border-radius:999px;
    border:3px solid rgba(46,204,113,.45);
    animation: search-pulse 1s ease-out 0s 3;
    pointer-events:none;
  `;
  content.appendChild(ring);

  const origZ = overlay.getZIndex?.() ?? Z_BASE;
  overlay.setZIndex?.(Z_HIT);
  setTimeout(() => {
    try {
      ring.remove();
    } catch {}
    overlay.setZIndex?.(origZ);
  }, 1600);
}

/** ====== 검색 강조/강제표시 유틸 ====== */
// 검색으로 '강제표시'해야 할 디테일 마커들을 기억(줌레벨 무시 노출)
if (!globalThis._forceShowOverlays) globalThis._forceShowOverlays = new Set();
// 검색 하이라이트(색/Z) 적용한 마커들 기억 → 다음 검색 때 원복
if (!globalThis._searchHitOverlays) globalThis._searchHitOverlays = [];

/** 디테일 마커 강조: 초록 배경 + zIndex 최상단(고정) */
function markOverlayHit(overlay) {
  const el = overlay.getContent?.();
  if (!(el instanceof HTMLElement)) return;
  if (el.dataset.searchHit === '1') return;

  el.dataset.prevBg = el.style.background || '';
  el.dataset.prevColor = el.style.color || '';
  el.dataset.prevZ = String(overlay.getZIndex?.() ?? Z_BASE);
  el.dataset.searchHit = '1';
  el.dataset.fixedZ = '1'; // hover에 zIndex 안 바꾸도록 플래그

  el.style.background = '#fb8c00';
  el.style.color = '#fff';
  overlay.setZIndex?.(Z_HIT);

  globalThis._searchHitOverlays.push(overlay);
}

/** 검색 UI 초기화: 임시핀 제거 + 강조 원복 + 강제표시 해제 */
function clearSearchUI() {
  clearSearchPins();

  (globalThis._searchHitOverlays || []).forEach(ov => {
    const el = ov.getContent?.();
    if (el instanceof HTMLElement && el.dataset.searchHit === '1') {
      el.style.background = el.dataset.prevBg || '';
      el.style.color = el.dataset.prevColor || '';
      el.style.boxShadow = el.dataset.prevShadow || '';
      ov.setZIndex?.(Number(el.dataset.prevZ || Z_BASE));
      delete el.dataset.prevBg;
      delete el.dataset.prevColor;
      delete el.dataset.prevShadow;
      delete el.dataset.prevZ;
      delete el.dataset.searchHit;
      delete el.dataset.fixedZ;
    }
  });
  globalThis._searchHitOverlays = [];
  globalThis._forceShowOverlays?.clear?.();
}

/* ===== (옵션) 텍스트 정규화 유틸 — 필요시 사용 ===== */
function normalizeText(s) {
  return String(s || '')
    .normalize('NFC')
    .trim()
    .replace(/\s+/g, '')
    .toLowerCase();
}

export default function MapPanel() {
  const theme = useTheme();
  const { mapData } = useSearchMapState(); // { details: {...}, summaries: {depth0,depth1,depth2}}

  console.log('mapData', mapData);
  const setCenterAddr = useSetRecoilState(mapCenterAddrState);
  const [mapReady, setMapReady] = useState(false);

  const searchCondition = useRecoilValue(mapSearchFilterState);
  const user = useRecoilValue(userState);

  const [open, setOpen] = useState(false);
  const [detailParams, setDetailParams] = useState(null);

  const [mapSearchText, setMapSearchText] = useState('');
  const [searchLoading, setSearchLoading] = useState(false);

  /** 최초 지도 생성 */
  useEffect(() => {
    const loadScript = () =>
      new Promise(resolve => {
        const script = document.createElement('script');
        const mapKey = import.meta.env.VITE_KAKAO_MAP_ID;
        script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${mapKey}&autoload=false&libraries=services`;
        script.async = true;
        document.head.appendChild(script);
        script.onload = resolve;
      });

    const initMap = () => {
      window.kakao.maps.load(() => {
        const container = document.getElementById('map');
        const map = new window.kakao.maps.Map(container, {
          center: new window.kakao.maps.LatLng(36.3504, 127.3845),
          level: 10, // 구 집계로 시작
        });
        const zoomControl = new window.kakao.maps.ZoomControl();
        map.addControl(zoomControl, window.kakao.maps.ControlPosition.RIGHT);
        window.mapInstance = map;

        // 대전 근처 bounds
        const bounds = new window.kakao.maps.LatLngBounds();
        bounds.extend(new window.kakao.maps.LatLng(36.461, 127.275));
        bounds.extend(new window.kakao.maps.LatLng(36.281, 127.493));
        map.setBounds(bounds);

        setMapReady(true);
      });
    };

    loadScript().then(() => window.kakao.maps.load(initMap));
  }, []);

  /** (지도 데이터 DB 데이터만 사용) */
  useEffect(() => {
    if (!window.kakao?.maps || !window.mapInstance || !mapReady) return;
    if (!mapData) return;

    const map = window.mapInstance;

    // 오버레이 캐시
    (globalThis._detailOverlays || new Map()).forEach(o => o.setMap(null));
    (globalThis._aggOverlays || new Map()).forEach(o => o.setMap(null));
    globalThis._detailOverlays = new Map();
    globalThis._aggOverlays = new Map();

    const draw = () => {
      const level = map.getLevel();

      const isDetail = level <= DETAIL_MAX_LEVEL;
      const isDong = level >= DONG_MAX_LEVEL_MIN && level <= DONG_MAX_LEVEL_MAX;
      const isGu = level >= GU_LEVEL_MIN && level <= GU_LEVEL_MAX;
      const isSido = level >= SIDO_LEVEL_MIN;

      // 항상 현재 화면에 보이는 '디테일' 객체들을 리스트로 넘긴다
      const visibleDetails = [];

      // ========== 상세 오버레이(마커) 갱신 ==========
      const details = Object.values(mapData?.details || {});
      details.forEach(item => {
        const coords = toLatLng(item.x, item.y);
        if (!coords) return;

        const key = item.addrDetail || `${coords.lat},${coords.lng}`;
        let overlay = globalThis._detailOverlays.get(key);

        // 오버레이 생성 (카테고리/금액/방문수 표기 + 클릭시 상세 모달)
        if (!overlay) {
          const div = document.createElement('div');
          div.style.cssText = `
            display:flex;align-items:center;justify-content:center;
            background:${theme.colors.primary};color:white;padding:5px 12px;
            border-radius:20px;font-weight:bold;font-size:${theme.sizes.medium};
            white-space:nowrap;box-shadow:0 2px 6px rgba(0,0,0,.3);height:30px;cursor:pointer;
          `;
          const catLabel = getCatLabel(item.categoryName);
          div.innerHTML =
            `${catLabel}&nbsp;${(item.totalSum ?? 0).toLocaleString()}원&nbsp;&nbsp;&nbsp;` +
            `<i class="fa fa-walking"></i>&nbsp;${item.visits ?? 0}`;

          overlay = new window.kakao.maps.CustomOverlay({
            position: new window.kakao.maps.LatLng(coords.lat, coords.lng),
            content: div,
            yAnchor: 1,
            zIndex: Z_BASE,
          });

          // hover 효과 (히트 마커면 z 고정)
          div.addEventListener('mouseenter', () => {
            const isHit = overlay.getContent?.()?.dataset.fixedZ === '1';
            if (!isHit) {
              div.style.transform = 'scale(1.25)';
              overlay.setZIndex(Z_HOVER);
            }
          });
          div.addEventListener('mouseleave', () => {
            const isHit = overlay.getContent?.()?.dataset.fixedZ === '1';
            if (!isHit) {
              div.style.transform = 'scale(1)';
              overlay.setZIndex(Z_BASE);
            }
          });

          // 클릭시 상세 모달 (기존 로직 유지)
          div.addEventListener('click', () => {
            const params = {
              cardOwnerPositionId: searchCondition.selectedRole?.value,
              cardUseName: searchCondition.cardUseName,
              numberOfVisits: searchCondition.numberOfVisits,
              addrName: searchCondition.addrName,
              startDate: searchCondition.startDate?.toISOString?.().split('T')[0],
              endDate: searchCondition.endDate?.toISOString?.().split('T')[0],
              sortOrder: searchCondition.sortOrder,
              addrDetail: item.addrDetail,
              detail: true,
              catLabel: item.categoryName || '기타',
            };
            setDetailParams(params);
            setOpen(true);
          });

          globalThis._detailOverlays.set(key, overlay);
        } else {
          overlay.setPosition(new window.kakao.maps.LatLng(coords.lat, coords.lng));
        }

        // 상세 모드이거나 '강제표시' 대상이면 줌레벨 무시하고 보이게
        const forceShow = globalThis._forceShowOverlays?.has(overlay);
        if ((isDetail && inView(coords.lat, coords.lng, map)) || forceShow) {
          overlay.setMap(map);
        } else {
          overlay.setMap(null);
        }

        // 화면에 보이는 디테일은 리스트에 담아 보냄 (모드 무관)
        if (inView(coords.lat, coords.lng, map)) {
          visibleDetails.push({
            ...item,
            categoryLabel: item.categoryName || '기타',
          });
        }
      });

      // ========== 집계(시/구/동) 오버레이 갱신 ==========
      globalThis._aggOverlays.forEach(o => o.setMap(null)); // 모드 바뀔 수 있으니 일단 숨김

      const pick = isDong
        ? mapData?.summaries?.depth2
        : isGu
          ? mapData?.summaries?.depth1
          : isSido
            ? mapData?.summaries?.depth0
            : [];

      if (!isDetail && Array.isArray(pick)) {
        pick.forEach(g => {
          const coords = toLatLng(g.x, g.y);
          if (!coords) return;
          if (!inView(coords.lat, coords.lng, map)) return;

          const key = `${g.depth}-${g.regionId}-${g.path}`;
          let overlay = globalThis._aggOverlays.get(key);

          if (!overlay) {
            const div = document.createElement('div');
            div.style.cssText = `
              display:flex;align-items:center;justify-content:center;text-align:center;
              background:${theme.colors.primary};color:white;padding:10px 14px;
              border-radius:24px;font-weight:bold;font-size:${theme.sizes.medium};
              white-space:nowrap;box-shadow:0 2px 6px rgba(0,0,0,.3);cursor:pointer;
            `;
            div.innerHTML = `${g.path}&nbsp;&nbsp;<i class="fa fa-walking"></i>&nbsp;${g.count ?? 0}회`;

            overlay = new window.kakao.maps.CustomOverlay({
              position: new window.kakao.maps.LatLng(coords.lat, coords.lng),
              content: div,
              yAnchor: 1,
              zIndex: Z_BASE,
            });

            // hover (히트 고정 무시 — 집계는 보통 히트 대상 아님이지만 동일 로직 적용)
            div.addEventListener('mouseenter', () => {
              const isHit = overlay.getContent?.()?.dataset.fixedZ === '1';
              if (!isHit) {
                div.style.transform = 'scale(1.25)';
                overlay.setZIndex(Z_HOVER);
              }
            });
            div.addEventListener('mouseleave', () => {
              const isHit = overlay.getContent?.()?.dataset.fixedZ === '1';
              if (!isHit) {
                div.style.transform = 'scale(1)';
                overlay.setZIndex(Z_BASE);
              }
            });

            // 클릭 시 한 칸 확대 + 그 위치로 센터 이동
            div.addEventListener('click', () => {
              const latlng = new window.kakao.maps.LatLng(coords.lat, coords.lng);
              map.setCenter(latlng);
              map.setLevel(Math.max(1, map.getLevel() - 1));
            });

            globalThis._aggOverlays.set(key, overlay);
          } else {
            overlay.setPosition(new window.kakao.maps.LatLng(coords.lat, coords.lng));
          }

          overlay.setMap(map);
        });
      }

      // 항상 ‘현재 화면에 보이는 디테일들’을 리스트 상태로 전달
      setCenterAddr(visibleDetails);
    };

    draw();
    const idle = () => draw();
    window.kakao.maps.event.addListener(map, 'idle', idle);
    return () => window.kakao.maps.event.removeListener(map, 'idle', idle);
  }, [mapData, mapReady, setCenterAddr, theme, searchCondition]);

  /** ===== 검색 로직 ===== */
  const handleSearch = async () => {
    const kw = mapSearchText.trim();
    const map = window.mapInstance;
    if (!window.kakao?.maps || !map) return;

    // 입력 없음 → 초기화
    if (!kw) {
      clearSearchUI();
      map.setLevel(10); // 기본 보기
      return;
    }

    setSearchLoading(true);
    try {
      // 새 검색 시작 전 항상 초기화 (이전 강조/앞쪽 z 인덱스, 임시핀 원복)
      clearSearchUI();

      const ps = new window.kakao.maps.services.Places();
      const geocoder = new window.kakao.maps.services.Geocoder();

      const keywordSearch = () =>
        new Promise(resolve => {
          ps.keywordSearch(
            kw,
            (data, status) => {
              console.log('[SEARCH] keywordSearch status=', status, 'raw=', data);
              if (status === window.kakao.maps.services.Status.OK) {
                const arr = data
                  .filter(d => d.place_name.includes(kw) || kw.includes(d.place_name))
                  .map(d => ({
                    name: d.place_name,
                    lat: parseFloat(d.y),
                    lng: parseFloat(d.x),
                  }));
                resolve(arr);
              } else resolve([]);
            },
            { size: 15 }
          );
        });

      const addressSearch = () =>
        new Promise(resolve => {
          geocoder.addressSearch(kw, (data, status) => {
            console.log('[SEARCH] addressSearch status=', status, 'raw=', data);
            if (status === window.kakao.maps.services.Status.OK) {
              const arr = data.map(d => ({
                name: d.address?.address_name || d.road_address?.address_name || kw,
                lat: parseFloat(d.y),
                lng: parseFloat(d.x),
              }));
              resolve(arr);
            } else resolve([]);
          });
        });

      // 키워드/주소 우선순위
      const preferAddr = looksLikeRegion(kw);
      let results = [];
      if (preferAddr) {
        results = await addressSearch();
        if (!results.length) results = await keywordSearch();
      } else {
        results = await keywordSearch();
        if (!results.length) results = await addressSearch();
      }

      // 중복 제거
      const uniq = dedupPoints(results);
      console.log('[SEARCH] results uniq=', uniq);
      if (!uniq.length) return;

      // 단건: 이동 + 강조 or 임시핀
      if (uniq.length === 1) {
        const { lat, lng } = uniq[0];
        goDetail(map, lat, lng);

        const near = findNearestDetail(lat, lng);
        if (near && near.distance <= 120) {
          markOverlayHit(near.overlay);
          globalThis._forceShowOverlays.add(near.overlay); // 줌레벨 무시 노출
          pulseOverlay(near.overlay);
        } else {
          createTempPin(lat, lng); // 디테일 마커가 없으면 임시핀
        }
        return;
      }

      // 다건: 전국 보기 유지 + 디테일 있으면 튀어나오게 + zIndex 최상단, 없으면 임시핀
      fitMapToPoints(map, uniq, 60);
      uniq.forEach(({ lat, lng }) => {
        const near = findNearestDetail(lat, lng);

        if (near && near.distance <= 150) {
          // 디테일 마커가 있는 경우: 초록 강조 + zIndex 최상단 + 강제표시
          markOverlayHit(near.overlay);
          globalThis._forceShowOverlays.add(near.overlay);
        } else {
          // 디테일 데이터가 아예 없는 좌표: 초록 뾰족 임시핀
          createTempPin(lat, lng);
        }
      });
    } finally {
      setSearchLoading(false);
    }
  };

  return (
    <>
      <MapBox>
        <SearchSection>
          <SearchInput
            value={mapSearchText}
            type="text"
            onChange={e => setMapSearchText(e.target.value)}
            placeholder="지역, 장소를 검색하세요."
            onKeyDown={e => {
              if (e.key === 'Enter') handleSearch();
              if (e.key === 'Escape') {
                setMapSearchText('');
                clearSearchUI(); // ESC로도 초기화
              }
            }}
          />
          <SearchButton onClick={handleSearch} disabled={searchLoading}>
            <SearchIcon size={22} /> {searchLoading ? '검색중' : '검색'}
          </SearchButton>
        </SearchSection>

        <MapContainer id="map" />
      </MapBox>

      <Modal open={open} onClose={() => setOpen(false)} title="상세보기">
        <DetailPage initialParams={detailParams} />
      </Modal>
    </>
  );
}

/* ===== styled ===== */
const MapBox = styled.div`
  width: 100%;
  height: 100%;
  position: relative;
`;
const MapContainer = styled.div`
  width: 100%;
  height: 100%;
`;
const SearchSection = styled.div`
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translate(-50%, 0);
  display: flex;
  background-color: #ffffff;
  border-radius: 999px;
  border: 2px solid ${({ theme }) => theme.colors.primary};
  justify-content: center;
  align-items: center;
  padding: 4px;
  gap: 8px;
  z-index: 2;
  width: 400px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  @media ${({ theme }) => theme.device.mobile} {
    width: 70%;
  }
`;
const SearchButton = styled.button`
  display: flex;
  align-items: center;
  background: ${({ color, theme }) => color || theme.colors.primary};
  border: none;
  color: white;
  font-weight: bold;
  font-size: ${({ theme }) => theme.sizes.medium};
  padding: 10px 16px;
  border-radius: 999px;
  cursor: pointer;
  white-space: nowrap;
  gap: 6px;
  svg {
    width: 20px;
    height: 20px;
  }
  &:disabled {
    opacity: 0.6;
    cursor: default;
  }
  @media ${({ theme }) => theme.device.mobile} {
    font-size: ${({ theme }) => theme.sizes.small};
    padding: 6px 12px;
  }
`;
const SearchInput = styled.input`
  border: none;
  background: unset;
  padding: 8px 10px;
  font-size: ${({ theme }) => theme.sizes.medium};
  flex: 1;
  min-width: 0;
  &:focus {
    outline: none;
  }
`;
const SearchIcon = styled(FiSearch)`
  margin-right: 6px;
`;
