// area container graph element, it is a rectangle
class GraphElement {
    constructor(container) {
        this.container = container;
        this.topRight = null;
        this.bottomLeft = null;
        this.data = null;
        this.order = 0;
    }

    setData(data) {
        this.data = data;
        return this;
    }

    getData() {
        return this.data;
    }

    setOrder(order) {
        this.order = order;
        return this;
    }

    getOrder() {
        return this.order;
    }

    getBottomLeftPosition() {
        return this.bottomLeft;
    }

    setTopRightPosition(x, y){
        this.topRight = new Point(x * this.container.getScale(), y*this.container.getScale());
        return this;
    }

    setBottomLeftPosition(x, y){
        this.bottomLeft = new Point(x*this.container.getScale(), y*this.container.getScale());
        return this;
    }


    // test a point is in this element
    contain(point) {
        const positionX = point.x;
        const positionY = point.y;
        return this.bottomLeft.x - 5 <= positionX && positionX <= this.topRight.x + 5 && this.bottomLeft.y - 5 <= positionY && positionY <= this.topRight.y + 5;
    }
}