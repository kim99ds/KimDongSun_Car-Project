(function () {
    function getActiveSlide(sw) {
        if (!sw) return null;
        const idx = sw.activeIndex ?? 0;
        return sw.slides && sw.slides[idx] ? sw.slides[idx] : null;
    }

    function setCandidateDetailHref(sw) {
        const btn = document.getElementById("btnCandidateDetail");
        if (!btn) return;

        const slide = getActiveSlide(sw);
        const modelId = slide?.getAttribute?.("data-model-id");

        if (!modelId) {
            btn.setAttribute("href", "#");
            btn.classList.add("is-disabled");
            return;
        }

        btn.classList.remove("is-disabled");
        btn.setAttribute("href", `/cars/${modelId}`);
    }

    function initSwiper() {
        const el = document.querySelector(".compare .swiper");
        if (!el) return null;

        const count = el.querySelectorAll(".swiper-slide").length;

        const sw = new Swiper(el, {
            slidesPerView: "auto",
            centeredSlides: true,
            speed: 600,
            loop: false,
            rewind: true,

            effect: count >= 2 ? "creative" : "slide",
            creativeEffect: {
                prev: { translate: [-120, 40, -200], rotate: [0, 0, -12], opacity: 0.3, scale: 0.9 },
                next: { translate: [120, -40, -200], rotate: [0, 0, 12], opacity: 0.3, scale: 0.9 },
                limitProgress: 3,
            },

            watchSlidesProgress: true,
            allowTouchMove: true,
        });

        return sw;
    }

    function bindNav(sw) {
        const prev = document.querySelector(".compare .prev");
        const next = document.querySelector(".compare .next");

        prev?.addEventListener("click", (e) => {
            e.preventDefault();
            sw.slidePrev();
        });

        next?.addEventListener("click", (e) => {
            e.preventDefault();
            sw.slideNext();
        });
    }

    function bindSlideClick(sw) {
        // 카드 클릭하면 해당 차량 상세로 이동
        const wrapper = document.querySelector(".compare .swiper");
        if (!wrapper) return;

        wrapper.addEventListener("click", (e) => {
            const slide = e.target?.closest?.(".swiper-slide");
            if (!slide) return;

            const modelId = slide.getAttribute("data-model-id");
            if (!modelId) return;

            window.location.href = `/cars/${modelId}`;
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        const sw = initSwiper();
        if (!sw) return;

        requestAnimationFrame(() => {
            sw.update();
            setCandidateDetailHref(sw);
        });

        bindNav(sw);
        bindSlideClick(sw);

        sw.on("slideChange", () => setCandidateDetailHref(sw));

        // 버튼 클릭 시 현재 슬라이드로 이동 (href가 #이면 막기)
        const btn = document.getElementById("btnCandidateDetail");
        btn?.addEventListener("click", (e) => {
            const href = btn.getAttribute("href");
            if (!href || href === "#") e.preventDefault();
        });
    });
})();