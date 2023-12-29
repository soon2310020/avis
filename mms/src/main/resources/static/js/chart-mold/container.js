// hourly chart, using for cycle time analysis and temperature analysis
class Container {
    constructor(containerId) {
        this.containerId = containerId;
        this.scale = 2;
        this.baseX = 40;
        this.baseY = 100;
        this.hintElement = document.getElementById('chart-hint');
        this.circlePoint = document.getElementById('circle-point');
        this.shotCountHint = document.getElementById('shot-count');
        this.shotHintHeader = document.getElementById('count-header');
        this.init();
    }

    getWidth() {
        return this.width;
    }

    getHeight() {
        return this.height;
    }

    getPositionX(x) {
        return x * this.scale;
    }

    getPositionY(y) {
        return (this.height - y) * this.scale;
    }

    getContext() {
        return this.context;
    }

    getScale() {
        return this.scale;
    }

    getBaseX(){
        return this.baseX;
    }

    setBaseX(baseX) {
        this.baseX = baseX;
    }

    getBaseY(){
        return this.baseY;
    }

    getHintElement() {
        return this.hintElement;
    }

    getCirclePoint() {
        return this.circlePoint;
    }

    getShotCountHint() {
        return this.shotCountHint;
    }

    getShotHintHeader() {
        return this.shotHintHeader;
    }

    setHoverListener(callback){
        this.canvas.onmousemove = (e) => {
            this.hintElement.style.display = 'none';
            this.circlePoint.style.display = 'none';
            const x = e.layerX;
            const y = e.layerY;
            callback(x, y);
        }
    }

    setClickListener(callback){
        this.canvas.onclick = (e) => {
            const x = e.layerX;
            const y = e.layerY;
            callback(x, y);
        }
    }

    init() {
        this.canvas = document.getElementById(this.containerId);
        const parent = document.getElementById(this.containerId).parentElement;
        this.parent = parent;
        this.width = parent.offsetWidth;
        this.height = parent.offsetHeight;
        const canvasWidth = this.width * this.scale;
        const canvasHeight = this.height * this.scale;
        this.canvas.setAttribute("width", canvasWidth + '');
        this.canvas.setAttribute("height", canvasHeight + '');
        this.canvas.style.width = `${this.width}px`;
        this.canvas.style.height = `${this.height}px`;

        this.context = this.canvas.getContext("2d");
    }

    reset() {
        this.context.clearRect(0, 0, this.width * this.scale, this.height * this.scale);
    }
}