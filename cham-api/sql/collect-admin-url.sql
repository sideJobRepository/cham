-- 관리자 API 권한 (몇 번 돌려도 같은 결과가 나오도록 짰다)
--
-- 왜 필요한가
--   ChamMonimapAuthorizationManager.check() 는 URL_RESOURCES 에 매칭되는 행이 없으면 '허용' 으로 떨어진다.
--   2026-09-24 운영 DB 에는 관리자 경로가 두 줄뿐이었다.
--     /cham/admin/card-use-uploads         GET  ADMIN
--     /cham/admin/card-use-uploads/public  PUT  ADMIN
--   그래서 삭제키 변경(/delete-key), 엑셀 내려받기(/excel), 자료정리(/cleanup/**),
--   새 수집관리(/collect/**), 엑셀 업로드·삭제(/cham/upload) 가 로그인 없이도 호출되는 상태였다.
--
-- 적용 후 cham-api 를 재기동해야 반영된다 (reload() 를 부르는 곳이 없다).
-- 행 하나에 HTTP 메서드 하나만 쓸 수 있다 ('GET,POST' 처럼 쓰면 기동 때 터진다).

-- 1) /cham/admin/** 전부 ADMIN
INSERT INTO CHAM.CHAM_MONIMAP_URL_RESOURCES (CHAM_MONIMAP_URL_RESOURCES_PATH, CHAM_MONIMAP_HTTP_METHODS, REGIST_DATE, MODIFY_DATE)
SELECT '/cham/admin/**', m.method, NOW(), NOW()
FROM (SELECT 'GET' AS method UNION ALL SELECT 'POST' UNION ALL SELECT 'PUT' UNION ALL SELECT 'DELETE') m
WHERE NOT EXISTS (SELECT 1 FROM CHAM.CHAM_MONIMAP_URL_RESOURCES u
                  WHERE u.CHAM_MONIMAP_URL_RESOURCES_PATH = '/cham/admin/**'
                    AND u.CHAM_MONIMAP_HTTP_METHODS = m.method);

-- 2) 엑셀 업로드·삭제키 삭제도 ADMIN (화면에서는 이미 관리자 메뉴에만 있다)
INSERT INTO CHAM.CHAM_MONIMAP_URL_RESOURCES (CHAM_MONIMAP_URL_RESOURCES_PATH, CHAM_MONIMAP_HTTP_METHODS, REGIST_DATE, MODIFY_DATE)
SELECT p.path, p.method, NOW(), NOW()
FROM (SELECT '/cham/upload' AS path, 'POST' AS method UNION ALL SELECT '/cham/upload/*', 'DELETE') p
WHERE NOT EXISTS (SELECT 1 FROM CHAM.CHAM_MONIMAP_URL_RESOURCES u
                  WHERE u.CHAM_MONIMAP_URL_RESOURCES_PATH = p.path
                    AND u.CHAM_MONIMAP_HTTP_METHODS = p.method);

-- 3) 위에서 넣은 행을 ADMIN 에 연결
INSERT INTO CHAM.CHAM_MONIMAP_URL_RESOURCES_ROLE (CHAM_MONIMAP_ROLE_ID, CHAM_MONIMAP_URL_RESOURCES_ID, REGIST_DATE, MODIFY_DATE)
SELECT r.CHAM_MONIMAP_ROLE_ID, u.CHAM_MONIMAP_URL_RESOURCES_ID, NOW(), NOW()
FROM CHAM.CHAM_MONIMAP_URL_RESOURCES u
         JOIN CHAM.CHAM_MONIMAP_ROLE r ON r.CHAM_MONIMAP_ROLE_NAME = 'ADMIN'
WHERE u.CHAM_MONIMAP_URL_RESOURCES_PATH IN ('/cham/admin/**', '/cham/upload', '/cham/upload/*')
  AND NOT EXISTS (SELECT 1 FROM CHAM.CHAM_MONIMAP_URL_RESOURCES_ROLE x
                  WHERE x.CHAM_MONIMAP_URL_RESOURCES_ID = u.CHAM_MONIMAP_URL_RESOURCES_ID);

-- 확인
-- SELECT u.CHAM_MONIMAP_URL_RESOURCES_PATH, u.CHAM_MONIMAP_HTTP_METHODS, r.CHAM_MONIMAP_ROLE_NAME
-- FROM CHAM.CHAM_MONIMAP_URL_RESOURCES u
--   LEFT JOIN CHAM.CHAM_MONIMAP_URL_RESOURCES_ROLE ur ON ur.CHAM_MONIMAP_URL_RESOURCES_ID = u.CHAM_MONIMAP_URL_RESOURCES_ID
--   LEFT JOIN CHAM.CHAM_MONIMAP_ROLE r ON r.CHAM_MONIMAP_ROLE_ID = ur.CHAM_MONIMAP_ROLE_ID;
