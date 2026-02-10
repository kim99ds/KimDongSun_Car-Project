// ========헤더=======================================================
$(function () {
    const $header = $(".site-header");
    const $allSub = $(".sub-menu");
    const $dropBg = $(".header-drop-bg");

    // 헤더 클릭 시 전체 서브메뉴 토글
    $header.on("click", function (e) {
        // 링크 클릭은 막지 않으려면 gnb 영역만 제한
        if (!$(e.target).closest(".gnb").length) return;

        if ($allSub.is(":visible")) {
            $allSub.stop().slideUp(350);
            $dropBg.stop().slideUp(350);
        } else {
            $allSub.stop().slideDown(350);
            $dropBg.stop().slideDown(350);
        }
    });

    // 바깥 클릭하면 닫기
    $(document).on("click", function (e) {
        if (!$(e.target).closest(".site-header").length) {
            $allSub.stop().slideUp(200);
            $dropBg.stop().slideUp(200);
        }
    });
});

$(document).ready(function() {
    setTimeout(function() {
        $('.resultCard').addClass('active');
    }, 100);
});

$(document).on("click", ".flip-card .submit-btn", function(e){
  e.stopPropagation(); // 카드 뒤집기 이벤트로 전파만 차단
});

  $(document).on("click", ".flip-card", function () {
    $(this).toggleClass("flipped");
  });
