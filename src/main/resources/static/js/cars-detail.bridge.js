/**
 * cars-detail.bridge.js
 * - 엔진/트림/툴팁: select ↔ legacy radio 브릿지
 * - "선택된 엔진(variant) 아래 trim-container"의 트림만 select에 노출
 */
document.addEventListener('DOMContentLoaded', () => {
    const engineSel = document.getElementById('engine');
    const trimSel   = document.getElementById('trim');
    const trimHelp  = document.getElementById('trimHelp');

    function syncTrimTooltip() {
        if (!trimHelp || !trimSel) return;
        
        const opt = trimSel.options[trimSel.selectedIndex];
        const desc = (opt && opt.dataset && opt.dataset.desc) ? String(opt.dataset.desc).trim() : '';
        trimHelp.dataset.tooltip = desc || '트림 설명이 등록되어 있지 않습니다.';
    }

    /**
     * 현재 선택된 엔진(variant)에 속한 trim 라디오만 가져오기
     * (legacy DOM 구조: variant div 안에 label(variant radio) + .trim-container(트림들))
     */
    function getActiveTrimRadios() {
        const checkedVariant = document.querySelector('input[name="variantId"]:checked');

        // 1) 가장 확실한 방법: 선택된 variant가 들어있는 "variant 블록"에서 trim-container 찾기
        if (checkedVariant) {
            const variantBlock = checkedVariant.closest('div'); // th:each variant 최상위 div
            if (variantBlock) {
                const radios = Array.from(
                    variantBlock.querySelectorAll('.trim-container input[name="trimId"]')
                );
                if (radios.length) return radios;
            }
        }

        // 2) fallback: 화면에서 "표시 중인 trim-container" 안의 라디오만
        const visibleContainers = Array.from(document.querySelectorAll('.trim-container'))
            .filter(c => {
                const cs = window.getComputedStyle(c);
                return cs && cs.display !== 'none' && cs.visibility !== 'hidden';
            });

        const visibleRadios = visibleContainers.flatMap(c =>
            Array.from(c.querySelectorAll('input[name="trimId"]'))
        );

        return visibleRadios;
    }

    function syncTrimSelectFromRadios() {
        if (!trimSel) return;

        const radios = getActiveTrimRadios();

        // 트림 select 초기화
        const opts = ['<option value="">트림 선택</option>'];

        if (radios.length === 0) {
            trimSel.innerHTML = opts.join('');
            syncTrimTooltip();
            return;
        }

        radios.forEach(r => {
            const label = r.closest('label');
            const text = label ? label.textContent.replace(/\s+/g, ' ').trim() : r.value;

            const rawDesc = (r.dataset && r.dataset.desc) ? String(r.dataset.desc) : '';
            const safeDesc = rawDesc.replace(/"/g, '&quot;');

            // 라디오가 체크되어 있으면 select에도 반영
            const selectedAttr = r.checked ? ' selected' : '';
            opts.push(`<option value="${r.value}" data-desc="${safeDesc}"${selectedAttr}>${text}</option>`);
        });

        trimSel.innerHTML = opts.join('');
        syncTrimTooltip();
    }

    // 엔진 select -> variantId 라디오 change
    engineSel?.addEventListener('change', () => {
        const v = engineSel.value;

        // 트림 select 먼저 비워서 "안보이는 것처럼" 되는 문제 방지
        if (trimSel) trimSel.innerHTML = '<option value="">트림 선택</option>';
        syncTrimTooltip();

        const r = document.querySelector(`input[name="variantId"][value="${v}"]`);
        if (r) {
            r.checked = true;
            r.dispatchEvent(new Event('change', { bubbles: true }));
        }

        // 기능 JS가 trim-container를 토글한 뒤에 select를 다시 채움
        setTimeout(() => {
            syncTrimSelectFromRadios();
            syncTrimTooltip();
        }, 0);
    });

    // 트림 select -> trimId 라디오 change
    trimSel?.addEventListener('change', () => {
        syncTrimTooltip();
        const t = trimSel.value;
        const r = document.querySelector(`input[name="trimId"][value="${t}"]`);
        if (r) {
            r.checked = true;
            r.dispatchEvent(new Event('change', { bubbles: true }));
        }
    });

    // variant 변경 후(기능 JS가 trim-container 표시 제어) → 트림 select 재구성
    document.addEventListener('change', (e) => {
        if (e.target && e.target.name === 'variantId') {
            setTimeout(() => {
                syncTrimSelectFromRadios();
                syncTrimTooltip();
            }, 0);
        }
    });

    // 초기 1회
    setTimeout(() => {
        syncTrimSelectFromRadios();
        syncTrimTooltip();
    }, 0);
});
