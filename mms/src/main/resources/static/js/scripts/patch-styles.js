function getElementFromClassName(className) {
  const elements = document.getElementsByClassName(className);
  if (elements.length === 0) {
    throw new Error(`CSS patch failed: .${className} not found`);
  }
  return elements[0];
}

function getElementHeight(element) {
  const style = window.getComputedStyle(element);
  return (
    element.offsetHeight +
    parseInt(style.marginTop) +
    parseInt(style.marginBottom)
  );
}

function patchStyles() {
  const appBodyElement = getElementFromClassName("app-body");
  const appHeaderElement = getElementFromClassName("app-header navbar");
  appBodyElement.style.marginTop = `${getElementHeight(appHeaderElement)}px`;

  const mainElement = document.getElementById("main");
  if (!mainElement) throw new Error("CSS patch failed: #main not found");

  const containerElement = getElementFromClassName("container-fluid");

  containerElement.style.width = "100%";
  containerElement.style.height = "100%";
  containerElement.style.margin = 0;
  containerElement.style.padding = 0;
}

patchStyles();
