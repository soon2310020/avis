{
    /* <script> */
}
const cssStyle = getComputedStyle(document.documentElement);
// .trim() is need for chrome

// chart graph
const __greenGraphLight = cssStyle.getPropertyValue('--green-graph-light-1').trim();
const __greenGraphLightHover = cssStyle.getPropertyValue('--green-graph-light-2').trim();

const __blueGraphMedium = cssStyle.getPropertyValue('--blue-graph-medium-1').trim();
const __blueGraphMediumHover = cssStyle.getPropertyValue('--blue-graph-medium-2').trim();
const __blueGraphDark = cssStyle.getPropertyValue('--blue-graph-dark-1').trim();
const __blueGraphDarkHover = cssStyle.getPropertyValue('--blue-graph-dark-2').trim();

const __pinkGraphMedium = cssStyle.getPropertyValue('--pink-graph-medium-1').trim();
const __pinkGraphMediumHover = cssStyle.getPropertyValue('--pink-graph-medium-2').trim();

// traffic light
const __purpleGraphLight = cssStyle.getPropertyValue('--purple-graph-light-1').trim();
const __purpleGraphLightHover = cssStyle.getPropertyValue('--purple-graph-light-2').trim();
const __purpleGraphMedium = cssStyle.getPropertyValue('--purple-graph-medium-1').trim();
const __purpleGraphMediumHover = cssStyle.getPropertyValue('--purple-graph-medium-2').trim();
const __purpleGraphDark = cssStyle.getPropertyValue('--purple-graph-dark-1').trim();
const __purpleGraphDarkHover = cssStyle.getPropertyValue('--purple-graph-dark-1').trim();

// traffic light
const __trafficLightYellow = cssStyle.getPropertyValue('--traffic-light-yellow-1').trim();
const __trafficLightYellowHover = cssStyle.getPropertyValue('--traffic-light-yellow-2').trim();
const __trafficLightRed = cssStyle.getPropertyValue('--traffic-light-red-1').trim();
const __trafficLightRedHover = cssStyle.getPropertyValue('--traffic-light-red-2').trim();
// </script>
