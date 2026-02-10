$(document).ready(function () {
    
    $('.filter-head').click(function () {
        $(this).toggleClass('closed');
        $(this).next('.filter-body').slideToggle(300);
    });

    const itemsPerPage = 9;
    const $cards = $('.car-wrap .car-card');
    const $pageBtns = $('.page-btn');

    $cards.hide();
    $cards.slice(0, itemsPerPage).show();

    $pageBtns.click(function() {
        $pageBtns.removeClass('active');
        $(this).addClass('active');

        const pageNum = $(this).data('page'); 
        const start = (pageNum - 1) * itemsPerPage; 
        const end = start + itemsPerPage;

        $cards.hide();
        $cards.slice(start, end).fadeIn(300);
    });

});