// ========헤더=======================================================
$(function () {
    const $header = $(".site-header");
    const $allSub = $(".sub-menu");
    const $dropBg = $(".header-drop-bg");

    $header.on("mouseenter", function () {
        $allSub.stop(true, true).slideDown(300);
        $dropBg.stop(true, true).slideDown(300);
    });

    $header.on("mouseleave", function () {
        $allSub.stop(true, true).slideUp(200);
        $dropBg.stop(true, true).slideUp(200);
    });
});

// ===============메인배너=======================================================
document.addEventListener("DOMContentLoaded", () => {


    const slides = document.querySelectorAll("#mainVisual .bg-slide");

    const current = document.querySelector("#mainVisual .current");
    const next = document.querySelector("#mainVisual .next");

    const heroSub = document.querySelector("#mainVisual .hero-sub");
    const heroTitle = document.querySelector("#mainVisual .hero-title");

    const mainVisual = document.querySelector("#mainVisual");


    if (!slides.length) return;

    let index = 0;
    let isAnimating = false;
    let autoTimer = null;

    const IMAGE_DELAY = 5000;
    // 영상 노출 시간
    const VIDEO_DELAY = 5500;

    function hideText() {
        heroSub.style.opacity = 0;
        heroTitle.style.opacity = 0;
        heroSub.style.transform = "translateY(-30px)";
        heroTitle.style.transform = "translateY(30px)";
    }

    function showText(isVideo = false) {
        requestAnimationFrame(() => {
            heroSub.style.opacity = 1;
            heroTitle.style.opacity = 1;

            if (isVideo) {
                heroSub.style.transition = "opacity 1.2s ease, transform 1.2s ease";
                heroTitle.style.transition = "opacity 1.4s ease, transform 1.4s ease";
                heroSub.style.transform = "translateY(10px)";
                heroTitle.style.transform = "translateY(0)";
            } else {
                heroSub.style.transition = "opacity 0.8s ease, transform 0.8s ease";
                heroTitle.style.transition = "opacity 0.8s ease, transform 0.8s ease";
                heroSub.style.transform = "translateY(0)";
                heroTitle.style.transform = "translateY(0)";
            }
        });
    }

    // 배경 세팅 (이미지 or 영상)
    function setBackground(layer, slide) {
        layer.innerHTML = "";
        layer.style.backgroundImage = "";

        if (slide.dataset.video) {
            const video = document.createElement("video");
            video.src = slide.dataset.video;
            video.autoplay = true;
            video.muted = true;
            video.loop = true;
            video.playsInline = true;

            video.style.width = "100%";
            video.style.height = "100%";
            video.style.objectFit = "cover";

            layer.appendChild(video);
        } else {
            layer.style.backgroundImage = `url(${slide.dataset.bg})`;
        }
    }

    setBackground(current, slides[0]);
    heroSub.textContent = slides[0].dataset.sub;
    heroTitle.textContent = slides[0].dataset.title;
    showText(!!slides[0].dataset.video);

    // 슬라이드 이동 함수
    function moveSlide(direction = "next", isDragged = false) {
        if (isAnimating) return;
        isAnimating = true;

        if (direction === "next") {
            index = (index + 1) % slides.length;
        } else {
            index = (index - 1 + slides.length) % slides.length;
        }

        hideText();

        // 드래그가 아닐 때만 다음 레이어 준비
        if (!isDragged) {
            next.style.transition = "none";
            setBackground(next, slides[index]);
            next.style.transform = direction === "next"
                ? "translateX(100%)"
                : "translateX(-100%)";
            next.offsetHeight;
        }

        requestAnimationFrame(() => {
            next.style.transition = "transform 1.2s ease";
            current.style.transition = "transform 1.2s ease";

            current.style.transform = direction === "next"
                ? "translateX(-100%)"
                : "translateX(100%)";

            next.style.transform = "translateX(0)";
        });

        setTimeout(() => {
            setBackground(current, slides[index]);

            current.style.transition = "none";
            next.style.transition = "none";
            current.style.transform = "translateX(0)";
            next.style.transform = direction === "next"
                ? "translateX(100%)"
                : "translateX(-100%)";

            heroSub.textContent = slides[index].dataset.sub;
            heroTitle.textContent = slides[index].dataset.title;
            showText(!!slides[index].dataset.video);

            isAnimating = false;
        }, 1200);
    }

    // 자동 슬라이드
    function startAuto() {
        if (autoTimer) clearInterval(autoTimer);

        const isVideo = !!slides[index].dataset.video;
        const delay = isVideo ? VIDEO_DELAY : IMAGE_DELAY;

        autoTimer = setInterval(() => {
            moveSlide("next");
        }, delay);
    }

    startAuto();

    // 드래그
    let startX = 0;
    let isDragging = false;

    // 드래그 시작
    mainVisual.addEventListener("mousedown", e => {
        // transform 충돌 막음
        if (isAnimating) return;

        clearInterval(autoTimer);
        // 마우스 x좌표 저장
        startX = e.clientX;
        isDragging = true;
        mainVisual.classList.add("is-dragging");
    });

    // 마우스와 같이 움직임
    window.addEventListener("mousemove", e => {

        if (!isDragging || isAnimating) return;

        // diff가 음수면 왼쪽 / 양수면 오른쪽
        const diff = e.clientX - startX;
        // 끌리는 만틈 이동 -> 현재 배너 폭 필요
        const width = mainVisual.offsetWidth;

        // 자여연스럽게 하기위해 지움
        current.style.transition = "none";
        current.style.transform = `translateX(${diff}px)`;

        next.style.transition = "none";

        // 왼쪽으로 끌 때
        // diff = -100이면 → width - 100 : 100px 만큼
        if (diff < 0) {
            setBackground(next, slides[(index + 1) % slides.length]);
            next.style.transform = `translateX(${width + diff}px)`;
        } else {
            // 오른쪽으로 끌 때
            // diff = 100이면 → -width + 100 : 100px 만큼
            setBackground(next, slides[(index - 1 + slides.length) % slides.length]);
            next.style.transform = `translateX(${-width + diff}px)`;
        }
    });

    window.addEventListener("mouseup", e => {
        // 드래그x 마우스 오면 무시
        if (!isDragging) return;
        isDragging = false;
        mainVisual.classList.remove("is-dragging");

        const diff = e.clientX - startX;

        if (Math.abs(diff) < 60) {
            current.style.transition = "transform 0.3s ease";
            current.style.transform = "translateX(0)";
            next.style.transition = "transform 0.3s ease";
            next.style.transform = diff < 0 ? "translateX(100%)" : "translateX(-100%)";
            startAuto();
            return;
        }

        if (diff < 0) moveSlide("next", true);
        else moveSlide("prev", true);

        startAuto();
    });

});

// ====================베스트카==================

const cards = document.querySelectorAll("#bestCar .bestcar-card");
let hasInteracted = false;

cards.forEach(card => {
    card.addEventListener("mouseenter", () => {

        cards.forEach(c => c.classList.remove("is-active"));

        card.classList.add("is-active");

        hasInteracted = true;
    });
});


//===============이벤트배너============================================

document.addEventListener("DOMContentLoaded", () => {
    const viewport = document.querySelector(".eventbanner-viewport");
    const slides = document.querySelectorAll(".eventbanner-slide");
    const dots = document.querySelectorAll(".eventbanner-pagination .dot");

    const titleEl = document.querySelector(".eventbanner-title");
    const descEl = document.querySelector(".eventbanner-desc");
    const ctaEl = document.querySelector(".eventbanner-cta");

    let current = 0;
    let timer = null;
    const interval = 2500;

    function init() {
        slides.forEach((slide, i) => {
            slide.style.backgroundImage = `url(${slide.dataset.bg})`;
            // 첫 번째만 보이고, 나머지는 숨김
            slide.style.opacity = i === 0 ? "1" : "0";
        });

        updateContent(0);
        updateDots(0);
        startAuto();
    }

    function updateContent(index) {
        const slide = slides[index];
        titleEl.textContent = slide.dataset.title;
        descEl.textContent = slide.dataset.desc;
        ctaEl.setAttribute("href", slide.dataset.link);

        const content = document.querySelector(".eventbanner-content");
        content.classList.toggle("is-right", index === 2);
    }

    function updateDots(index) {
        dots.forEach((dot, i) => {
            dot.classList.toggle("is-active", i === index);
        });
    }

    // 슬라이드 전환 (페이드)
    function goTo(index) {
        if (index === current) return;

        slides[current].style.opacity = "0";
        slides[index].style.opacity = "1";

        current = index;
        updateContent(index);
        updateDots(index);
    }

    // 자동 슬라이드
    function startAuto() {
        stopAuto();
        timer = setInterval(() => {
            const next = (current + 1) % slides.length;
            goTo(next);
        }, interval);
    }

    function stopAuto() {
        if (timer) clearInterval(timer);
    }

    dots.forEach((dot, i) => {
        dot.addEventListener("click", () => {
            goTo(i);
            startAuto();
        });
    });

    // 마우스 올리면 멈춤 / 벗어나면 재개
    viewport.addEventListener("mouseenter", stopAuto);
    viewport.addEventListener("mouseleave", startAuto);

    init();
});

//================브랜드 별 인기============================================

document.addEventListener("DOMContentLoaded", () => {
    const tabs = document.querySelectorAll("#brandPopular .tab");
    const tracks = document.querySelectorAll("#brandPopular .cars-track");

    const brandName = document.querySelector("#brandPopular .brand-name");
    const brandSub = document.querySelector("#brandPopular .brand-sub");

    const prevBtn = document.querySelector("#brandPopular .prev");
    const nextBtn = document.querySelector("#brandPopular .next");

    let activeTrack = document.querySelector(
        "#brandPopular .cars-track.is-active"
    );

    /* 탭 전환 */
    tabs.forEach(tab => {
        tab.addEventListener("click", () => {
            tabs.forEach(t => t.classList.remove("is-active"));
            tab.classList.add("is-active");


            brandName.textContent = tab.dataset.name;
            brandSub.textContent = tab.dataset.sub;

            const brand = tab.textContent.trim();

            tracks.forEach(track => {
                track.classList.remove("is-active");
                if (track.dataset.brand === brand) {
                    track.classList.add("is-active");
                    activeTrack = track;
                    updateBig();
                }
            });
        });
    });

    /* 캐러셀 이동 */
    function rotateNext() {
        if (!activeTrack) return;

        const items = Array.from(activeTrack.children);
        activeTrack.appendChild(items[0]);
        updateBig();
    }

    function rotatePrev() {
        if (!activeTrack) return;

        const items = Array.from(activeTrack.children);

        // 마지막을 앞으로 보냄
        activeTrack.insertBefore(items[items.length - 1], items[0]);

        updateBig();
    }

    // 항상 첫 번째 아이템만 크게
    function updateBig() {
        const items = activeTrack.querySelectorAll(".car-item");
        items.forEach(item => item.classList.remove("is-big"));
        if (items[0]) items[0].classList.add("is-big");
    }

    prevBtn.addEventListener("click", rotatePrev);
    nextBtn.addEventListener("click", rotateNext);
});

// ===========어워즈=================================

document.addEventListener("DOMContentLoaded", () => {
    const section = document.querySelector("#awards");
    // awards 섹션이 없는 페이지에서는 실행 안 되게 안전장치
    if (!section) return;

    // DOM 캐싱
    const track = section.querySelector(".awards-track");
    const cards = Array.from(section.querySelectorAll(".award-card"));
    const bg = section.querySelector(".awards-bg");

    const prevBtn = section.querySelector(".awards-nav.prev");
    const nextBtn = section.querySelector(".awards-nav.next");

    const segs = Array.from(section.querySelectorAll(".awards-progress .seg"));
    const playBtn = section.querySelector(".awards-progress .play");
    const pauseBtn = section.querySelector(".awards-progress .pause");

    const GAP = 130;
    const CARD_WIDTH = 660;
    const ACTIVE_SCALE = 1.1;

    let currentIndex = cards.findIndex(c => c.classList.contains("is-active"));
    if (currentIndex === -1) currentIndex = 0;


    let isPlaying = true;
    let timer = null;
    let progressRAF = null;
    let progressStart = null;

    const DURATION = 4000;

    function updateBackground(index) {
        const img = cards[index].querySelector("img");
        if (!img) return;
        bg.style.backgroundImage = `url(${img.src})`;
    }

    function updateSeg(index) {
        segs.forEach((s, i) => {
            s.classList.toggle("is-active", i === index);
        });
    }

    function updateCards(index) {
        cards.forEach((c, i) => {
            c.classList.toggle("is-active", i === index);
        });
    }


    function moveTrack(index) {
        const viewport = section.querySelector(".awards-viewport");
        const viewportCenter = viewport.offsetWidth / 2;

        const centerOffset = (CARD_WIDTH * ACTIVE_SCALE) / 2;
        // index에 따라 얼마나 이동해야 하는지
        const base = (CARD_WIDTH + GAP) * index;

        // 시각적 중앙 보정값
        const ADJUST = 60;
        const x = viewportCenter - centerOffset - base + ADJUST;
        track.style.transition = "transform 0.8s ease";
        track.style.transform = `translateX(${x}px)`;
    }


    function resetProgress() {
        if (progressRAF) cancelAnimationFrame(progressRAF);
        progressStart = null;

        segs.forEach(seg => {
            seg.style.background = "rgba(255,255,255,0.3)";
        });
    }

    function animateProgress() {
        function step(ts) {
            if (!progressStart) progressStart = ts;
            const elapsed = ts - progressStart;
            const ratio = Math.min(elapsed / DURATION, 1);

            const activeSeg = segs[currentIndex];
            activeSeg.style.background = `
                    linear-gradient(
                    to right,
                    #fff ${ratio * 100}%,
                    rgba(255,255,255,0.3) ${ratio * 100}%
                    )
                `;

            if (ratio < 1 && isPlaying) {
                progressRAF = requestAnimationFrame(step);
            }
        }

        progressRAF = requestAnimationFrame(step);
    }


    function moveTo(index) {
        if (index < 0) index = cards.length - 1;
        if (index >= cards.length) index = 0;

        currentIndex = index;

        updateCards(index); // 중앙 카드 교체
        moveTrack(index); // 트랙 이동
        updateBackground(index); // 배경 변경
        updateSeg(index); // 하단 bar 변경

        resetProgress();

        if (isPlaying) {
            animateProgress();
        }
    }

    // 자동재생 시작
    function startAuto() {
        isPlaying = true;

        // 아이콘 토글 (레이아웃 안 흔들리게 visibility 사용)
        playBtn.style.visibility = "hidden";
        playBtn.style.opacity = "0";

        pauseBtn.style.visibility = "visible";
        pauseBtn.style.opacity = "1";

        resetProgress();
        animateProgress();

        timer = setInterval(() => {
            moveTo(currentIndex + 1);
        }, DURATION);
    }

    function stopAuto() {
        isPlaying = false;
        pauseBtn.style.visibility = "hidden";
        pauseBtn.style.opacity = "0";

        playBtn.style.visibility = "visible";
        playBtn.style.opacity = "1";
        clearInterval(timer);
        timer = null;

        if (progressRAF) cancelAnimationFrame(progressRAF);
    }

    prevBtn.addEventListener("click", () => {
        moveTo(currentIndex - 1);
        if (isPlaying) {
            clearInterval(timer);
            startAuto();
        }
    });

    nextBtn.addEventListener("click", () => {
        moveTo(currentIndex + 1);
        if (isPlaying) {
            clearInterval(timer);
            startAuto();
        }
    });

    playBtn.addEventListener("click", () => {
        if (!isPlaying) startAuto();
    });

    pauseBtn.addEventListener("click", () => {
        stopAuto();
    });

    playBtn.style.display = "inline-block";
    pauseBtn.style.display = "inline-block";

    playBtn.style.visibility = "hidden";
    playBtn.style.opacity = "0";

    pauseBtn.style.visibility = "visible";
    pauseBtn.style.opacity = "1";

    // 초기화
    updateBackground(currentIndex);
    moveTrack(currentIndex);
    updateSeg(currentIndex);
    startAuto();
});

// ================= TOP BUTTON ===========================
document.addEventListener("DOMContentLoaded", () => {
    const topBtn = document.querySelector("#topBtn");
    const mainVisual = document.querySelector("#mainVisual");
    // 둘 중 하나라도 없으면 실행 중단 (에러 방지용 안전장치)
    if (!topBtn || !mainVisual) return;

    const triggerY = mainVisual.offsetHeight * 0.7;

    window.addEventListener("scroll", () => {
        if (window.scrollY > triggerY) {
            topBtn.classList.add("is-show");
        } else {
            topBtn.classList.remove("is-show");
        }
    });

    topBtn.addEventListener("click", () => {
        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });
    });
});

