(function () {
    let navLock = false;

    // --------------------
    // VIEW / CHAPTER
    // --------------------
    const getView = () => {
        const v = document.body.dataset.view || document.body.getAttribute("data-view") || "search";
        return (v === "tier" || v === "result" || v === "search") ? v : "search";
    };

    const getCurrentChapter = () => {
        const view = getView();
        if (view === "tier") return document.querySelector("#chapter-2");
        if (view === "result") return document.querySelector("#chapter-3");
        return document.querySelector("#chapter-1");
    };

    const fadeIn = (chapter) => {
        if (!chapter) return;

        document.querySelectorAll(".chapter").forEach((c) => c.classList.remove("is-active"));
        void document.body.offsetHeight;

        requestAnimationFrame(() => {
            chapter.classList.add("is-active");
        });
    };

    const fadeOutAndNavigate = (url) => {
        if (!url) return;

        const chapter = getCurrentChapter();
        if (!chapter) {
            window.location.assign(url);
            return;
        }

        if (navLock) {
            window.location.assign(url);
            return;
        }
        navLock = true;

        chapter.classList.remove("is-active");

        setTimeout(() => {
            window.location.assign(url);
        }, 650);
    };

    // -------------------- overlay --------------------
    function buildPreResultOverlay(tierText, carName, carImg) {
        let overlay = document.querySelector(".pre-result-overlay");
        if (overlay) return overlay;

        overlay = document.createElement("div");
        overlay.className = "pre-result-overlay is-show";

        const safeImg = (carImg && carImg.trim()) ? carImg.trim() : "";

        overlay.innerHTML = `
    <div class="result-card pre-fly-card">
      <div class="card-inner">
        <div class="card-face card-front">
          <h3>${tierText || "TIER"}</h3>

          <div class="result-text">
            <b><span class="result-rank-name">${(tierText || "").replace("TIER ", "")}TIER</span> 입니다 !</b>
            <p class="carName">${carName || ""}</p>
          </div>

          <!-- ✅ 여기: 스핀 카드 이미지 -->
          <div class="result-image" style="${safeImg ? "" : "display:none;"}">
            <img src="${safeImg}" alt="차량"
     onerror="this.onerror=null; this.src='/unique/images/cars/' + (document.body.dataset.selectedTrimId || '') + '.png';">

          </div>

          <div class="see-more-btn-all">
            <a class="see-more-btn" href="javascript:void(0)" tabindex="-1" aria-hidden="true">
              계급도로 돌아가기
            </a>
          </div>
        </div>

        <div class="card-face card-back">
          <img src="/unique/images/mainLogo.png" alt="logo" onerror="this.style.display='none'">
        </div>
      </div>
    </div>
  `;

        document.body.appendChild(overlay);
        return overlay;
    }


    function removePreOverlay() {
        const overlay = document.querySelector(".pre-result-overlay");
        if (overlay) overlay.remove();
    }

    // ✅ 스핀 후 "정면(0deg)"에서 멈추고 잠깐 정지(hold)까지
    function spinOverlayCard(cardInner, onDone) {
        const spinMs = 1000;
        const turns  = 2.4;
        const slowMs = 1500;
        const holdMs = 220; // ✅ 정면에서 멈춘 뒤 이동 전 잠깐 정지

        const easeInOutQuad = (x) => (x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2);
        const easeOutQuint  = (x) => 1 - Math.pow(1 - x, 5);

        let mode = "spin";
        let start = performance.now();
        let settleStart = 0;

        let from = 0;
        let to = 0;

        function loop(now) {
            if (mode === "spin") {
                const t = Math.min(1, (now - start) / spinMs);
                const e = easeInOutQuad(t);

                const angle = (turns * 360) * e;
                cardInner.style.transform = `rotateY(${angle}deg)`;

                if (t < 1) requestAnimationFrame(loop);
                else {
                    mode = "slowdown";
                    settleStart = now;
                    from = angle;

                    // 0deg로 딱 맞춰 정면 정지
                    const diff = ((0 - from + 540) % 360) - 180;
                    to = from + diff;

                    requestAnimationFrame(loop);
                }
                return;
            }

            const t2 = Math.min(1, (now - settleStart) / slowMs);
            const e2 = easeOutQuint(t2);

            const cur = from + (to - from) * e2;
            cardInner.style.transform = `rotateY(${cur}deg)`;

            if (t2 < 1) requestAnimationFrame(loop);
            else {
                // ✅ 정면 고정
                cardInner.style.transform = "rotateY(0deg)";

                // ✅ 정면 상태 반영 → hold → done
                requestAnimationFrame(() => {
                    requestAnimationFrame(() => {
                        setTimeout(() => {
                            onDone && onDone();
                        }, holdMs);
                    });
                });
            }
        }

        requestAnimationFrame(loop);
    }

    function parseTierFromHref(href) {
        try {
            const u = new URL(href, window.location.origin);
            const tier = u.searchParams.get("tier");
            return tier ? String(tier) : null;
        } catch {
            return null;
        }
    }

    // -------------------- CH2: spin -> save rect -> navigate --------------------
    function goToResultWithFly(a) {
        if (navLock) return;
        navLock = true;

        const href = a.getAttribute("href");
        const tier = parseTierFromHref(href);

        // ✅ [변경된 부분 시작] 스핀 카드에 보일 티어는 "내 티어"로 고정
        const myTier =
            document.body?.dataset?.myTier
            || document.querySelector("#chapter-2 .tier-link.is-active")?.textContent?.match(/\d+/)?.[0]
            || tier;

        const tierText = myTier ? `TIER ${myTier}` : "TIER";
        // ✅ [변경된 부분 끝]

        const carName = a.getAttribute("data-name") || "";
        const carImg = a.getAttribute("data-img") || "";

        a.style.pointerEvents = "none";

        const overlay = buildPreResultOverlay(tierText, carName, carImg);
        const card = overlay.querySelector(".pre-fly-card");
        const inner = overlay.querySelector(".card-inner");

        if (!card || !inner) {
            window.location.assign(href);
            return;
        }

        spinOverlayCard(inner, () => {
            card.classList.add("is-flying");

            const r = card.getBoundingClientRect();
            sessionStorage.setItem(
                "flyCardFrom",
                JSON.stringify({
                    left: r.left,
                    top: r.top,
                    width: r.width,
                    height: r.height
                })
            );

            sessionStorage.setItem("preSpin", "1");
            window.location.assign(href);
        });
    }


    // -------------------- CH3: fly into result card 자리 --------------------
    function playFlyIntoResult() {
        const raw = sessionStorage.getItem("flyCardFrom");
        if (!raw) return;

        let from;
        try {
            from = JSON.parse(raw);
        } catch {
            sessionStorage.removeItem("flyCardFrom");
            return;
        }
        sessionStorage.removeItem("flyCardFrom");

        const resultCard = document.querySelector("#chapter-3 .result-card");
        const list = document.querySelector("#chapter-3 .card-grid");
        if (!resultCard) return;

        // 리스트는 fly 끝난 뒤에 등장
        if (list) {
            list.classList.remove("is-fadein");
            list.classList.add("is-prehide");
        }

        const to = resultCard.getBoundingClientRect();

        // ✅ 결과창의 실제 카드 그대로 복제해서 이동
        const cloned = resultCard.cloneNode(true);
        cloned.style.width = "100%";
        cloned.style.height = "100%";
        cloned.style.margin = "0";

        // ✅ 이동 중 버튼 숨김
        cloned.classList.add("is-flying");

        // ✅ fly clone에서는 이미지 강제 표시(기능 영향 없음)
        cloned.querySelectorAll(".result-image").forEach(el => {
            el.style.display = "block";
            el.style.visibility = "visible";
            el.style.opacity = "1";
        });
        cloned.querySelectorAll(".result-image img").forEach(img => {
            img.style.display = "block";
            img.style.visibility = "visible";
            img.style.opacity = "1";
        });


        // ✅ 정면 고정(스핀 끝난 카드 상태 = 정면)
        const clonedInner = cloned.querySelector(".card-inner");
        if (clonedInner) clonedInner.style.transform = "rotateY(0deg)";

        const fly = document.createElement("div");
        fly.className = "fly-clone";
        fly.style.left = from.left + "px";
        fly.style.top = from.top + "px";
        fly.style.width = from.width + "px";
        fly.style.height = from.height + "px";
        fly.appendChild(cloned);
        document.body.appendChild(fly);
        void fly.offsetWidth;
        resultCard.classList.add("is-hide-during-fly");

        const dx = to.left - from.left;
        const dy = to.top - from.top;
        const sx = to.width / from.width;
        const sy = to.height / from.height;

        // ✅ 이동을 더 천천히
        const DURATION = 950; // 기존 520ms → 950ms

        fly.animate(
            [
                { transform: "translate(0px,0px) scale(1,1)" },
                { transform: `translate(${dx}px,${dy}px) scale(${sx},${sy})` }
            ],
            {
                duration: DURATION,
                easing: "cubic-bezier(.15,.9,.15,1)",
                fill: "forwards"
            }
        );

        setTimeout(() => {
            fly.remove();
            resultCard.classList.remove("is-hide-during-fly");

            // ✅ 결과 카드 원본은 정상 표시(버튼도 다시 보이게)
            // (원본에는 is-flying 클래스 안 붙어있음)

            document.documentElement.classList.remove("has-preSpin");

            if (list) {
                list.classList.remove("is-prehide");
                list.classList.add("is-fadein");
            }

            navLock = false;
        }, DURATION + 60);
    }

    // -------------------- bfcache / restore --------------------
    function ensureVisibleCurrentView() {
        removePreOverlay();
        navLock = false;

        document.querySelectorAll(".tier-link").forEach((a) => {
            a.style.pointerEvents = "";
        });

        fadeIn(getCurrentChapter());
    }

    window.addEventListener("pageshow", (e) => {
        if (!e.persisted) return;
        ensureVisibleCurrentView();
    });

    // -------------------- RESULT: grid fade only --------------------
    function fadeInResultGrid() {
        const ch3 = document.querySelector("#chapter-3");
        if (!ch3) return;

        const grid = ch3.querySelector(".card-grid");
        if (!grid) return;

        if (document.documentElement.classList.contains("has-preSpin")) return;

        grid.classList.remove("is-fadein");
        grid.classList.add("is-prehide");

        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                grid.classList.remove("is-prehide");
                grid.classList.add("is-fadein");
            });
        });
    }

    function watchChapter3Activation() {
        const ch3 = document.querySelector("#chapter-3");
        if (!ch3) return;

        const run = () => {
            if (!ch3.classList.contains("is-active")) return;
            fadeInResultGrid();
        };

        run();

        const mo = new MutationObserver(run);
        mo.observe(ch3, { attributes: true, attributeFilter: ["class"] });

        window.addEventListener("resize", run);
    }

    document.addEventListener("DOMContentLoaded", () => {
        fadeIn(getCurrentChapter());

        if (sessionStorage.getItem("preSpin") !== "1") {
            document.documentElement.classList.remove("has-preSpin");
        }

        if (getView() === "result") {
            const cameFromPreSpin = sessionStorage.getItem("preSpin") === "1";
            if (cameFromPreSpin) {
                sessionStorage.removeItem("preSpin");
                playFlyIntoResult();
            } else {
                document.documentElement.classList.remove("has-preSpin");
                watchChapter3Activation();
            }
        }

        // tier link 클릭 -> 스핀 -> 결과 이동
        document.addEventListener("click", (e) => {
            const a = e.target.closest("a[href]");
            if (!a) return;

            if (a.target === "_blank" || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return;

            const viewNow = getView();
            const isTierLink = viewNow === "tier" && a.classList.contains("tier-link");
            if (!isTierLink) return;

            e.preventDefault();
            goToResultWithFly(a);
        });

        // 검색 form submit: 페이드아웃 후 이동
        const form = document.querySelector("#chapter-1 .chapter-search-box");
        if (form) {
            form.addEventListener("submit", (e) => {
                e.preventDefault();

                const action = form.getAttribute("action") || window.location.pathname;
                const fd = new FormData(form);
                const qs = new URLSearchParams(fd).toString();
                const url = qs ? `${action}?${qs}` : action;

                fadeOutAndNavigate(url);
            });
        }

        watchChapter3Activation();
    });
})();