<template>
  <div>
    <div class="block__image">
      <img
        :src="getImage(project.projectImage).url"
        :class="{
          'exsisted-image': getImage(project.projectImage).status == 'EXIST',
        }"
        alt=""
      />
    </div>
    <div class="block__button">
      <a
        :ref="`view-button-${index}`"
        href="javascript:void(0)"
        class="cta-secondary"
        @click="ctaAnimation(index)"
        >View Product Profile</a
      >
    </div>
    <div class="block__container">
      <div class="block__content">
        <div class="block__content__title">Product Name</div>
        <div class="content__hyper-link d-flex" @click="chooseProject()">
          <a-tooltip v-if="shouldTruncate" placement="bottom">
            <template slot="title">
              <div style="padding: 6px 8px">
                <span>{{ project.name }}</span>
              </div>
            </template>
            <div class="truncate-card-title">{{ getTruncateName }}</div>
          </a-tooltip>
          <div v-else style="margin-right: 10px;">
            {{ project.name }}
          </div>
          <img src="/images/icon/category/expand-arrow.svg" alt="" />
        </div>
      </div>
      <div class="block__content">
        <div class="block__content__title">Number of Parts</div>
        <div
          class="content__hyper-link"
          @click="
            handleChangeRoute({
              route: 'list-part',
              data: { project: project },
            })
          "
        >
          {{
            `${project.partCount} ${project.partCount > 1 ? "Parts" : "Part"}`
          }}
          <img src="/images/icon/category/expand-arrow.svg" alt="" />
        </div>
      </div>
      <div class="block__content">
        <div class="block__content__title">Number of Toolings</div>
        <div
          class="content__hyper-link"
          @click="
            handleChangeRoute({
              route: 'list-mold',
              data: { project: project },
            })
          "
        >
          {{
            `${project.moldCount} ${
              project.moldCount > 1 ? "Toolings" : "Tooling"
            }`
          }}
          <img src="/images/icon/category/expand-arrow.svg" alt="" />
        </div>
      </div>
      <div class="block__content">
        <div class="block__content__title">Number of Suppliers</div>
        <div
          class="content__hyper-link"
          @click="
            handleChangeRoute({
              route: 'list-supplier',
              data: { project: project },
            })
          "
        >
          {{
            `${project.supplierCount} ${
              project.supplierCount > 1 ? "Suppliers" : "Supplier"
            }`
          }}
          <img src="/images/icon/category/expand-arrow.svg" alt="" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
const TRUNCATED_SIZE = 20;
module.exports = {
  name: "project-card",
  props: {
    resources: Object,
    index: Number,
    project: {
      type: Object,
      default: {
        id: null,
        productionDemand: null,
        molds: [],
        name: "",
        parts: [],
        projectImage: null,
        suppliers: [],
        totalProduced: 0,
      },
    },
  },
  computed: {
    shouldTruncate() {
      return this.project.name.length > TRUNCATED_SIZE;
    },
    getTruncateName(){
      return this.project.name.slice(0, TRUNCATED_SIZE) + "...";
    }
  },
  methods: {
    ctaAnimation(index) {
      const el = this.$refs[`view-button-${index}`];
      if (el) {
        el.classList.add("cta-secondary-animation");
        setTimeout(() => {
          el.classList.remove("cta-secondary-animation");
          this.chooseProject();
        }, 700);
      }
    },
    getImage(projectImage) {
      if (projectImage) {
        return {
          url: projectImage.saveLocation,
          status: "EXIST",
        };
      }
      return {
        url: "/images/icon/category/not-found-image.svg",
        status: "NOT_EXIST",
      };
    },
    chooseProject() {
      this.$emit("choose-project", this.project);
    },
    handleChangeRoute(route) {
      this.$emit("change-route", route);
    },
  },
  watch: {
    project(newVal) {
      console.log("PROJECT_CARD>>>project", newVal);
    },
    index(newVal) {
      console.log("PROJECT_CARD>>>index", newVal);
    },
  },
};
</script>

<style scoped>
</style>
<style scoped src="/css/cta-animation.css"  cus-no-cache></style>
