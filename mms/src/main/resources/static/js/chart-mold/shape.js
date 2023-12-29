class Shape {
    constructor(container) {
        this.container = container;
        this.context = container.getContext();
        this.strokeColor = "#000";
        this.fillColor = "#FFF";
        this.lineWidth = 1;
    }

    setPosition(x, y) {
        this.x = this.container.getPositionX(x);
        this.y = this.container.getPositionY(y);
        return this;
    }

    setShapeStrokeColor(color) {
        this.strokeColor = color;
        return this;
    }

    setLineWidth(lineWidth) {
        this.lineWidth = lineWidth;
        return this;
    }

    setShapeFillColor(color) {
        this.fillColor = color;
        return this;
    }

    setShapeLineWidth(lineWidth) {
        this.lineWidth = lineWidth;
        return this;
    }

    draw() {
    }
}

class Line extends Shape {
    constructor(container) {
        super(container);
        this.endX = 0;
        this.endY = 0;
    }

    setEndPosition(endX, endY) {
        this.endX = this.container.getPositionX(endX);
        this.endY = this.container.getPositionY(endY);
        return this;
    }

    draw() {
        this.context.beginPath();
        this.context.strokeStyle = this.strokeColor;
        this.context.lineWidth = this.lineWidth;
        this.context.moveTo(this.x, this.y);
        this.context.lineTo(this.endX, this.endY);
        this.context.stroke();
    }
}

class CurveLine extends Line {
    constructor(container) {
        super(container);
        this.listPoint = [];
    }

    getListPoint(listPoint) {
        return this.listPoint;
    }

    setPosition(x, y) {
        this.listPoint = [
            new Point(this.container.getPositionX(x), this.container.getPositionY(y))
        ];
        return this;
    }

    setNextPoint(x, y) {
        this.listPoint.push(
            new Point(this.container.getPositionX(x), this.container.getPositionY(y))
        );
        return this;
    }

    getControlPoints(x0, y0, x1, y1, x2, y2, t) {
        //  x0,y0,x1,y1 are the coordinates of the end (knot) pts of this segment
        //  x2,y2 is the next knot -- not connected here but needed to calculate p2
        //  p1 is the control point calculated here, from x1 back toward x0.
        //  p2 is the next control point, calculated here and returned to become the
        //  next segment's p1.
        //  t is the 'tension' which controls how far the control points spread.

        //  Scaling factors: distances from this knot to the previous and following knots.
        var d01 = Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));
        var d12 = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

        var fa = t * d01 / (d01 + d12);
        var fb = t - fa;

        var p1x = x1 + fa * (x0 - x2);
        var p1y = y1 + fa * (y0 - y2);

        var p2x = x1 - fb * (x0 - x2);
        var p2y = y1 - fb * (y0 - y2);

        return [p1x, p1y, p2x, p2y]
    }

    drawControlLine(ctx, x, y, px, py) {
        //  Only for demo purposes: show the control line and control points.
        ctx.save();
        ctx.beginPath();
        ctx.lineWidth = 1;
        ctx.strokeStyle = "rgba(0,0,0,0.3)";
        ctx.moveTo(x, y);
        ctx.lineTo(px, py);
        ctx.closePath();
        ctx.stroke();
        this.drawPoint(ctx, px, py, 1.5, "#000000");
        ctx.restore();
    }

    drawPoint(ctx,x,y,r,color){
        ctx.save();
        ctx.beginPath();
        ctx.lineWidth=1;
        ctx.fillStyle='red';
        ctx.arc(x,y,r,0.0,2*Math.PI,false);
        ctx.closePath();
        ctx.stroke();
        ctx.fill();
        ctx.restore();
    }

    draw() {
        this.context.beginPath();
        let points = this.listPoint;
        if(points.length < 2) {
            return;
        }
        this.context.strokeStyle = this.strokeColor;
        this.context.lineWidth = this.lineWidth;

        // draw curve line
        let i = 0;
        let cp = [];
        let n = points.length * 2;
        let t = 0.5;
        let pts = [];
        let baseY = this.container.getPositionY(this.container.getBaseY());
        for (i = 0; i < points.length; i++) {
            pts.push(points[i].x);
            pts.push(points[i].y);
        }
        let color = this.strokeColor;

        for (i = 0; i < n - 4; i += 2) {
            cp = cp.concat(this.getControlPoints(pts[i], pts[i + 1], pts[i + 2], pts[i + 3], pts[i + 4], pts[i + 5], t));
        }

        let ctx = this.container.getContext();
        for (i = 2; i < pts.length - 5; i += 2) {
            ctx.strokeStyle = color;
            ctx.beginPath();
            ctx.moveTo(pts[i], pts[i + 1]);
            if ((pts[i + 3] === baseY && pts[i + 5] !== baseY) || (pts[i + 1] === baseY && pts[i + 3] === baseY)) {
                ctx.lineTo(pts[i + 2], pts[i + 3])
            }else {
                ctx.bezierCurveTo(cp[2 * i - 2], cp[2 * i - 1], cp[2 * i], cp[2 * i + 1], pts[i + 2], pts[i + 3]);
            }

            ctx.stroke();
            ctx.closePath();
        }
        //  For open curves the first and last arcs are simple quadratics.
        ctx.strokeStyle = color;
        ctx.beginPath();
        ctx.moveTo(pts[0], pts[1]);
        ctx.quadraticCurveTo(cp[0], cp[1], pts[2], pts[3]);
        ctx.stroke();
        ctx.closePath();

        ctx.strokeStyle = color;
        ctx.beginPath();
        ctx.moveTo(pts[n - 2], pts[n - 1]);
        ctx.quadraticCurveTo(cp[2 * n - 10], cp[2 * n - 9], pts[n - 4], pts[n - 3]);
        ctx.stroke();
        ctx.closePath();
    }
}

class CurveArea extends CurveLine {
    constructor(container) {
        super(container);
        this.listPoint = [];
    }

    setListPoint(listPoint) {
        this.listPoint = listPoint;
        return this;
    }

    draw() {
        // return;
        this.context.beginPath();
        let points = this.listPoint;
        if (points.length < 2) {
            return;
        }
        this.context.strokeStyle = this.strokeColor;
        this.context.globalAlpha = 0.7;
        this.context.moveTo(points[0].x, points[0].y);

        this.context.fillStyle = 'red';
        let cp = [];
        let n = points.length * 2;
        let t = 0.5;
        let pts = [];
        let i;
        let baseY = this.container.getPositionY(this.container.getBaseY());
        for (i = 0; i < points.length; i++) {
            pts.push(points[i].x);
            pts.push(points[i].y);
        }

        for (i = 0; i < n - 4; i += 2) {
            cp = cp.concat(this.getControlPoints(pts[i], pts[i + 1], pts[i + 2], pts[i + 3], pts[i + 4], pts[i + 5], t));
        }

        let color = this.fillColor;
        let ctx = this.container.getContext();
        for (i = 2; i < pts.length - 5; i += 2) {
            // for (i = 2; i < 4; i += 2) {
            ctx.strokeStyle = color;
            ctx.beginPath();
            ctx.moveTo(pts[i], pts[i + 1]);
            if ((pts[i + 3] === baseY && pts[i + 5] !== baseY) || (pts[i + 1] === baseY && pts[i + 3] === baseY)) {
                ctx.lineTo(pts[i + 2], pts[i + 3])
            }else {
                ctx.bezierCurveTo(cp[2 * i - 2], cp[2 * i - 1], cp[2 * i], cp[2 * i + 1], pts[i + 2], pts[i + 3]);

            }
            ctx.lineTo(pts[i + 2], baseY);
            ctx.lineTo(pts[i], baseY);
            ctx.lineTo(pts[i], pts[ i + 1]);
            ctx.fillStyle = color;
            ctx.fill();

            ctx.closePath();
        }
        // ctx.strokeStyle = color;
        ctx.beginPath();
        ctx.moveTo(pts[0], pts[1]);
        ctx.quadraticCurveTo(cp[0], cp[1], pts[2], pts[3]);

        ctx.lineTo(pts[2], baseY);
        ctx.lineTo(pts[0], baseY);
        ctx.lineTo(pts[0], pts[1]);
        ctx.fillStyle = color;
        ctx.fill();
        ctx.closePath();

        ctx.strokeStyle = color;
        ctx.beginPath();
        ctx.moveTo(pts[n - 2], pts[n - 1]);
        ctx.quadraticCurveTo(cp[2 * n - 10], cp[2 * n - 9], pts[n - 4], pts[n - 3]);

        ctx.lineTo(points[points.length - 2].x, points[points.length - 2].y);
        ctx.lineTo(points[points.length - 2].x, baseY);
        ctx.lineTo(points[points.length - 1].x, baseY);
        ctx.fillStyle = color;
        ctx.fill();
        ctx.closePath();

    }
}

class ChartCurveArea extends CurveArea {
    constructor(container) {
        super(container);
        this.fillColor = '#c5f6fb';
    }

    setListPoint(listPoint) {
        if (listPoint.length > 1) {
            // const firstPoint = listPoint[0];
            // const lastPoint = listPoint[listPoint.length - 1];
            // listPoint.unshift(new Point(firstPoint.x, this.container.getPositionY(this.container.getBaseY())));
            // listPoint.push(new Point(lastPoint.x, this.container.getPositionY(this.container.getBaseY())));
            this.listPoint = listPoint;
        }
        return this;
    }
}

class Rectangle extends Shape {
    constructor(container) {
        super(container);
        this.width = 0;
        this.height = 0;
    }

    setDimension(width, height) {
        this.width = width * this.container.getScale();
        this.height = height * this.container.getScale();
        return this;
    }

    draw() {
        this.context.beginPath();
        this.context.rect(this.x, this.y - this.height, this.width, this.height);
        this.context.fillStyle = this.fillColor;
        this.context.fill();
        if(this.strokeColor) {
            this.context.strokeStyle = this.strokeColor;
            this.context.lineWidth = this.lineWidth;
            this.context.strokeRect(this.x, this.y - this.height, this.width, this.height);
        }
    }
    getRealY() {
      return this.container.getHeight() * this.container.getScale() - this.y;
    }

    isContainPoint(point) {
        let realY = this.getRealY();
        const x = (this.x <= point.x && point.x <= this.x + this.width);
        const y = (realY <= point.y && point.y <= realY +this.height);
        return x && y
    }
}

class ChartHintRectangle extends Rectangle {
    constructor(container) {
        super(container);
    }

    draw() {
        this.context.beginPath();
        this.context.rect(this.x, this.y - this.height, this.width, this.height);
        this.context.fillStyle = this.fillColor;
        this.context.fill();
        this.context.strokeStyle = this.strokeColor;
        this.context.lineWidth = this.lineWidth;
        this.context.stroke();
    }
}

class Circle extends Shape {
    setRadius(radius) {
        this.radius = radius;
        return this;
    }

    draw() {
        this.context.beginPath();
        this.context.strokeStyle = this.strokeColor;
        this.context.lineWidth = this.lineWidth;
        this.context.arc(this.x, this.y, this.radius * this.container.getScale(), 0, 2 * Math.PI);
        this.context.stroke();
    }
}

class ChartText extends Shape {
    constructor(container) {
        super(container);
        this.fontSize = 12;
        this.fontSize = this.fontSize * this.container.getScale();
        this.text = "";
        this.fontWeight = 'bold';
        this.fontFamily = 'Helvetica';
        this.rotate = 0;
        this.translateX = 0;
        this.translateY = 0;
    }

    setFontSize(fontSize) {
        this.fontSize = fontSize * this.container.getScale();
        return this;
    }

    setText(text) {
        this.text = text;
        return this;
    }

    setRotate(rotate) {
        this.rotate = rotate;
        return this;
    }

    setTranslate(translateX, translateY) {
        this.translateX = translateX * this.container.getScale();
        this.translateY = -translateY * this.container.getScale();
        return this;
    }

    setFontWeight(fontWeight) {
        this.fontWeight = fontWeight;
        return this;
    }

    draw() {
        this.context.save();
        this.context.fillStyle = this.strokeColor;
        this.context.font = `500 ${this.fontSize}px Helvetica`;
        this.context.rotate(this.rotate);
        this.context.translate(this.translateX, this.translateY);
        this.context.fillText(this.text, this.x, this.y);
        this.context.restore();
    }
}

class ChartUnitText extends ChartText {
    constructor(container) {
        super(container);
        this.fontWeight = 'lighter';
        this.strokeColor = '#777';
    }
}