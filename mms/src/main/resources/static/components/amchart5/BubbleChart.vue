<template>
  <div class="hello" ref="bubblechartdiv"></div>
</template>

<script>
module.exports = {
  props: {
    chartData: Object,
    xAxisName: String,
    yAxisName: String,
    moreSeriesDataName: String,
  },
  data() {
    return {
      root: "",
      chart: "",
      xAxis: "",
      yAxis: "",
      legend: "",
      seriesList: "",
    };
  },
  mounted() {
    this.root = am5.Root.new(this.$refs.bubblechartdiv);
    this.root.setThemes([am5themes_Animated.new(this.root)]);

    this.chart = this.root.container.children.push(
      am5xy.XYChart.new(this.root, {
        panX: true,
        panY: true,
        wheelY: "zoomXY",
        layout: this.root.verticalLayout,
      })
    );

    this.xAxis = this.chart.xAxes.push(
      am5xy.ValueAxis.new(this.root, {
        tooltip: am5.Tooltip.new(this.root, {}),
        renderer: am5xy.AxisRendererX.new(this.root, {}),
      })
    );
    this.xAxis.children.push(
      am5.Label.new(this.root, {
        text: this.xAxisName,
        x: am5.p50,
        centerX: am5.p50,
      })
    );

    this.yAxis = this.chart.yAxes.push(
      am5xy.ValueAxis.new(this.root, {
        tooltip: am5.Tooltip.new(this.root, {}),
        renderer: am5xy.AxisRendererY.new(this.root, {}),
      })
    );
    this.yAxis.children.unshift(
      am5.Label.new(this.root, {
        rotation: -90,
        text: this.yAxisName,
        y: am5.p50,
        centerX: am5.p50,
      })
    );

    this.legend = this.chart.children.push(
      am5.Legend.new(this.root, {
        x: am5.percent(50),
        centerX: am5.percent(50),
      })
    );
    if (this.chartData.shots) {
      this.chartData.shots.map((item) => {
        let seriesData = item.points.map((pointItem) => {
          return {
            title: item.title,
            t: pointItem.t,
            x: pointItem.x,
            y: pointItem.y,
          };
        });
        this.createSeries(item.title, seriesData, this.root);
      });
    }

    this.chart.set(
      "scrollbarX",
      am5.Scrollbar.new(this.root, { orientation: "horizontal", height: 10 })
    );

    this.chart.set(
      "scrollbarY",
      am5.Scrollbar.new(this.root, { orientation: "vertical", width: 10 })
    );

    let scrollbarX = this.chart.get("scrollbarX");
    scrollbarX.thumb.setAll({
      fillOpacity: 0.5,
    });
    scrollbarX.startGrip.setAll({
      scale: 0.7,
    });
    scrollbarX.endGrip.setAll({
      scale: 0.7,
    });

    let scrollbarY = this.chart.get("scrollbarY");
    scrollbarY.thumb.setAll({
      fillOpacity: 0.5,
    });
    scrollbarY.startGrip.setAll({
      scale: 0.7,
    });
    scrollbarY.endGrip.setAll({
      scale: 0.7,
    });

    this.chart.set("cursor", am5xy.XYCursor.new(this.root, {}));

    let cursor = this.chart.get("cursor");

    cursor.setAll({
      xAxis: this.xAxis,
      yAxis: this.yAxis,
      // snapToSeries: this.seriesList,
    });

    // var previousBulletSprites = [];
    // cursor.events.on("cursormoved", cursorMoved);

    // let chart = this.chart;
    // function cursorMoved() {
    //   for (var i = 0; i < previousBulletSprites.length; i++) {
    //     previousBulletSprites[i].unhover();
    //   }
    //   previousBulletSprites = [];
    //   chart.series.each(function (series) {
    //     let dataItem = series.get("tooltip").dataItem;
    //     if (dataItem) {
    //       let bulletSprite = dataItem.bullets[0].get("sprite");
    //       bulletSprite.hover();
    //       previousBulletSprites.push(bulletSprite);
    //     }
    //   });
    // }

    this.chart.appear(1500);
  },
  methods: {
    createSeries(title, seriesData, root) {
      const self = this;
      let series;

      series = this.chart.series.push(
        am5xy.LineSeries.new(this.root, {
          name: title,
          xAxis: self.xAxis,
          yAxis: self.yAxis,
          valueYField: "y",
          valueXField: "x",
          setStateOnChildren: true,
          // 툴팁을 추가해야 하지만 원하지 않으면 숨길 수 있습니다.
          tooltip: am5.Tooltip.new(root, {
            // labelText: undefined,
            forceHidden: true,
            // animationDuration: 0,
          }),
        })
      );

      series.bullets.push(function () {
        let circle = am5.Circle.new(root, {
          radius: 5,
          fill: series.get("fill"),
          fillOpacity: 0.1,
          stroke: series.get("fill"),
          strokeWidth: 2,
          interactive: true,
          tooltipText: `[bold]{title}[/]
                ${self.moreSeriesDataName}: {t}
                ${self.yAxisName}: {valueY}
                ${self.xAxisName}: {valueX}`,
        });

        circle.states.create("hover", {
          scale: 3,
        });

        return am5.Bullet.new(root, {
          sprite: circle,
        });
      });
      series.strokes.template.set("visible", false);
      this.legend.data.setAll(this.chart.series.values);

      series.data.setAll(seriesData);
      series.appear(1500);

      if (this.seriesList) {
        this.seriesList = [...this.seriesList, series];
      } else {
        this.seriesList = [series];
      }
    },
  },
  beforeDestroy() {
    if (this.root) {
      this.root.dispose();
    }
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.hello {
  width: 100%;
  height: 500px;
}
</style>