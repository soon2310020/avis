<template>
    <div class="container content-group" :style="{height: `${height}px`}">
        <div class="content-group-header">
            <div class="row">
                <div class="col-md-5 tab-group-title active">
                    <p v-text="resources['tooling_id']"></p>
                    <span class="__icon-sort" :class="isDesc" @click="sort('tooling')"></span>
                </div>
                <div class="col-md-4 tab-group-title">
                    <p v-text="resources['part']"></p>
                    <span class="__icon-sort" :class="partSortType" @click="sort('part')"></span>
                </div>
                <div class="col-md-3 tab-group-title">
                </div>
            </div>
        </div>
        <div class="content-group-wrapper">
            <div v-for="(tooling, toolingIndex) in toolingList" :key="tooling.id" class="row">
                <div class="col-md-5">
                    <div class="tab-group-item">
                        <div v-text="tooling.equipmentCode"></div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="tab-group-item">
                        <mold-parts-dropdown-view :parts="tooling.parts" :mold="tooling" :index="toolingIndex"
                            :show-part-chart="showPartChart"></mold-parts-dropdown-view>
                    </div>
                </div>
                <div class="col-md-3 action-column">
                    <div v-if="checkToolingSelected(tooling.id)"
                        class="d-flex align-items-center h-100 justify-content-start"
                        @click="addToolingToTab(tooling.id)">
                        <span style="margin-right: 4px">Add to tab</span>
                        <img src="/images/icon/add-circle-white.svg" alt="">
                    </div>
                    <div v-else class="d-flex align-items-center h-100 justify-content-start"
                        @click="removeToolingFromTab(tooling.id)">
                        <span style="margin-right: 4px">Remove from tab</span>
                        <img src="/images/icon/sub-circle-white.svg" alt="">
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
module.exports = {
    props: {
        height: {
            type: [Number, String],
            default: () => (315)
        },
        columnList: {
            type: Array,
            default: () => []
        }
    },
    data() {
        return {
            sortField: '',
            isDesc: false,
        }
    },
}
</script>

<style scoped>
.content-group-wrapper {
  overflow-y: scroll;
  overflow-x: hidden;
  padding-left: 34px;
  color: #4B4B4B;
}
.content-group-wrapper > .row{
  margin-left: -31px;
  padding-left: 19px;
}
.content-group-wrapper > .row:nth-of-type(odd) {
  background: #FBFCFD;
}
.content-group-header {
  background-color: rgb(245, 248, 255);
  padding-left: 34px;
}
.action-column > div {
  cursor: pointer;
}

</style>