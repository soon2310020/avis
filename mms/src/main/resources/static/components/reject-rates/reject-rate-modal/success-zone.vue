<template>
  <div class="success-zone">
    <div class="success-zone__container">
      <div class="success-zone__icon">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="34"
          height="34"
          viewBox="0 0 34 34"
        >
          <g id="check" transform="translate(-0.083)">
            <g id="Circle" transform="translate(0.083)">
              <path
                id="Path_1077"
                data-name="Path 1077"
                d="M17,0A17,17,0,1,1,0,17,17,17,0,0,1,17,0Z"
                fill="#3491ff"
              />
              <path
                id="Path_1074"
                data-name="Path 1074"
                d="M255.712,0h-.027V34h.027c11.042,0,19.994-7.611,19.994-17S266.754,0,255.712,0Z"
                transform="translate(-241.706)"
                fill="#3491ff"
              />
            </g>
            <g id="Check-2" data-name="Check" transform="translate(5.509 8.25)">
              <path
                id="Path_1075"
                data-name="Path 1075"
                d="M108.2,153.16l8.087,8.087a.9.9,0,0,0,1.277,0l13.276-13.276a.9.9,0,0,0,0-1.277l-2.416-2.416a.9.9,0,0,0-1.277,0l-9.656,9.656a.9.9,0,0,1-1.277,0l-4.327-4.327a.9.9,0,0,0-1.259-.018l-2.408,2.277A.9.9,0,0,0,108.2,153.16Z"
                transform="translate(-107.939 -144.013)"
                fill="#fff"
              />
              <path
                id="Path_1076"
                data-name="Path 1076"
                d="M264.632,144.276a.9.9,0,0,0-1.277,0l-7.67,7.67v7.388l11.364-11.364a.9.9,0,0,0,0-1.277Z"
                transform="translate(-244.144 -144.012)"
                fill="#fff"
              />
            </g>
          </g>
        </svg>
      </div>
      <div class="success-zone__title">
        <span>Success</span>
      </div>
      <div class="success-zone__content">
        <span>The reject rate has been input successfully.</span>
      </div>
      <div class="success-zone__footer">
        <a
          id="success-zone-success-btn"
          href="javascript:void(0)"
          @click.stop="onCancel"
          class="success-zone__footer__btn"
          >Okay</a
        >
      </div>
    </div>
  </div>
</template>

<script>
// success zone
module.exports = {
  name: "SuccessZone",
  props: {
    onCancel: {
      type: Function,
      default: () => {}
    }
  },
};
</script>
