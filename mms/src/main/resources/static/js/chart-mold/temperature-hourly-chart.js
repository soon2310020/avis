class TemperatureHourlyChart extends HourlyChart {
    constructor(containerId, data, isShotLine, parentScope) {
        super(containerId, data, 'TEMPERATURE', parentScope);
        this.isShotLine = isShotLine;
        this.verticalAxisName = 'Temperature (°C)';

    }

    // get cycle axis depend on current data
    setVerticalAxis() {
        let smallestValueFirst = this.getSmallestData(0);
        let smallestValueSecond = this.getSmallestData(1);
        let smallestValueThird = this.getSmallestData(2);
        let smallestValue = smallestValueFirst;
        if (smallestValue > smallestValueSecond) {
            smallestValue = smallestValueSecond
        }

        if (smallestValue > smallestValueThird) {
            smallestValue = smallestValueThird
        }

        let smallestValueGreaterThanZeroFirst = this.getSmallestDataGreaterThanZero(0);
        let smallestValueGreaterThanZeroSecond = this.getSmallestDataGreaterThanZero(1);
        let smallestValueGreaterThanZeroThird = this.getSmallestDataGreaterThanZero(2);
        let smallestValueGreaterThanZero = smallestValueGreaterThanZeroFirst;
        if (smallestValueGreaterThanZero > smallestValueGreaterThanZeroSecond) {
            smallestValueGreaterThanZero = smallestValueGreaterThanZeroSecond
        }

        if (smallestValueGreaterThanZero > smallestValueGreaterThanZeroThird) {
            smallestValueGreaterThanZero = smallestValueGreaterThanZeroThird
        }

        let biggestValueFirst = this.getBiggestData(0);
        let biggestValueSecond = this.getBiggestData(1);
        let biggestValueThird = this.getBiggestData(2);
        let biggestValue = biggestValueFirst;
        if (biggestValue < biggestValueSecond) {
            biggestValue =  biggestValueSecond;
        }
        if (biggestValue < biggestValueThird) {
            biggestValue =  biggestValueThird;
        }

        // set default when not exist
        if (!biggestValue && !smallestValue) {
            biggestValue = 10;
            smallestValue = 0;
        }
        let difference = biggestValue - smallestValue;
        if (difference === 0) {
            difference = 0.1;
        }
        let standardTopValue = biggestValue + smallestValue / 2;
        if (smallestValue === 0) {
            standardTopValue = biggestValue + smallestValueGreaterThanZero / 4;
        }

        let unit = Number(this.getHeightUnit(standardTopValue).toFixed(1));

        // divide pivot with 10 unit
        // let lowIndex = Math.floor(smallestValue / unit);
        let highIndex = Math.floor(standardTopValue / unit);
        for(let i = 0; i <= highIndex; i++ ) {
            this.verticalAxis.push(Number(i*unit).toFixed(1).replace('.0', ''));
        }
        this.unitHeight = (this.container.getHeight() - this.baseY - this.topOffset)/((highIndex)*unit);

        // reset baseX depend biggest value
        this.container.setBaseX(((parseInt(standardTopValue) + '').length - 1) * 5 + 35);
        this.baseX = this.container.getBaseX();
    }

    getHintInfo(graphElement){
        const elementData = graphElement.getData();
        return {
            THI: elementData.topTemp + '°C',
            TAV: elementData.middleTemp + '°C',
            TLO: elementData.bottomTemp + '°C'
        };
    }

    draw() {
        this.flush();
        if (this.isShotLine) {
            this.drawTemperatureLine();
        } else {
            this.drawTemperatureBar();
        }
        super.draw();
        this.drawRightVertical();
        this.drawAreaName();
    }

    drawAreaName(){
        // draw graph cycle time
        this.drawAreaTemperatureGraph();

        // draw within between
        this.drawAreaTemperatureLine();
    }

    drawAreaTemperatureGraph(){
        const bottomLine = new Line(this.container);
        const circle = new Circle(this.container);
        const topLine = new Line(this.container);
        const bottomToMiddleLine = new Line(this.container);
        const middleToTopLine = new Line(this.container);
        const basePositionY = this.baseY - 75;
        let bottomTemp = basePositionY - 15;
        let MiddleTemp = basePositionY;
        let topTemp = basePositionY + 15;
        let positionX = (this.container.getWidth() - this.baseX)/2 - 110;
        let positionYBottom = bottomTemp;
        let positionYMiddle = MiddleTemp;
        let positionYTop = topTemp;
        let radiusOfMiddle = 6;
        const limitedLineLength = 10;


        // draw bottom line
        bottomLine.setPosition(positionX - limitedLineLength / 2, positionYBottom)
            .setEndPosition(positionX + limitedLineLength / 2, positionYBottom)
            .setShapeStrokeColor(this.graphColor)
            .setShapeLineWidth(4)
            .draw();

        circle.setPosition(positionX, positionYMiddle)
            .setRadius(radiusOfMiddle)
            .setShapeStrokeColor(this.graphColor)
            .setShapeLineWidth(4)
            .draw();

        topLine.setPosition(positionX - limitedLineLength / 2, positionYTop)
            .setEndPosition(positionX + limitedLineLength / 2, positionYTop)
            .setShapeStrokeColor(this.graphColor)
            .setShapeLineWidth(4)
            .draw();

        // join bottom to middle
        bottomToMiddleLine.setPosition(positionX, positionYBottom)
            .setEndPosition(positionX, positionYMiddle - radiusOfMiddle)
            .setShapeStrokeColor(this.graphColor)
            .setShapeLineWidth(4)
            .draw();
        // join middle to top
        middleToTopLine.setPosition(positionX, positionYMiddle + radiusOfMiddle)
            .setEndPosition(positionX, positionYTop)
            .setShapeStrokeColor(this.graphColor)
            .setShapeLineWidth(4)
            .draw();

        // set legend position
        this.setLegendPosition(this.legendTypes.GRAPH, positionX - limitedLineLength / 2 - 5, positionYBottom, limitedLineLength + 105, topTemp - bottomTemp + 10);

        // draw text hint
        let text = new ChartText(this.container);
        let textPositionX = positionX + radiusOfMiddle*2;
        let textPositionY = basePositionY - radiusOfMiddle + 1;
        text.setPosition(textPositionX, textPositionY).setFontSize(13).setShapeStrokeColor(this.legendTextColor).setText('Temperature (sec)').draw();
        if (!this.showingElement.GRAPH) {
            this.drawStraightLine(textPositionX, textPositionY + 4, 105);
        }
    }

    drawAreaTemperatureLine(){
        let positionX = (this.container.getWidth() - this.baseX)/2 + 60;
        let positionY = this.baseY - 84;
        let legendIconWidth = 35;
        let legendTextWidth = 25;
        let distanceWithLegendIcon = 5;

        let rectArea = new ChartHintRectangle(this.container);
        if (this.isShotLine) {
            rectArea.setPosition(positionX, positionY + 9);
            rectArea.setDimension(legendIconWidth, 1).setShapeFillColor('#F0F8FB').setShapeStrokeColor('#62C1DD').setShapeLineWidth(3).draw();
        } else {
            rectArea.setPosition(positionX, positionY + 4);
            rectArea.setDimension(legendIconWidth, 12).setShapeFillColor('#B1E0EE').setShapeStrokeColor('#63C2DE').setShapeLineWidth(3).draw();
        }

        // set legend position
        this.setLegendPosition(this.legendTypes.TEMPERATURE, positionX, positionY + 6 - 5, legendIconWidth + legendTextWidth + distanceWithLegendIcon, 6 + 10);
        let text = new ChartText(this.container);
        let textPositionX = positionX + legendIconWidth + distanceWithLegendIcon;
        let textPositionY = positionY + 5;
        text.setPosition(textPositionX, textPositionY).setShapeStrokeColor(this.legendTextColor).setText('Shot').setFontSize(13).draw();
        if (!this.showingElement.TEMPERATURE) {
            this.drawStraightLine(textPositionX, textPositionY + 4, legendTextWidth);
        }
    }
}
