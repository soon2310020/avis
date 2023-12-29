class Shape {
  constructor(container) {
    this.container = container;
    this.context = container.getContext();
    this.strokeColor = "#000";
    this.fillColor = "#FFF";
    this.lineWidth = 1;
  }

  setPosition(x, y) {
    this.x = x;
    this.y = y;
    return this;
  }

  setStrokeColor(color) {
    this.strokeColor = color;
    return this;
  }

  setFillColor(color) {
    this.fillColor = color;
    return this;
  }

  setLineWidth(lineWidth) {
    this.lineWidth = lineWidth;
    return this;
  }

  draw() {
  }
}