class CycleTimeHourlyChart extends HourlyChart {
    constructor(containerId, cycleZoneData, data, totalInfo, colorZone, isShotLine, parentScope) {
        super(containerId, data, 'CYCLE_TIME', parentScope);
        this.isShotLine = isShotLine;
        this.cycleZoneData = cycleZoneData;
        this.totalInfo = totalInfo;
        this.setVerticalAxis();
        this.colorZone = colorZone;
        this.verticalAxisName = 'Cycle Time (sec)';
        this.legendTypes.L1 = 'L1';
        this.legendTypes.L2 = 'L2';
        this.showingElement.L1 = true;
        this.showingElement.L2 = true;
    }

    setVerticalAxis() {
        if (!this.cycleZoneData) {
            return;
        }
        this.verticalAxis = [0];
        let topPosition = this.cycleZoneData[this.cycleZoneData.length - 1];
        let biggestValue = Number.MIN_SAFE_INTEGER;
        this.data.map(element => {
            if (biggestValue < element[0]) {
                biggestValue = element[0];
            }

            if (biggestValue < element[1]) {
                biggestValue = element[1];
            }

            if (biggestValue < element[2]) {
                biggestValue = element[2];
            }
        });

        if (topPosition < biggestValue) {
            topPosition = biggestValue;
        }
        if (!topPosition) {
            topPosition = 10;
        }
        let unit = Number(this.getHeightUnit(topPosition).toFixed(1));
        let maxIndex = Math.ceil(topPosition / unit);
        for (let i = 1; i <= maxIndex + 1; i++) {
            this.verticalAxis.push(Number(i*unit).toFixed(1).replace('.0', ''));
        }
        this.unitHeight = (this.container.getHeight() - this.baseY - this.topOffset) / (maxIndex * unit);

        // reset baseX depend biggest value
        // reset baseX depend biggest value
        console.log('biggestValue', biggestValue);
        let biggestStr = parseInt(biggestValue) + '';
        if (biggestValue < this.totalInfo.totalShotL2Top) {
            biggestStr = parseInt(this.totalInfo.totalShotL2Top) + '';
        }
        this.container.setBaseX(((biggestStr.length - 1) * 5 + 40));
        this.baseX = this.container.getBaseX();
    }

    getUpdatedZoneData() {
        let cycleZoneData = Object.assign([], this.cycleZoneData);
        cycleZoneData.unshift(0);
        cycleZoneData.push(this.verticalAxis[this.verticalAxis.length - 1]);
        return cycleZoneData;
    }

    getGraphColor(bottomTemp, MiddleTemp, topTemp){
        if (this.totalInfo.totalShotL2Bottom > MiddleTemp || this.totalInfo.totalShotL2Top < MiddleTemp) {
            return 'red';
        }
        if(this.totalInfo.totalShotL1Bottom > MiddleTemp || this.totalInfo.totalShotL1Top < MiddleTemp) {
            // return '#FFFF99';
            return '#FFDB1E';
        }

        if (this.totalInfo.totalShotL2Bottom > bottomTemp || this.totalInfo.totalShotL2Top < topTemp) {
            return 'orange';
        }

        return '#3c763d';
    }

    draw() {
        this.flush();
        const colorZoneArr = Object.values(this.colorZone);
        const line = new Line(this.container);

        // draw line zone
        let cycleZoneData = this.getUpdatedZoneData();

        for (let i = 1; i < cycleZoneData.length - 1; i++) {
            let positionY = this.baseY + this.unitHeight * (cycleZoneData[i] - cycleZoneData[0]);
            if ((this.showingElement.L1 && (i === 2 || i === 3)) || (this.showingElement.L2 && (i === 1 || i === 4))) {
                // draw zone color
                line.setPosition(this.baseX, positionY)
                    .setEndPosition(this.container.getWidth() - this.rightOffset, positionY)
                    .setShapeStrokeColor(colorZoneArr[i - 1])
                    .setShapeLineWidth(2)
                    .draw();
            }

        }
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
        console.log('container width', this.container.getWidth());
        let unitWidth =  140;
        let offsetX = this.container.getWidth() / (this.container.getScale() * 2) - 100;
        // draw graph cycle time
        this.drawAreaGraphCycleTime(offsetX, unitWidth, 1);

        // draw within between
        // this.drawAreaWithinBetween(offsetX, unitWidth, 2);
        // draw L1
        this.drawAreaL1(offsetX, unitWidth, 2);
        //draw L2
        this.drawAreaL2(offsetX, unitWidth, 3);

        // draw within between
        this.drawAreaTemperatureLine(offsetX, unitWidth, 4);
    }

    drawAreaGraphCycleTime(offsetX, unitWidth, index){
        const bottomLine = new Line(this.container);
        const circle = new Circle(this.container);
        const topLine = new Line(this.container);
        const bottomToMiddleLine = new Line(this.container);
        const middleToTopLine = new Line(this.container);
        const basePositionY = this.baseY - 80;
        let bottomTemp = basePositionY - 15;
        let MiddleTemp = basePositionY;
        let topTemp = basePositionY + 15;
        let positionX = this.baseX + 10 + (index - 1) * unitWidth + offsetX;
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
        this.setLegendPosition(this.legendTypes.GRAPH, positionX - limitedLineLength / 2 - 5, positionYBottom, limitedLineLength + 95, topTemp - bottomTemp + 10);
        // draw text hint
        let text = new ChartText(this.container);
        text.setPosition(positionX + radiusOfMiddle*2, basePositionY - radiusOfMiddle + 1).setFontSize(13).setShapeStrokeColor(this.legendTextColor).setText('Cycle Time (sec)').draw();
        if (!this.showingElement.GRAPH) {
            this.drawStraightLine(positionX + radiusOfMiddle*2, basePositionY, 95);
        }
    }

    drawAreaWithinBetween(offsetX, unitWidth, index){
        // this.drawAreaRectangle(offsetX, unitWidth, index, '#FFF', '#aebb76', 'Within Tolerance (sec)');
    }
//Todo
    drawAreaL1(offsetX, unitWidth, index){
        // this.drawAreaRectangle(offsetX, unitWidth + 37, index, '#FFF', '#FEC104', 'L1 (sec)', this.legendTypes.L1);
        this.drawAreaRectangle(offsetX, unitWidth + 37, index, '#FFF',  getStyle("--warning"), 'L1 (sec)', this.legendTypes.L1);
    }

    drawAreaL2(offsetX, unitWidth, index) {
        this.drawAreaRectangle(offsetX, unitWidth + 25, index, '#FFF', '#F76B6B', 'L2 (sec)', this.legendTypes.L2);
    }

    drawAreaRectangle(offsetX, unitWidth, index, fillColor, strokeColor, name, legendType){
        let positionX = this.baseX + (index - 1) * unitWidth + offsetX;
        let positionY = this.baseY - 89;
        let legendIconWidth = 35;
        let legendTextWidth = 45;
        let distanceWithLegendIcon = 5;

        let rectArea = new ChartHintRectangle(this.container);
        rectArea.setPosition(positionX, positionY + 9);
        rectArea.setDimension(legendIconWidth, 1).setShapeFillColor(fillColor).setShapeStrokeColor(strokeColor).setShapeLineWidth(3).draw();

        // set legend position
        this.setLegendPosition(legendType, positionX, positionY + 6 - 5, legendIconWidth + legendTextWidth + distanceWithLegendIcon, 6 + 10);

        let text = new ChartText(this.container);
        let textPositionX = positionX + legendIconWidth + distanceWithLegendIcon;
        let textPositionY = positionY + 5;
        text.setPosition(textPositionX, textPositionY).setText(name).setShapeStrokeColor(this.legendTextColor).setFontSize(13).draw();
        if ((legendType === this.legendTypes.L1 && !this.showingElement.L1) || (legendType === this.legendTypes.L2 && !this.showingElement.L2)) {
            this.drawStraightLine(textPositionX, textPositionY + 5, legendTextWidth);
        }

    }

    drawAreaTemperatureLine(offsetX, unitWidth, index){
        let positionX = (this.container.getWidth() - this.baseX)/2 + 240;
        let positionY = this.baseY - 89;
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
        let textPositionX = positionX + legendIconWidth +  distanceWithLegendIcon;
        let textPositionY = positionY + 5;
        text.setPosition(textPositionX, textPositionY).setText('Shot').setShapeStrokeColor(this.legendTextColor).setFontSize(13).draw();
        if (!this.showingElement.TEMPERATURE) {
            this.drawStraightLine(textPositionX, textPositionY + 5, legendTextWidth);
        }
    }

    getHintInfo(graphElement){
        const elementData = graphElement.getData();
        return {
            ULCT: elementData.topTemp + 's',
            MFCT: elementData.middleTemp + 's',
            LLCT: elementData.bottomTemp + 's'
        };
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
}
