window.onload = function () {
    document.title = 'User' + ' | eMoldino';
    setTimeout(() => {
        $('div').removeClass('wave_sidebar');
        $('div').removeClass('profile_wave1');
        $('div').removeClass('profile_wave2');
        $('div').removeClass('profile_wave3');
        $('div').removeClass('profile_wave4');
        $('li').removeClass('wave_header');
        $('img').removeClass('wave_img');
        document.getElementById('remove_profile').remove();
        $('div').removeClass('hide_account');
    }, 200);
};
