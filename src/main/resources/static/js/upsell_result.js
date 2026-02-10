(function(){
    // ✅ Controller가 내려준 JSON을 안전하게 파싱
    let list = [];
    try{
        const raw = document.getElementById('candJson')?.textContent || '[]';
        list = JSON.parse(raw);
    }catch(e){
        list = [];
    }

    let idx = 0;

    const surface = document.getElementById('candSurface');
    const candImg  = document.getElementById('candImg');
    const candName = document.getElementById('candName');
    const candPrice= document.getElementById('candPrice');
    const ghostImg = document.getElementById('ghostImg');

    const prev = document.querySelector('.nav.prev');
    const next = document.querySelector('.nav.next');

    function fmt(n){
        const v = Number(n);
        if (Number.isFinite(v)) return v.toLocaleString('ko-KR') + '원';
        return (n ?? '-') + '';
    }

    function label(c){
        const b = c.brandName ? (c.brandName + ' ') : '';
        const m = c.modelName || c.trimName || '';
        return (b + m).trim() || '추천 차량';
    }

    function setCurrent(c){
        candImg.src = c.imageUrl || '/images/santafe1.webp';
        candName.textContent = label(c);
        candPrice.textContent = fmt(c.basePrice);
    }

    function setPeek(c){
        ghostImg.src = (c && c.imageUrl) ? c.imageUrl : '/images/santafe1.webp';
    }

    function render(initial=false){
        if (!Array.isArray(list) || list.length === 0){
            candName.textContent = '추천 차량이 없습니다';
            candPrice.textContent = '-';
            candImg.src = '/images/santafe1.webp';
            setPeek(null);
            return;
        }

        const cur = list[idx];
        const nxt = list[(idx + 1) % list.length];

        if (initial){
            setCurrent(cur);
            setPeek(nxt);
            return;
        }

        surface.classList.remove('fade-in');
        surface.classList.add('fade-out');

        setTimeout(() => {
            setCurrent(cur);
            setPeek(nxt);

            surface.classList.remove('fade-out');
            surface.classList.add('fade-in');
        }, 160);
    }

    prev?.addEventListener('click', () => {
        if (!list.length) return;
        idx = (idx - 1 + list.length) % list.length;
        render(false);
    });

    next?.addEventListener('click', () => {
        if (!list.length) return;
        idx = (idx + 1) % list.length;
        render(false);
    });

    render(true);
})();
