Common.removeSkeleton = function () {
    let waveList = document.getElementsByClassName('wave');
    let wave1List = document.getElementsByClassName('wave1');
    let waveSidebarList = document.getElementsByClassName('wave_sidebar');
    let waveHeaderList = document.getElementsByClassName('wave_header');
    let waveImgList = document.getElementsByClassName('wave_img');

    removeElementClass(waveList, 'wave');
    removeElementClass(wave1List, 'wave1');
    removeElementClass(waveSidebarList, 'wave_sidebar');
    removeElementClass(waveHeaderList, 'wave_header');
    removeElementClass(waveImgList, 'wave_img');

    function removeElementClass(elementList, className) {
        for (let i = 0; i < elementList.length; i++) {
            elementList.item(i).classList.remove(className);
        }
    }
};
