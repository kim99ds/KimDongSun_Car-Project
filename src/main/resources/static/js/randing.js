document.addEventListener('DOMContentLoaded', () => {
    // 실제 섹션(.clone 제외)
    const realSections = document.querySelectorAll('.car:not(.clone)');
    const cloneFirst = document.querySelector('.clone-first');
    const cloneLast = document.querySelector('.clone-last');
    const sections = document.querySelectorAll('.car');
    const scrollUIs = document.querySelectorAll('.car .scroll');

    if (!realSections.length) return;

    const duration = 1200;

    // index 구조
    // [0] clone-last
    // [1] real 1
    // [2] real 2
    // ...
    // [n] real last
    // [n+1] clone-first
    let currentIndex = 1;
    let isScrolling = false;
    let wheelTimeout = null;

    // clone 내용 채우기
    cloneFirst.innerHTML = realSections[0].innerHTML;
    cloneLast.innerHTML = realSections[realSections.length - 1].innerHTML;

    // 시작 위치를 진짜 첫 섹션으로
    window.scrollTo(0, sections[currentIndex].offsetTop);

    function easeInOutCubic(t) {
        return t < 0.5
            ? 4 * t * t * t
            : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }

    function hideScrollUI() {
        scrollUIs.forEach(ui => ui.classList.add('hide'));
    }

    function showScrollUI() {
        scrollUIs.forEach(ui => ui.classList.remove('hide'));
    }

    function smoothScrollTo(targetY, onComplete) {
        const startY = window.scrollY;
        const distance = targetY - startY;
        let startTime = null;

        function animation(time) {
            if (!startTime) startTime = time;

            const progress = Math.min((time - startTime) / duration, 1);
            const eased = easeInOutCubic(progress);

            window.scrollTo(0, startY + distance * eased);

            if (progress < 1) {
                requestAnimationFrame(animation);
            } else {
                isScrolling = false;
                showScrollUI();
                if (onComplete) onComplete();
            }
        }

        requestAnimationFrame(animation);
    }

    function move(direction) {
        if (isScrolling) return;
        isScrolling = true;
        hideScrollUI();

        currentIndex += direction === 'down' ? 1 : -1;

        smoothScrollTo(sections[currentIndex].offsetTop, () => {
            // clone-first → 진짜 첫
            if (sections[currentIndex] === cloneFirst) {
                document.body.classList.add('is-jumping');
                currentIndex = 1;

                requestAnimationFrame(() => {
                    window.scrollTo(0, sections[currentIndex].offsetTop);
                    document.body.classList.remove('is-jumping');
                });
            }

            // clone-last → 진짜 마지막
            if (sections[currentIndex] === cloneLast) {
                document.body.classList.add('is-jumping');
                currentIndex = realSections.length;

                requestAnimationFrame(() => {
                    window.scrollTo(0, sections[currentIndex].offsetTop);
                    document.body.classList.remove('is-jumping');
                });
            }
        });
    }

    // 마우스 휠
    window.addEventListener('wheel', e => {
        e.preventDefault();
        if (wheelTimeout) return;

        if (e.deltaY > 0) move('down');
        else if (e.deltaY < 0) move('up');
    }, { passive: false });

    // 키보드
    window.addEventListener('keydown', e => {
        if (isScrolling) return;
        if (e.key === 'ArrowDown') move('down');
        if (e.key === 'ArrowUp') move('up');
    });
});
