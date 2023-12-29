<template>
  <div class="header-wrapper">
    <div class="form-group row">
      <div class="col-lg-3 font-weight-bold">
        {{ getNumberOfTypeTitle }}
      </div>
      <div class="col-md-5">{{ numberOfObjectType }}</div>
    </div>
    <div class="form-group row">
      <div class="col-lg-3 font-weight-bold">
        {{ resources['completion_rate'] }}
      </div>
      <div class="col-md-5">
        <dynamic-progress-bar :percentage="completionRate"></dynamic-progress-bar>
      </div>
    </div>
    <div class="form-group row">
      <div class="col-lg-3 font-weight-bold">
        {{ resources['request_id'] }}
      </div>
      <div class="col-md-5">
        <base-drop-count :is-hyper-link="true" :data-list="requestList"
                         :title="requestList[0] ? requestList[0].requestId : ''" @title-click="() => $emit('handle-click-request', requestList[0])">
          <div style="height: 165px; overflow: auto; width: 146px">
            <div v-for="item in requestList" :key="item.id"
                 class="custom-hyper-link request-item"
                 @click="() => $emit('handle-click-request', item)"
                 style="display: inline-flex; width: 100%; white-space: nowrap; cursor: pointer;">
              {{ item.requestId }}
            </div>
          </div>
        </base-drop-count>
      </div>
    </div>
  </div>
</template>

<script>
const OBJECT_TYPES = {
  COMPANY: 'COMPANY',
  LOCATION: 'LOCATION',
  PART: 'PART',
  TOOLING: 'TOOLING',
  MACHINE: 'MACHINE'
}

module.exports = {
  props: {
    resources: {
      type: Object,
      default: () => ({})
    },
    objectType: {
      type: String,
      default: () => ''
    },
    numberOfObjectType: {
      type: Number,
      default: () => 0
    },
    completionRate: {
      type: Number,
      default: () => 0
    },
    requestList: {
      type: Array,
      default: () => []
    }
  },
  components: {
    "dynamic-progress-bar": httpVueLoader("/components/@base/progress-bar/dynamic-progress-bar.vue"),
  },
  setup(props, ctx) {
    // STATE //

    // COMPUTED //
    const getNumberOfTypeTitle = computed(() => {
      switch (props.objectType) {
        case OBJECT_TYPES.COMPANY:
          return props.resources['number_of_companies'];
        case OBJECT_TYPES.LOCATION:
          return props.resources['number_of_plants'];
        case OBJECT_TYPES.PART:
          return props.resources['number_of_parts'];
        case OBJECT_TYPES.MACHINE:
          return props.resources['number_of_machines'];
        case OBJECT_TYPES.TOOLING:
          return props.resources['number_of_toolings'];
      }
    })


    // WATCH //

    // watch(() => props.visible, (newVal) => {
    // });

    // METHOD //
    const handleSubmit = () => {
    }


    // created method //
    const initModal = () => {
    }

    // CREATED //
    initModal()

    return {
      // STATE //
      // COMPUTED //
      getNumberOfTypeTitle,
      // METHOD //
    }
  }
}
</script>

<style scoped>
.header-wrapper {
  margin-top: 27px;
}
.request-item {
  padding: 13px 14px;
  border-bottom: 1px solid #D6DADE;
  border-radius: 0px;
}
</style>