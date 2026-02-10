$(document).ready(function(){
    
    // 1. 탭 메뉴 전환 기능
    $('.mypage-nav ul li a').on('click', function(e){
        e.preventDefault();
        
        // 메뉴 활성화 스타일 변경
        $('.mypage-nav ul li').removeClass('active');
        $(this).parent('li').addClass('active');

        // 탭 콘텐츠 전환
        var tab_id = $(this).attr('data-tab');
        $('.tab-content').removeClass('current');
        $('#' + tab_id).addClass('current');
    });

    // 2. 회원 정보 수정 - [수정] 버튼 클릭 시
    $('.btn-modify').on('click', function(){
        $(this).hide();
        $('.edit-btn-group').show();
        $('.edit-form input:not(.always-readonly)').prop('readonly', false);
        $('.edit-form select').prop('disabled', false);
        $('.hidden-in-view').show();
        $('#uname').focus();
    });

    // 3. 회원 정보 수정 - [취소] 버튼 클릭 시
    $('.btn-cancel').on('click', function(){
        $('.edit-btn-group').hide();
        $('.btn-modify').show();
        $('.edit-form input').prop('readonly', true);
        $('.edit-form select').prop('disabled', true);
        $('.hidden-in-view').hide();
    });

    // 4. 회원 탈퇴 버튼 클릭
    $('.btn-withdraw').on('click', function(){
        // 체크박스 확인
        if(!$('#agree-withdraw').is(':checked')){
            alert('탈퇴 유의사항에 동의해 주세요.');
            return;
        }
        
        // 비밀번호 입력 확인
        if($('#wd-pw').val() === ''){
            alert('비밀번호를 입력해 주세요.');
            $('#wd-pw').focus();
            return;
        }

        if(confirm('정말로 탈퇴하시겠습니까? 탈퇴 후 정보는 복구할 수 없습니다.')){
            alert('탈퇴 처리가 완료되었습니다. 이용해 주셔서 감사합니다.');
            window.location.href = 'index.html'; // 메인으로 이동
        }
    });
});