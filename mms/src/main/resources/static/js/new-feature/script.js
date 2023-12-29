class HoleRectangle {
    constructor(start, width, height) {
        this.start = start;
        this.width = width;
        this.height = height;
    }


    getStart() {
        return this.start;
    }

    getWidth() {
        return this.width;
    }

    getHeight() {
        return this.height;
    }
}

class HolePopup {
    constructor(holeRectangle) {
        this.holeRectangle = holeRectangle;
    }

    build() {
        this.calculateTopBackdrop();
        this.calculateMainBackdrop();
    }

    calculateTopBackdrop() {
        let height = this.getTopBackdropHeight();
        $('.first-row').css('height', height +'px');
    }

    getTopBackdropHeight() {
        return this.holeRectangle.getStart().y;
    }

    calculateMainBackdrop() {
        // set main row
        let height = this.getMainBackdropHeight();
        $('.main-row').css('height', height + 'px');

        // set main element
        let mainHoleBackdrop = new MainHoleBackdrop(this.holeRectangle);
        mainHoleBackdrop.build();
    }

    getMainBackdropHeight() {
        return this.holeRectangle.getHeight();
    }
}

class MainHoleBackdrop {
    constructor(holeRectangle) {
        this.holeRectangle = holeRectangle;
    }
    build() {
        this.calculateLeftWidth();
        this.calculateMainWidth();
    }
    calculateLeftWidth() {
        let width = this.getLeftWidth();
        $('.left-cell').css('width', width + 'px');
    }
    getLeftWidth() {
        return this.holeRectangle.getStart().x;
    }

    calculateMainWidth() {
        let width = this.getMainWidth();
        $('.main-cell').css('width', width + 'px');
    }
    getMainWidth() {
        return this.holeRectangle.getWidth();
    }
}
// let holeRectangle = new HoleRectangle(null, null);
// let holePopup = new HolePopup(holeRectangle);
// holePopup.build();




