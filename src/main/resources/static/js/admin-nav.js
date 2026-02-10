(function () {
    function withFragmentParam(url) {
        const u = new URL(url, window.location.origin);
        if (!u.searchParams.has('fragment')) {
            u.searchParams.set('fragment', '1');
        }
        return u.pathname + '?' + u.searchParams.toString();
    }

    async function loadFragment(href, push) {
        const contentEl = document.getElementById('admin-content');
        if (!contentEl) {
            // admin/app 레이아웃이 아닌 화면이면 동작하지 않음
            return;
        }

        const fragmentUrl = withFragmentParam(href);

        try {
            const res = await fetch(fragmentUrl, {
                headers: { 'X-Admin-Fragment': '1' },
                credentials: 'same-origin'
            });

            if (!res.ok) {
                contentEl.innerHTML = '<div class="bg-[#222] rounded p-10">콘텐츠 로딩 실패</div>';
                return;
            }

            const html = await res.text();
            contentEl.innerHTML = html;

            if (push) {
                history.pushState({}, '', href);
            }
        } catch (e) {
            contentEl.innerHTML = '<div class="bg-[#222] rounded p-10">네트워크 오류</div>';
        }
    }

    function shouldIgnoreClick(e) {
        // 새 탭/새창, 우클릭, 드래그 등 기본 UX 보장
        if (e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return true;
        if (typeof e.button === 'number' && e.button !== 0) return true;
        return false;
    }

    document.addEventListener('click', (e) => {
        const a = e.target.closest('[data-admin-nav]');
        if (!a) return;

        // admin/app 레이아웃이 아닐 때는 기본 네비게이션(페이지 이동)을 그대로 사용
        const contentEl = document.getElementById('admin-content');
        if (!contentEl) return;

        if (shouldIgnoreClick(e)) return;

        const href = a.getAttribute('href');
        if (!href || href.startsWith('#')) return;

        // 외부 링크는 건드리지 않음
        if (href.startsWith('http://') || href.startsWith('https://')) return;

        e.preventDefault();
        loadFragment(href, true);
    });

    window.addEventListener('popstate', () => {
        loadFragment(window.location.pathname + window.location.search, false);
    });
})();
