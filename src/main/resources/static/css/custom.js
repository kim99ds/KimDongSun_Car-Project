// ======== 헤더 =======================================================
$(function () {
    const $header = $(".site-header");
    const $allSub = $(".sub-menu");
    const $dropBg = $(".header-drop-bg");

    $header.on("click", function (e) {
        if (!$(e.target).closest(".gnb").length) return;

        if ($allSub.is(":visible")) {
            $allSub.stop().slideUp(350);
            $dropBg.stop().slideUp(350);
        } else {
            $allSub.stop().slideDown(350);
            $dropBg.stop().slideDown(350);
        }
    });

    $(document).on("click", function (e) {
        if (!$(e.target).closest(".site-header").length) {
            $allSub.stop().slideUp(200);
            $dropBg.stop().slideUp(200);
        }
    });
});


// ======== 질문 3 : 예산 range (✅ 최종 픽셀 배치) ========
document.addEventListener("DOMContentLoaded", () => {
    const wrap = document.querySelector(".question-4 .range-wrap");
    if (!wrap) return;

    const range = wrap.querySelector('input[type="range"]');
    if (!range) return;

    const topLabels = wrap.querySelectorAll(".range-labels span[data-value]");
    const bottomLabels = wrap.querySelectorAll(".range-ticks span[data-value]");
    const allLabels = wrap.querySelectorAll(".range-labels span[data-value], .range-ticks span[data-value]");

    const THUMB = 20;              // CSS thumb와 반드시 동일
    const HALF = THUMB / 2;

    const setLeft = (el, px) => el.style.setProperty("left", `${px}px`, "important");
    const setTransform = (el, v) => el.style.setProperty("transform", v, "important");

    const place = () => {
        const w = range.getBoundingClientRect().width;
        if (!w || w < 30) return;

        const min = Number(range.min);
        const max = Number(range.max);
        if (Number.isNaN(min) || Number.isNaN(max) || max <= min) return;

        // ✅ thumb 중심이 움직이는 실제 구간: [HALF, w - HALF]
        const usable = w - THUMB;

        const xOf = (value) => {
            const v = Number(value);
            if (Number.isNaN(v)) return null;
            const r = (v - min) / (max - min);
            return HALF + usable * r;
        };

        allLabels.forEach(el => {
            const x = xOf(el.dataset.value);
            if (x == null) return;
            setLeft(el, x);
            setTransform(el, "translateX(-50%)");
        });

        // ✅ 양끝 라벨만 안 잘리게
        const firstV = String(range.min);
        const lastV = String(range.max);

        const firstTop = wrap.querySelector(`.range-labels span[data-value="${firstV}"]`);
        const firstBottom = wrap.querySelector(`.range-ticks span[data-value="${firstV}"]`);
        const lastTop = wrap.querySelector(`.range-labels span[data-value="${lastV}"]`);
        const lastBottom = wrap.querySelector(`.range-ticks span[data-value="${lastV}"]`);

        if (firstTop) setTransform(firstTop, "translateX(0)");
        if (firstBottom) setTransform(firstBottom, "translateX(0)");
        if (lastTop) setTransform(lastTop, "translateX(-100%)");
        if (lastBottom) setTransform(lastBottom, "translateX(-100%)");
    };

    // ✅ “처음으로 돌아가는” 현상 방지: width가 잡힐 때까지 재시도 + load에서도 1번 더
    const retry = (n = 0) => {
        place();
        if (n < 30) requestAnimationFrame(() => retry(n + 1));
    };

    retry(0);
    window.addEventListener("load", place);
    window.addEventListener("resize", place);
    range.addEventListener("input", place);

    // 라벨 클릭 시 range 이동
    allLabels.forEach(el => {
        el.addEventListener("click", () => {
            range.value = el.dataset.value;
            range.dispatchEvent(new Event("input", { bubbles: true }));
        });
    });
});


// ======== 질문 5-1 : EV 충전 가능 여부 토글 ==========================
document.addEventListener("DOMContentLoaded", () => {
    const evCheck = document.getElementById("evCheck");
    const q51 = document.querySelector(".question-51");
    if (!evCheck || !q51) return;

    const evRadios = q51.querySelectorAll('input[name="ev_charge"]');
    const evSelect = q51.querySelector("select");

    function closeQ51() {
        q51.classList.remove("is-open");
        evRadios.forEach(r => (r.checked = false));
        if (evSelect) evSelect.value = "";
    }

    function openQ51() {
        q51.classList.add("is-open");
    }

    evCheck.checked ? openQ51() : closeQ51();

    evCheck.addEventListener("change", () => {
        evCheck.checked ? openQ51() : closeQ51();
    });
});
