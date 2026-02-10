(function () {
  function els() {
    // ✅ HTML id에 맞춤
    const modal = document.getElementById('policy_edit_modal');
    const form = document.getElementById('policyEditForm');

    const policyIdInput = document.getElementById('edit_policy_id');
    const eventIdInput = document.getElementById('edit_policy_event_id');
    const typeSelect = document.getElementById('edit_policy_type');
    const valueInput = document.getElementById('edit_policy_value');

    return { modal, form, policyIdInput, eventIdInput, typeSelect, valueInput };
  }

  function bindCloseHandlersOnce(modal) {
    if (!modal || modal.dataset.bound === '1') return;
    modal.dataset.bound = '1';

    // dialog 백드롭 클릭 닫힘은 <form method="dialog" class="modal-backdrop"> 로 처리되므로 굳이 안해도 됨
    document.addEventListener('keydown', (e) => {
      if (e.key === "Escape" && typeof modal.open === "boolean" && modal.open) {
        closeModal();
      }
    });

    // 취소 버튼(HTML에 있는 닫기 버튼)도 close 처리
    const cancelBtn = modal.querySelector(".btn-custom.btn-neutral");
    if (cancelBtn) {
      cancelBtn.addEventListener('click', (e) => {
        // HTML onclick으로 close() 이미 하고 있지만 중복 방어
        e.preventDefault();
        closeModal();
      });
    }
  }

  function setDiscountTypeSafely(typeSelect, rawType) {
    if (!typeSelect) return;

    const incoming = String(rawType || '').trim().toUpperCase(); // RATE/PRICE
    if (!incoming) return;

    const values = Array.from(typeSelect.options).map(o => o.value);
    if (values.includes(incoming)) {
      typeSelect.value = incoming;
      return;
    }

    // 못 찾으면 그냥 비움
    console.warn('[policy-modal] cannot match discountType:', incoming, 'option values=', values);
  }

  function openModal(data) {
    const { modal, form, policyIdInput, eventIdInput, typeSelect, valueInput } = els();
    if (!modal || !form) {
      console.warn('[policy-modal] modal/form not found (id mismatch)');
      return;
    }

    bindCloseHandlersOnce(modal);

    const pid = String(data.policyId || '').trim();
    const eid = String(data.eventId || '').trim();

    if (policyIdInput) policyIdInput.value = pid;
    if (eventIdInput) eventIdInput.value = eid;

    setDiscountTypeSafely(typeSelect, data.type);

    if (valueInput) valueInput.value = data.value || '';

    // ✅ 컨트롤러로 POST 나가게 action 세팅
    if (pid) {
      form.action = '/admin/event_manage/policies/' + encodeURIComponent(pid);
    } else {
      console.warn('[policy-modal] policyId is empty. check data-policy-id binding');
    }

    if (typeof modal.showModal === "function") {
      if (!modal.open) modal.showModal();
    }
  }

  function closeModal() {
    const { modal } = els();
    if (!modal) return;
    if (typeof modal.close === "function" && modal.open) modal.close();
  }

  // ✅ 다른 클릭 핸들러보다 먼저 잡아서 페이지 이동을 확실히 막음
  document.addEventListener('click', (e) => {
    const btn = e.target.closest('.js-policy-edit');
    if (!btn) return;

    e.preventDefault();
    e.stopPropagation();

    openModal({
      policyId: btn.getAttribute('data-policy-id') || '',
      eventId: btn.getAttribute('data-event-id') || '',
      type: btn.getAttribute('data-policy-type') || '',
      value: btn.getAttribute('data-policy-value') || ''
    });
  }, true);
})();
