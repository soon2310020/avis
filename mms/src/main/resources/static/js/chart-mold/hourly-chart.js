class HourlyChart {
    constructor(containerId, data, graphType,parent) {
        this.rightOffset = 33;
        this.topOffset = 20;
        this.container = new Container(containerId);
        this.containerId = containerId;
        this.baseX = this.container.getBaseX();
        this.baseY = this.container.getBaseY();
        this.data = data;
        this.verticalAxis = [];
        this.unitHeight = 0;
        this.pointHeight = 16;
        this.verticalAxisName = 'Vertical Title';
        this.horizontalAxisName = 'Hour';
        this.rightVerticalAxisName = 'Shot Quantity';
        this.axisFontSize = 13;
        this.graphColor = '#3c763d';
        this.straightThroughColor = '#000';

        // difference between two point
        this.rightOffset = 53;
        this.offsetLength = Math.floor((this.container.getWidth() - this.baseX - this.rightOffset) / 24);
        this.listElementGraph = [];
        this.listCircleElementPoint = [];
        this.listCircleElementPoint2 = [];
        this.currentCircleElement = null;
        this.rightVerticalAxis = [];
        this.legendPositions = {};
        this.legendTypes = {
            GRAPH: 'GRAPH',
            TEMPERATURE: 'TEMPERATURE'
        };
        this.showingElement = {
            GRAPH: true,
            TEMPERATURE: true
        };
        this.graphType = graphType;
        this.parent = parent;
        this.legendTextColor = '#666666';
        this.setVerticalAxis();
        this.setEventListener();
        this.rightUnitHeight = 0;
        this.setRightVerticalAxis();
        if (this.isSafari()) {
            console.log('is safari');
        }else {
            console.log('is not safari');
        }
    }

    // get cycle axis depend on current data
    setVerticalAxis() {
        let smallestValue = this.getSmallestData(0);
        let biggestValue = this.getBiggestData(2);

        // divide pivot with 10 unit
        let lowIndex = Math.floor(smallestValue / 10);
        let highIndex = Math.floor(biggestValue / 10);
        for(let  i = lowIndex - 1; i <= highIndex + 1; i++ ) {
            this.verticalAxis.push(i  * 10);
        }
    }
    isSafari() {
        return /constructor/i.test(window.HTMLElement) || (function (p) { return p.toString() === "[object SafariRemoteNotification]"; })(!window['safari'] || (typeof safari !== 'undefined' && window['safari'].pushNotification));
    }
    getSafariOffsetY() {
        return 60;
    }

    getBiggestData(index) {
        let biggestValue = Number.MIN_SAFE_INTEGER;
        this.data.map(element => {
            if (biggestValue < element[index]) {
                biggestValue = element[index];
            }
        });

        if (biggestValue === Number.MIN_SAFE_INTEGER) {
            return 0;
        }
        return biggestValue;
    }

    setLegendPosition(type, x, y, width, height) {
        let shape = new Rectangle(this.container);
        shape.setPosition(x, y).setDimension(width, height);
        this.legendPositions[type] = shape;
    }

    getSmallestData(index){
        let smallestValue = Number.MAX_SAFE_INTEGER;
        this.data.map(element => {
            if (smallestValue > element[index]) {
                smallestValue = element[index];
            }
        });
        if (smallestValue === Number.MAX_SAFE_INTEGER) {
            return 0;
        }
        return smallestValue;
    }

    getSmallestDataGreaterThanZero(index){
        let smallestValue = Number.MAX_SAFE_INTEGER;
        this.data.map(element => {
            if (smallestValue > element[index] && element[index] > 0) {
                smallestValue = element[index];
            }
        });
        if (smallestValue === Number.MAX_SAFE_INTEGER) {
            return 0;
        }
        return smallestValue;
    }

    drawVerticalAxis(){
        const line = new Line(this.container);
        line.setPosition(this.baseX, this.baseY)
            .setEndPosition(this.baseX, this.container.getHeight())
            .draw();

        const text = new ChartUnitText(this.container);
        for (let i = 1; i < this.verticalAxis.length; i++) {
            let positionX1 = this.baseX;
            let positionY1 = this.baseY + this.unitHeight * (this.verticalAxis[i] - this.verticalAxis[0]);
            let positionX2 = this.baseX + this.container.getWidth();
            // line.setPosition(positionX1, positionY1)
            //         .setEndPosition(positionX2, positionY1)
            //         .draw();

            // draw text position
            text.setPosition(positionX1 - 5*this.verticalAxis[i].toString().length - 10, positionY1 - 3)
                .setText(this.verticalAxis[i])
                .setFontSize(13)
                .draw();
        }

        // draw axis name
        text.setText(this.verticalAxisName).setPosition(0, 0).setTranslate(-(this.container.getHeight()/2 - 30),this.container.getHeight() - 15).setRotate(-Math.PI/2).setFontSize(this.axisFontSize).draw();
    }

    drawHorizontalAxis(){
        const line = new Line(this.container);
        line.setPosition(this.baseX, this.baseY - 1)
            .setEndPosition(this.container.getWidth() - this.rightOffset, this.baseY - 1)
            .draw();
        const pointLine = new Line(this.container);
        const pointText = new ChartUnitText(this.container);
        for (let i = 0; i < 24; i++) {
            let text = i;
            if(text < 10) {
                text = '0' + i;
            }
            // draw point
            // pointLine.setPosition(this.baseX + this.offsetLength * (i + 0.5), this.baseY + this.pointHeight / 2)
            //     .setEndPosition(this.baseX + this.offsetLength * (i + 0.5), this.baseY - this.pointHeight / 2)
            //     .draw();
            // draw hint of point
            pointText.setPosition(this.baseX + this.offsetLength * (i + 0.5) - 7, this.baseY - this.pointHeight / 2 - 15)
                .setText(text)
                .draw();
        }

        // draw axis name
        let text = new ChartText(this.container);
        text.setText(this.horizontalAxisName).setPosition(this.container.getWidth()/2 - 10, this.baseY - 50).setFontSize(this.axisFontSize).setShapeStrokeColor('#666666').draw();
    }

    drawGridDivider() {
        // draw horizontal grid line
        this.drawHorizontalLineDivider();
        // draw vertical grid line
        this.drawVerticalLineDivider();
    }

    drawHorizontalLineDivider(){
        const line = new Line(this.container);
        const countUnit = this.verticalAxis[1] - this.verticalAxis[0];
        for(let i=0; i< this.verticalAxis.length; i++) {
            let positionStartX = this.baseX - 5;
            let positionY = this.baseY + (countUnit*i)* this.unitHeight;
            let positionEndX = this.container.getWidth() - this.rightOffset + 5;

            line.setPosition(positionStartX, positionY).setEndPosition(positionEndX, positionY).setShapeStrokeColor('#CDCDCD').draw();
        }

    }

    drawVerticalLineDivider(){
        const line = new Line(this.container);
        const countVerticalLine = 24;
        for(let i=0; i<countVerticalLine; i++) {
            line.setPosition(this.baseX + this.offsetLength * (i + 1), this.baseY - this.pointHeight / 2)
                .setEndPosition(this.baseX + this.offsetLength * (i + 1), this.container.getHeight())
                .setShapeStrokeColor('#CDCDCD')
                .draw();
        }
    }

    drawStraightLine(x, y, width) {
        let straightThroughLine = new Line(this.container);
        straightThroughLine.setPosition(x, y)
            .setEndPosition(x + width, y)
            .setShapeStrokeColor(this.straightThroughColor)
            .draw();
    }

    getHeightUnit(distance) {
        let logarithmUnit = Math.floor(Math.log10(distance));

        let k = Math.ceil(distance / Math.pow(10, logarithmUnit));
        let unit = 0;
        if (k <= 2) {
            unit = 2 * Math.pow(10, logarithmUnit - 1);
        }else if (k <= 5) {
            unit  = 5 * Math.pow(10, logarithmUnit - 1);
        }else {
            unit = 10 * Math.pow(10, logarithmUnit - 1);
        }

        if (unit < 1) {
            unit = 1;
        }

        return unit;
    }

    setRightVerticalAxis(){

        let smallestValue = this.getSmallestData(3);
        let biggestValue = this.getBiggestData(3);
        // set default when not exist
        if (!biggestValue && !smallestValue || !this.showingElement.TEMPERATURE) {
            biggestValue = 10;
            smallestValue = 0;
        }

        let difference = biggestValue - smallestValue;
        if (difference === 0) {
            difference = 0.1;
        }
        let unit = Number(this.getHeightUnit(difference).toFixed(1));

        let lowIndex = Math.floor(smallestValue / unit);
        let highIndex = Math.floor(biggestValue / unit);
        if (lowIndex === 0) {
            lowIndex = 1;
        }
        for(let  i = lowIndex - 1; i <= highIndex + 1; i++ ) {
            this.rightVerticalAxis.push(Number(i*unit).toFixed(1).replace('.0', ''));
        }
        this.rightUnitHeight = (this.container.getHeight() - this.baseY - this.topOffset)/((highIndex - lowIndex + 2) * unit);

        // reset baseX depend biggest value
        this.rightOffset = (Number(biggestValue).toFixed(1).replace('.0', '') + '').length * 5 + 30;
    }

    drawRightVertical(){
        // draw line
        const line = new Line(this.container);
        line.setPosition(this.container.getWidth() - this.rightOffset, this.baseY)
            .setEndPosition(this.container.getWidth() - this.rightOffset, this.container.getHeight())
            .draw();
        // draw point
        const text = new ChartUnitText(this.container);
        let positionX1 = this.container.getWidth() - this.rightOffset;
        for (let i = 1; i < this.rightVerticalAxis.length; i++) {
            let positionY1 = this.baseY + this.rightUnitHeight * (this.rightVerticalAxis[i] - this.rightVerticalAxis[0]);
            // draw text position
            text.setPosition(positionX1 + 5, positionY1 - 5)
                .setText(this.rightVerticalAxis[i])
                .setFontSize(this.axisFontSize).draw();
        }

        text.setText(this.rightVerticalAxisName).setPosition(0, 0).setTranslate(this.container.getHeight()/2 - 100,this.container.getWidth() + this.container.getHeight() - 10).setRotate(Math.PI/2).setFontSize(this.axisFontSize).draw();
        // clear waste line
        line.setPosition(this.container.getWidth() - this.rightOffset, this.baseY)
            .setEndPosition(this.container.getWidth(), this.baseY)
            .setShapeStrokeColor('#FFF')
            .draw();
    }

    drawTemperatureLine(){
        if (!this.showingElement.TEMPERATURE) {
            return;
        }
        const baseAxis = this.rightVerticalAxis[0];
        let offsetX = 5;
        const temperatureLine = new CurveLine(this.container);
        const color = '#62C1DD';
        const circle = new Circle(this.container);
        // circle.setRadius(2).setShapeStrokeColor(color).setShapeLineWidth(5).draw();
        temperatureLine.setShapeStrokeColor(color).setShapeLineWidth(4);
        let tempData = this.data.map(element => element[3]);
        let listCirclePoint = [];
        let lastPositionX = 0;
        for(let i=0; i < this.data.length; i++) {
            let temperatureData = this.data[i][3];
            if(temperatureData === 0){
                temperatureData = baseAxis;
            }
            let positionX = this.baseX + this.offsetLength * (i + 0.5);
            let positionY = this.baseY + (temperatureData - baseAxis) * this.rightUnitHeight;
            if(i === 0) {
                temperatureLine.setPosition(positionX, positionY)
            }else {
                temperatureLine.setNextPoint(positionX, positionY)
            }

            listCirclePoint.push(new Point(positionX, positionY));



            // add new point to list element graph
            const elementGraph = new GraphElement(this.container);
            elementGraph.setBottomLeftPosition(positionX, positionY)
                .setTopRightPosition(positionX, positionY)
                .setData({position: i, shot: this.data[i][3]}).setOrder(i);
            this.listCircleElementPoint.push(elementGraph);

            // last element

        }

        // fill color according temperature line
        const curveArea = new ChartCurveArea(this.container);
        let listPointArea = Object.assign([], temperatureLine.getListPoint());
        // curveArea.setListPoint(listPointArea).draw();
        temperatureLine.draw();

        listCirclePoint.forEach(point => {
            circle.setPosition(point.x, point.y).setRadius(2).setShapeLineWidth(3).setShapeStrokeColor(color);
            circle.draw();
        })
    }
    drawTemperatureBar() {
        if (!this.showingElement.TEMPERATURE) {
            return;
        }
        const rectangle = new Rectangle(this.container);
        const canvas = document.getElementById(this.containerId);
        // let height = canvas.offsetHeight
        // let canvasActualHeight = height - this.baseY;
        let canvasActualHeight = this.container.getHeight() - this.baseY;
        let maxValue = this.rightVerticalAxis[this.rightVerticalAxis.length - 1];
        let offsetFromGridLine = 4;
        let barSize = this.offsetLength - offsetFromGridLine*2;
        this.data.forEach((item, index) => {
            let val = item[3];
            console.log(canvasActualHeight, val, maxValue, '1');
            console.log(Math.round( canvasActualHeight * val/maxValue), '2');
            let barHeight = Math.round( canvasActualHeight * val/maxValue);
            let positionX = this.baseX + offsetFromGridLine + this.offsetLength * index;
            let positionY = this.baseY;
            if (barHeight > 0) {
                const elementGraph = new GraphElement(this.container);
                elementGraph.setBottomLeftPosition(positionX, positionY)
                    .setTopRightPosition(positionX + barSize, positionY + barHeight)
                    .setData({position: index, shot: item[3]}).setOrder(index);
                rectangle.setPosition(positionX, positionY).setDimension(barSize, barHeight - 15)
                    .setShapeFillColor('#BBDFEC').setShapeStrokeColor('#7BC0DB').setLineWidth(3).draw();
                this.listCircleElementPoint2.push(elementGraph);
            }
        });
    };

    getGraphColor(bottomTemp, MiddleTemp, topTemp){
        return '#3c763d';
    }

    draw() {
        if (this.showingElement.GRAPH) {
            this.drawMainGraph();
        }
        this.drawVerticalAxis();
        this.drawHorizontalAxis();
        this.drawGridDivider();
    }

    drawMainGraph() {
        // calculate difference per unit of Y Axis
        const baseAxis = this.verticalAxis[0];
        // draw element
        const bottomLine = new Line(this.container);
        const circle = new Circle(this.container);
        const topLine = new Line(this.container);
        const bottomToMiddleLine = new Line(this.container);
        const middleToTopLine = new Line(this.container);
        const shotText = new ChartText(this.container);
        for (let i = 0; i < this.data.length; i++) {
            let element = this.data[i];
            let offsetX = 5;
            let bottomTemp = element[0];
            let MiddleTemp = element[1];
            let topTemp = element[2];
            let totalShot = element[3];
            let positionX = this.baseX + this.offsetLength * (i + 0.5) + offsetX - 4;
            let positionYBottom = this.baseY + (bottomTemp - baseAxis) * this.unitHeight;
            let positionYMiddle = this.baseY + (MiddleTemp - baseAxis) * this.unitHeight;
            let positionYTop = this.baseY + (topTemp - baseAxis) * this.unitHeight;
            let radiusOfMiddle = 6;
            const limitedLineLength = 16;
            if (MiddleTemp === 0) {
                continue;
            }

            let graphColor = this.getGraphColor(bottomTemp, MiddleTemp, topTemp);
            // draw bottom line
            bottomLine.setPosition(positionX - limitedLineLength / 2, positionYBottom)
                .setEndPosition(positionX + limitedLineLength / 2, positionYBottom)
                .setShapeStrokeColor(graphColor)
                .setShapeLineWidth(4)
                .draw();

            circle.setPosition(positionX, positionYMiddle)
                .setRadius(radiusOfMiddle)
                .setShapeStrokeColor(graphColor)
                .setShapeLineWidth(4)
                .draw();

            topLine.setPosition(positionX - limitedLineLength / 2, positionYTop)
                .setEndPosition(positionX + limitedLineLength / 2, positionYTop)
                .setShapeStrokeColor(graphColor)
                .setShapeLineWidth(4)
                .draw();

            // join bottom to middle
            bottomToMiddleLine.setPosition(positionX, positionYBottom)
                .setEndPosition(positionX, positionYMiddle - radiusOfMiddle)
                .setShapeStrokeColor(graphColor)
                .setShapeLineWidth(4)
                .draw();
            // join middle to top
            middleToTopLine.setPosition(positionX, positionYMiddle + radiusOfMiddle)
                .setEndPosition(positionX, positionYTop)
                .setShapeStrokeColor(graphColor)
                .setShapeLineWidth(4)
                .draw();

            // draw shot text
            let offsetTextPositionX = 5;
            let offsetTextPositionY = 5;
            if (totalShot > 10) {
                offsetTextPositionX = 10
            }

            shotText.setPosition(positionX - offsetTextPositionX, positionYTop + offsetTextPositionY)
                .setText(MiddleTemp)
                .setFontSize(13)
                .draw();

            // add new point to list element graph
            const elementGraph = new GraphElement(this.container);
            let dataPositionYBottom = positionYBottom;
            let dataPositionYTop = positionYTop;
            if (positionYMiddle + radiusOfMiddle > positionYTop) {
                dataPositionYTop = positionYMiddle + radiusOfMiddle;
            }
            if (positionYMiddle - radiusOfMiddle < positionYBottom) {
                dataPositionYBottom = positionYMiddle - radiusOfMiddle;
            }
            elementGraph.setBottomLeftPosition(positionX - limitedLineLength / 2, dataPositionYBottom)
                .setTopRightPosition(positionX + limitedLineLength / 2, dataPositionYTop)
                .setData({bottomTemp, middleTemp: MiddleTemp, topTemp, shot: totalShot}).setOrder(i);
            this.listElementGraph.push(elementGraph);
        }
    }

    flush() {
        this.container.reset();
    }

    setEventListener(){
        this.container.setHoverListener((x, y) => {
            if(this.isSafari()) {
                y = y + this.getSafariOffsetY();
            }
            const point = new Point(this.container.getPositionX(x), this.container.getPositionY(y));
            this.listCircleElementPoint2.forEach(element => {
                if(element.contain(point)) {
                    let elementPosition = element.getBottomLeftPosition();
                    let circlePoint = this.container.getCirclePoint();
                    circlePoint.style.left = (elementPosition.x/this.container.getScale() + 7) + 'px';
                    // circlePoint.style.top = (elementPosition.y /this.container.getScale() - 5) + 'px';
                    circlePoint.style.top = y + 'px';
                    let shotCountPoint = this.container.getShotCountHint();
                    shotCountPoint.innerHTML = 'Shot: ' + element.getData().shot;
                    let shotHintHeader = this.container.getShotHintHeader();
                    shotHintHeader.innerHTML = element.getData().position;

                    circlePoint.style.display  = 'block';
                    this.currentCircleElement = element;
                }
            })

            this.listElementGraph.forEach(element => {
                if(element.contain(point)){
                    const data = this.getHintInfo(element);
                    let hintElement = this.container.getHintElement();
                    hintElement.style.left = x + 5 + 'px';
                    hintElement.style.top = y + 5 + 'px';
                    hintElement.style.display  = 'block';
                    let hintTextArr = [`<div style="font-weight: bold">${element.getOrder()}</div>`];
                    Object.keys(data).forEach(key => {
                        hintTextArr.push(`<div style="padding-left: 10px">${key}: ${data[key]}</div>`);
                    });
                    let hintText = hintTextArr.join(' ');
                    hintElement.innerHTML = hintText;
                    let circlePoint = this.container.getCirclePoint();
                    circlePoint.style.display  = 'none';
                }
            });

            // capture hover event in circle element point
            this.listCircleElementPoint.forEach(element => {
                if(element.contain(point)) {
                    let elementPosition = element.getBottomLeftPosition();
                    let circlePoint = this.container.getCirclePoint();
                    circlePoint.style.left = (elementPosition.x/this.container.getScale() - 5) + 'px';
                    circlePoint.style.top = (this.container.getHeight() - elementPosition.y/this.container.getScale() - 5) + 'px';
                    let shotCountPoint = this.container.getShotCountHint();
                    shotCountPoint.innerHTML = 'Shot: ' + element.getData().shot;
                    let shotHintHeader = this.container.getShotHintHeader();
                    shotHintHeader.innerHTML = element.getData().position;

                    circlePoint.style.display  = 'block';
                    this.currentCircleElement = element;
                }
            })
        });

        this.container.getCirclePoint().onclick = () => {
            if (this.graphType === 'TEMPERATURE') {
                this.showDetail(this.currentCircleElement)
            }
        };

        this.container.setClickListener((x,y) => {
            if(this.isSafari()) {
                y = y + this.getSafariOffsetY();
            }
            const point = new Point(this.container.getPositionX(x), this.container.getPositionY(y));
            this.listElementGraph.forEach(element => {
                if(element.contain(point)){
                    if (this.graphType === 'CYCLE_TIME' || this.graphType === 'TEMPERATURE'){
                        this.showDetail(element);
                    }
                }
            });
            this.captureClickLegendEvent(point);
        })
    }

    captureClickLegendEvent(point) {
        Object.keys(this.legendPositions).forEach(legendType => {
            let shape = this.legendPositions[legendType];
            if (shape.isContainPoint(point)) {
                this.showingElement[legendType] = !this.showingElement[legendType];
                let rectangle = new Rectangle(this.container);
                rectangle.setPosition(0,0).setDimension(this.container.getWidth(), this.container.getHeight()).setShapeFillColor('#FFF').draw();
                this.draw();
            }
        });
    }

    showDetail(graphElement) {
        let order = graphElement.getOrder();
        if (order < 10){
            order = '0' + order;
        }
        if(this.parent && typeof this.parent.details === "function"){
            console.log('hourly-chart showDetail')
            this.parent.details(order);
        }
        let child = Common.vue.getChild(vm.$children, "chart-mold");
        if (child != null &&  typeof child.details === "function" ) {
            child.details(order);
        }
    }

    getHintInfo(){}
}
