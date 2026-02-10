
(() => {
  const modal = document.getElementById("cpModal");
  if (!modal) return;

  const body = modal.querySelector(".cp-modal__body");
  const closeEls = () => modal.querySelectorAll("[data-modal-close]");
  const open = () => {
    modal.classList.remove("hidden");
    modal.setAttribute("aria-hidden", "false");
    document.body.classList.add("cp-modal-open");
  };
  const close = () => {
    modal.classList.add("hidden");
    modal.setAttribute("aria-hidden", "true");
    body.innerHTML = "";
    document.body.classList.remove("cp-modal-open");
  };

  const bindClose = () => {
    closeEls().forEach(el => el.addEventListener("click", close));
    document.addEventListener("keydown", (e) => {
      if (e.key === "Escape" && !modal.classList.contains("hidden")) close();
    });
  };

  async function loadIntoModal(url) {
    open();
    body.innerHTML = `<div class="cp-modal__loading">불러오는 중...</div>`;
    try {
      const res = await fetch(url, { headers: { "X-Requested-With": "XMLHttpRequest" } });
      const html = await res.text();
      const doc = new DOMParser().parseFromString(html, "text/html");

      // Try to extract main content area (admin pages)
      const candidates = [
        doc.querySelector(".cp-admin-event"),
        doc.querySelector(".cp-admin"),
        doc.querySelector("#main-content"),
        doc.querySelector("main"),
      ].filter(Boolean);

      const content = candidates[0] || doc.body;
      // Use inner content to avoid nested full layouts
      body.innerHTML = content.innerHTML;

      // Remove nested nav/sidebar if present (safety)
      body.querySelectorAll("aside, header, nav").forEach(n => n.remove());

      // Bind cancel buttons inside loaded form
      body.querySelectorAll("a, button").forEach(el => {
        const t = (el.textContent || "").trim();
        if (t === "취소" || t === "목록으로" || el.classList.contains("btn-cancel")) {
          el.addEventListener("click", (e) => { e.preventDefault(); close(); });
        }
      });

      // Intercept the first form submit (edit)
      const form = body.querySelector("form");
      if (form) {
        form.addEventListener("submit", async (e) => {
          e.preventDefault();
          const fd = new FormData(form);
          const method = (form.getAttribute("method") || "post").toUpperCase();
          const action = form.getAttribute("action") || url;

          const submitRes = await fetch(action, {
            method,
            body: fd,
            headers: { "X-Requested-With": "XMLHttpRequest" }
          });

          // If server returns redirect, fetch will follow; just reload list for consistency
          if (submitRes.ok) {
            close();
            window.location.reload();
          } else {
            // Show returned html or error
            const errHtml = await submitRes.text();
            body.innerHTML = errHtml;
          }
        });
      }
    } catch (err) {
      body.innerHTML = `<div class="cp-modal__loading">로드 실패: ${String(err)}</div>`;
    }
  }

  document.addEventListener("click", (e) => {
    const a = e.target.closest("a.js-open-modal");
    if (!a) return;
    const href = a.getAttribute("href");
    if (!href || href === "#") return;
    e.preventDefault();
    loadIntoModal(href);
  });

  bindClose();
})();


// ===============================
// 403 Modal helpers (shared)
// ===============================
function openCpModal(title, bodyHtml, actionsHtml){
  const backdrop = document.getElementById('cp-modal-backdrop');
  const modal = document.getElementById('cp-modal');
  const titleEl = document.getElementById('cp-modal-title');
  const bodyEl = document.getElementById('cp-modal-body');
  const actionsEl = document.getElementById('cp-modal-actions');

  if(!backdrop || !modal || !titleEl || !bodyEl || !actionsEl){
    console.warn('[cp-modal] modal elements not found');
    return;
  }

  titleEl.textContent = title || '수정';
  bodyEl.innerHTML = bodyHtml || '';
  actionsEl.innerHTML = actionsHtml || '';

  backdrop.style.display = 'block';
  modal.style.display = 'block';
  modal.setAttribute('aria-hidden','false');

  // close on backdrop click
  backdrop.onclick = () => closeCpModal();
  // close on ESC
  document.addEventListener('keydown', cpModalEscClose, { once: true });

  // focus first input if exists
  setTimeout(() => {
    const first = modal.querySelector('input, select, textarea, button');
    if(first) first.focus();
  }, 0);
}
function cpModalEscClose(e){
  if(e.key === 'Escape') closeCpModal();
}
function closeCpModal(){
  const backdrop = document.getElementById('cp-modal-backdrop');
  const modal = document.getElementById('cp-modal');
  if(backdrop) backdrop.style.display = 'none';
  if(modal){
    modal.style.display = 'none';
    modal.setAttribute('aria-hidden','true');
  }
}

