<template>
  <div>
    <div class="block__image">
      <img
        :src="getImage(brand.brandImage || brand.projectImage).url"
        :class="{
          'exsisted-image':
            getImage(brand.brandImage || brand.projectImage).status == 'EXIST',
        }"
        alt=""
      />
    </div>
    <div class="block__button">
      <!-- <div v-if="brand.isNoBrand" style="height: 28px"></div> -->
      <a
        :ref="`view-brand-button-${index}`"
        href="javascript:void(0)"
        class="cta-secondary"
        @click="ctaAnimation(index)"
        >View Brand Profile</a
      >
    </div>
    <div class="block__container">
      <div class="block__content">
        <div class="block__content__title">Brand</div>
        <div v-if="brand.isNoBrand" class="content__no-hyper-link d-flex">
          <a-tooltip v-if="shouldTruncate" placement="bottom">
            <template slot="title">
              <div style="padding: 6px 8px">
                <span>{{ brand.name }}</span>
              </div>
            </template>
            <div class="truncate-card-title">{{ getTruncateName }}</div>
          </a-tooltip>
          <div v-else style="margin-right: 10px">{{ brand.name }}</div>
        </div>
        <div v-else class="content__hyper-link d-flex" @click="chooseBrand()">
          <a-tooltip v-if="shouldTruncate" placement="bottom">
            <template slot="title">
              <div style="padding: 6px 8px">
                <span>{{ brand.name }}</span>
              </div>
            </template>
            <div class="truncate-card-title">{{ getTruncateName }}</div>
          </a-tooltip>
          <div v-else style="margin-right: 10px">{{ brand.name }}</div>
          <img src="/images/icon/category/expand-arrow.svg" alt="" />
        </div>
      </div>
      <div class="block__content">
        <div class="block__content__title">Number of Products</div>
        <div class="content__hyper-link" @click="chooseBrand()">
          {{
            `${brand.productCount} ${
              brand.productCount > 1 ? "Products" : "Product"
            }`
          }}
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
              data: { brand: brand },
            })
          "
        >
          {{ `${brand.partCount} ${brand.partCount > 1 ? "Parts" : "Part"}` }}
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
              data: { brand: brand },
            })
          "
        >
          {{
            `${brand.moldCount} ${brand.moldCount > 1 ? "Toolings" : "Tooling"}`
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
              data: { brand: brand },
            })
          "
        >
          {{
            `${brand.supplierCount} ${
              brand.supplierCount > 1 ? "Suppliers" : "Supplier"
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
  name: "brand-card",
  props: {
    resources: Object,
    index: Number,
    brand: {
      type: Object,
      default: {
        id: null,
        molds: [],
        name: "",
        parts: [],
        products: [],
        brandImage: null,
        suppliers: [],
        totalProduced: 0,
      },
    },
  },
  computed: {
    shouldTruncate() {
      return this.brand.name.length > TRUNCATED_SIZE;
    },
    getTruncateName() {
      return this.brand.name.slice(0, TRUNCATED_SIZE) + "...";
    },
  },
  methods: {
    ctaAnimation(index) {
      const el = this.$refs[`view-brand-button-${index}`];
      if (el) {
        el.classList.add("cta-secondary-animation");
        setTimeout(() => {
          el.classList.remove("cta-secondary-animation");
          this.chooseBrand();
        }, 700);
      }
    },
    getImage(brandImage) {
      if (brandImage) {
        return {
          url: brandImage.saveLocation,
          status: "EXIST",
        };
      }
      return {
        url: "/images/icon/category/not-found-image.svg",
        status: "NOT_EXIST",
      };
    },
    chooseBrand() {
      this.$emit("choose-brand", this.brand);
    },
    handleChangeRoute(route) {
      this.$emit("change-route", route);
    },
  },
  watch: {
    brand(newVal) {
      console.log("BRAND_CARD>>>brand", newVal);
    },
    index(newVal) {
      console.log("BRAND_CARD>>>index", index);
    },
  },
};
</script>

<style scoped>
.truncated-brand-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.content__no-hyper-link {
  font-weight: 400;
  font-size: 16px;
  color: rgb(89, 89, 89);
}
</style>
<style scoped src="/css/cta-animation.css" cus-no-cache></style>
