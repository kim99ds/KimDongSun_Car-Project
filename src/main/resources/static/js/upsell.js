// upsell.js
(function () {
    const qAdd = document.getElementById('qAddPrice');
    const qPage = document.getElementById('qPage');
    const qSearch = document.getElementById('qSearch');
    const qKeyword = document.getElementById('qKeyword');
    const btnSearch = document.getElementById('btnSearch');

    /* =========================================================
       ✅ NEW: 새로고침(리로드) 시 항상 초기 화면으로 이동
       - F5 / Ctrl+R / Cmd+R / 브라우저 새로고침 버튼 모두 포함
       - 필터 파라미터 제거 → 초기화
       - 초기 addPrice 기본값(+500만원)은 서버 로직 그대로 사용
    ========================================================= */
    const BASE_PATH = '/unique/upsell';

    const goBase = () => {
        // 이미 기본 경로 + 쿼리 없음이면 그대로 둠
        if (location.pathname === BASE_PATH && location.search === '') return;
        // 히스토리에 남기지 않고 교체
        location.replace(BASE_PATH);
    };

    // 키보드 새로고침 가로채기(F5, Ctrl+R, Cmd+R)
    window.addEventListener('keydown', (e) => {
        const key = (e.key || '').toLowerCase();
        const isRefresh =
            key === 'f5' ||
            ((e.ctrlKey || e.metaKey) && key === 'r');

        if (isRefresh) {
            e.preventDefault();
            goBase();
        }
    });

    // 브라우저 새로고침 버튼/주소창 새로고침 감지
    // - navigation entry type이 reload면 초기 화면으로 이동
    try {
        const navEntries = performance.getEntriesByType && performance.getEntriesByType('navigation');
        if (navEntries && navEntries.length > 0) {
            if (navEntries[0].type === 'reload') {
                goBase();
            }
        } else if (performance && performance.navigation) {
            // 구형 브라우저 fallback
            if (performance.navigation.type === 1) {
                goBase();
            }
        }
    } catch (_) {
        // ignore
    }

    const buildUrl = (pageNum) => {
        const checkedBrands = Array.from(document.querySelectorAll('input[name="brands"]:checked'))
            .map(el => 'brands=' + encodeURIComponent(el.value))
            .join('&');

        const checkedSegments = Array.from(document.querySelectorAll('input[name="segments"]:checked'))
            .map(el => 'segments=' + encodeURIComponent(el.value))
            .join('&');

        const kw = (qSearch?.value || '').trim();
        const q = kw ? ('q=' + encodeURIComponent(kw)) : '';

        const add = qAdd?.value ? ('addPrice=' + encodeURIComponent(qAdd.value)) : '';
        const page = 'page=' + encodeURIComponent(pageNum || '1');

        const parts = [checkedBrands, checkedSegments, q, add, page].filter(Boolean);
        return '/unique/upsell?' + parts.join('&');
    };

    const runSearch = () => {
        if (qKeyword) qKeyword.value = (qSearch?.value || '').trim();
        window.location.href = buildUrl('1');
    };

    // 검색 버튼/엔터
    if (btnSearch) btnSearch.addEventListener('click', runSearch);
    if (qSearch) {
        qSearch.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                runSearch();
            }
        });
    }

    // 브랜드/세그먼트 체크 변경 시 바로 갱신
    document.addEventListener('change', (e) => {
        const t = e.target;
        if (!t) return;

        if (t.matches('input[name="brands"], input[name="segments"]')) {
            if (qPage) qPage.value = '1';
            window.location.href = buildUrl('1');
        }
    });

    // 가격 버튼
    document.addEventListener('click', (e) => {
        const btn = e.target.closest('.tag-btn');
        if (!btn) return;
        e.preventDefault();

        if (qAdd) qAdd.value = btn.dataset.add || '';
        if (qPage) qPage.value = '1';
        window.location.href = buildUrl('1');
    });

    /* =========================
       Accordion (접기/펼치기)
       - .filter-section[data-acc]
       - localStorage 저장
    ========================= */
    const KEY = 'upsell_filter_acc_v2';

    const loadState = () => {
        try {
            return JSON.parse(localStorage.getItem(KEY) || '{}');
        } catch (_) {
            return {};
        }
    };

    const saveState = (state) => {
        try {
            localStorage.setItem(KEY, JSON.stringify(state));
        } catch (_) {}
    };

    const setCollapsed = (sectionEl, collapsed) => {
        if (!sectionEl) return;
        sectionEl.classList.toggle('is-collapsed', collapsed);

        const head = sectionEl.querySelector('.filter-head');
        if (head) head.classList.toggle('closed', collapsed);
    };

    const initAccordion = () => {
        const state = loadState();

        document.querySelectorAll('.filter-section[data-acc]').forEach(sec => {
            const key = sec.getAttribute('data-acc');
            const collapsed = state[key] === true;
            setCollapsed(sec, collapsed);

            const head = sec.querySelector('.filter-head');
            if (!head) return;

            head.addEventListener('click', () => {
                const nextCollapsed = !sec.classList.contains('is-collapsed');
                setCollapsed(sec, nextCollapsed);

                const next = loadState();
                next[key] = nextCollapsed;
                saveState(next);
            });
        });
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initAccordion);
    } else {
        initAccordion();
    }
})();