axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';
window.onload = () => {
    document.title = 'Tooling' + ' | eMoldino';
    setTimeout(() => {
        $('div').removeClass('wave');
        $('div').removeClass('wave1');
        $('div').removeClass('wave_sidebar');
        $('li').removeClass('wave_header');
        $('img').removeClass('wave_img');
    }, 500);
};

$(() => {
    var selectTarget = $('.selectbox select');

    selectTarget.on({
        focus: () => {
            $(this).parent().addClass('focus');
        },
        blur: () => {
            $(this).parent().removeClass('focus');
        },
    });

    selectTarget.change(() => {
        var select_name = $(this).children('option:selected').text();
        $(this).siblings('label').text(select_name);
    });
});