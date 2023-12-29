<template>
  <div>
    <div v-show="isLoading" class="modal-body__container loading-wave"></div>
    <div v-show="!isLoading" class="modal-body__container">
      <div
        class="modal-body__carret carret-left"
        :class="{
          inactive: inactiveCarret == 'backward' || inactiveCarret == 'all',
        }"
        @click="changePage('-')"
        @mouseover="handleHoverCarret('backward')"
        @mouseleave="handleHoverCarret('')"
      >
        <img
          v-if="inactiveCarret == 'backward' || inactiveCarret == 'all'"
          src="/images/icon/category/backward-carret-inactive.svg"
          alt=""
        />
        <img
          v-else-if="hoverCarret != 'backward'"
          src="/images/icon/category/backward-carret.svg"
          alt=""
        />
        <img
          v-else-if="hoverCarret == 'backward'"
          src="/images/icon/category/backward-carret-hover.svg"
          alt=""
        />
      </div>
      <div
        class="modal-body__carret carret-right"
        :class="{
          inactive: inactiveCarret == 'forward' || inactiveCarret == 'all',
        }"
        @click="changePage('+')"
        @mouseover="handleHoverCarret('forward')"
        @mouseleave="handleHoverCarret('')"
      >
        <img
          v-if="inactiveCarret == 'forward' || inactiveCarret == 'all'"
          src="/images/icon/category/forward-carret-inactive.svg"
          alt=""
        />
        <img
          v-else-if="hoverCarret != 'forward'"
          src="/images/icon/category/forward-carret.svg"
          alt=""
        />
        <img
          v-else-if="hoverCarret == 'forward'"
          src="/images/icon/category/forward-carret-hover.svg"
          alt=""
        />
      </div>
      <div class="modal-body__content">
        <div class="carousel" role="list">
          <div class="flickity-viewport">
            <div
              id="flickity-slider"
              ref="flickity-slider"
              class="flickity-slider"
              style="left: 0px; transform: translateX(0%)"
            >
              <div
                v-for="(project, index) in brand.childProfiles"
                :key="index"
                class="carousel-cell block"
                role="listitem"
                style="position: absolute; left: 0px"
              >
                <project-card
                  :project="project"
                  :index="index"
                  :resources="resources"
                  @choose-project="handleChooseProject"
                  @change-route="handleChangeRoute"
                ></project-card>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
const SLIDER_WIDTH = 919;
module.exports = {
  name: "category-library-overview",
  components: {
    "project-card": httpVueLoader(
      "/components/category/category-library/project-card.vue"
    ),
  },
  props: {
    resources: Object,
    brand: {
      type: Object,
      default: () => ({
        id: null,
        name: "",
        childProfiles: [],
      }),
    },
    handleChooseProject: Function,
    isLoading: Boolean
  },
  data() {
    return {
      hoverCarret: "",
      currentPos: 0,
      maxPos: 0,
      inactiveCarret: "all",
      lastPercent: 0,
    };
  },
  computed: {
    getProjectSize() {
      if (this.brand.childProfiles) {
        return `${this.brand.childProfiles.length} ${
          this.brand.childProfiles.length > 1 ? "Products" : "Product"
        }`;
      }
      return "";
    },
  },
  methods: {
    initCarouselItem() {
      this.clearCarousel();
      console.log('initCarouselItem brand', this.brand)
      this.currentPos = 0
      this.lastPercent = 0
      if (!this.brand?.childProfiles?.length) return
      this.maxPos = this.brand.childProfiles.length;
      if (this.brand.childProfiles.length < 4) {
        this.inactiveCarret = "all";
      } else {
        this.inactiveCarret = "backward";
      }
      // const parent = document.getElementById("flickity-slider");
      const parent = this.$refs['flickity-slider']
      console.log('parent', parent)
      if (parent) {
        const elements = parent.childNodes;
        console.log('elements', elements)
        console.log('elements.length', elements.length)
        this.$nextTick(() => {
          for (let index = 0; index < elements.length; index++) {
            console.log('elements[index]', elements[index])
            const transformValue = `translateX(${103.58 * index}%)`;
            elements[index].style.transform = transformValue;
            console.log('transformValue', transformValue)
          }
        })
      }
    },
    clearCarousel(){
      const slider = this.$refs["flickity-slider"];
      if (slider) {
        slider.style.transform = `translateX(0%)`;
      }
    },
    changeSlider(sign) {
      const slider = this.$refs["flickity-slider"];
      if (slider) {
        const partern = /translateX\(([0-9\.\-]+)%\)/;
        const match = slider.style.transform.match(partern);
        let currentValue = match[1];
        let totalPercent =
          (this.maxPos * 29000 - SLIDER_WIDTH * 100) / SLIDER_WIDTH;
        if (this.currentPos == 1 && sign == "-") {
          slider.style.transform = `translateX(0%)`;
        } else if (this.currentPos == this.maxPos - 4 && sign == "+") {
          this.lastPercent = +currentValue + +totalPercent;
          slider.style.transform = `translateX(-${totalPercent}%)`;
        } else {
          let newValue = 0;
          let changeValue = 0;
          if (this.currentPos == 0 && sign == "+") {
            changeValue = 26000 / SLIDER_WIDTH;
          } else if (this.currentPos == this.maxPos - 3 && sign == "-") {
            changeValue = this.lastPercent;
            this.lastPercent = 0;
          } else {
            changeValue = 29000 / SLIDER_WIDTH;
          }
          if (sign == "+") {
            newValue = currentValue - changeValue;
          } else {
            newValue = +currentValue + +changeValue;
          }
          slider.style.transform = `translateX(${newValue}%)`;
        }
      }
    },
    checkPos() {
      if (this.currentPos == 0) {
        this.inactiveCarret = "backward";
      } else if (this.currentPos == this.maxPos - 3) {
        this.inactiveCarret = "forward";
      } else {
        this.inactiveCarret = "";
      }
    },
    changePage(sign) {
      this.changeSlider(sign);
      this.currentPos = eval(`${this.currentPos}${sign}1`);
      this.checkPos();
    },
    handleHoverCarret(type) {
      this.hoverCarret = type;
    },
    handleChangeRoute(route) {
      this.$emit("change-route", route);
    },
  },
  watch: {
    brand(newVal) {
      console.log('OVERVIEW>>>brand', newVal)
      this.initCarouselItem()
    }
  }
};
</script>

<style>
</style>