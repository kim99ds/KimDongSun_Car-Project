/**
 * cars-detail.popular.js
 * - 파퓰러 패키지 I/II 강제 단일선택 (기존 change 리스너를 그대로 태워서 동기화)
 */
(function(){
  const POPULAR_IDS = new Set([21412, 21413]);
  let syncing = false;

  function getPkgId(cb){
    const id = Number(
      cb?.dataset?.packageId ??
      cb?.dataset?.packageid ??
      cb?.dataset?.pkgId ??
      cb?.dataset?.id
    );
    return Number.isNaN(id) ? null : id;
  }

  function labelText(cb){
    const lab = cb?.closest('label');
    return (lab?.textContent || '').replace(/\s+/g,' ').trim();
  }

  function isPopular(cb){
    const id = getPkgId(cb);
    if (id != null && POPULAR_IDS.has(id)) return true;

    const t = labelText(cb);
    return t.includes('파퓰러 패키지 I') || t.includes('파퓰러 패키지 II') || (t.includes('파퓰러') && t.includes('패키지'));
  }

  document.addEventListener('change', (e)=>{
    const t = e.target;
    if (syncing) return;

    if (!(t instanceof HTMLInputElement)) return;
    if (!t.classList.contains('package-checkbox')) return;
    if (!t.checked) return;
    if (!isPopular(t)) return;

    syncing = true;
    try {
      document.querySelectorAll('#package-area .package-checkbox:checked').forEach(other=>{
        if (other === t) return;
        if (!isPopular(other)) return;

        // 포함 잠금은 풀면 안 됨
        if (other.dataset.pkgLocked === "Y") return;

        other.checked = false;
        other.dispatchEvent(new Event('change', { bubbles:true }));
      });

    } finally {
      syncing = false;
    }
  }, true);
})();
