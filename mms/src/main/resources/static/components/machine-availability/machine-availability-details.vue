<template>
  <div
    id="op-machine-availability-details"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    style="overflow-y: scroll"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <form @submit.prevent="submit">
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
          <div class="custom-modal-title">
            <div class="modal-title">
              <span class="modal-title-text">Machine Report</span>
              <a
                id="detail-button"
                href="javascript:void(0)"
                class="header-button"
                :class="{
                  'header-button-active': selectedButton == 'detail-button',
                }"
                @click="buttonAnimation('detail-button')"
                >Detail</a
              >
              <a
                href="javascript:void(0)"
                class="header-button header-button-disable"
                >Notes</a
              >
            </div>
            <div
              class="t-close-button close-button"
              @click="dimissModal"
              aria-hidden="true"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="11.699"
                height="11.699"
                viewBox="0 0 11.699 11.699"
              >
                <g
                  id="Group_8882"
                  data-name="Group 8882"
                  transform="translate(0 0)"
                >
                  <rect
                    id="Rectangle_10"
                    data-name="Rectangle 10"
                    width="2.363"
                    height="14.181"
                    transform="translate(10.028 0) rotate(45)"
                  />
                  <rect
                    id="Rectangle_11"
                    data-name="Rectangle 11"
                    width="2.364"
                    height="14.181"
                    transform="translate(11.699 10.028) rotate(135)"
                  />
                </g>
              </svg>
            </div>
            <div class="head-line"></div>
          </div>
          <div class="modal-body" style="max-height: 500px">
            <div class="card">
              <div class="card-header">
                <i class="fa fa-align-justify"></i>
                <span>Machine</span>
              </div>

              <table class="table table-mold-details">
                <tbody>
                  <tr>
                    <th v-text="resources['machine_id']">Machine ID</th>
                    <td>{{ machine.machineCode }}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['Company_Name_Company_ID']">
                      Company Name (Company ID)
                    </th>
                    <td>
                      {{
                        machine.companyName + " (" + machine.companyCode + ")"
                      }}
                    </td>
                  </tr>
                  <tr>
                    <th v-text="resources['Location_Name_Location_ID']">
                      Location Name (Location ID)
                    </th>
                    <td>
                      {{
                        machine.locationName + " (" + machine.locationCode + ")"
                      }}
                    </td>
                  </tr>
                  <tr>
                    <th v-text="resources['line']">Line</th>
                    <td>{{ machine.line }}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['machine_maker']">Machine Maker</th>
                    <td>{{ machine.machineMaker }}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['machine_type']">Machine Type</th>
                    <td>{{ machine.machineType }}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['machine_model']">Machine Model</th>
                    <td>{{ machine.machineModel }}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['machine_ton']">Machine Tonnage</th>
                    <td>
                      {{
                        machine.machineTonnage != null
                          ? machine.machineTonnage + " ton"
                          : ""
                      }}
                    </td>
                  </tr>
                  <!--                <tr>-->
                  <!--                  <th v-text="resources['status']"></th>-->
                  <!--                  <td>{{machine.enabled ? "Enabled" : "Disabled"}}</td>-->
                  <!--                </tr>-->
                  <tr
                    v-for="(item, index) in listConfigCustomField"
                    :key="index"
                  >
                    <th>{{ item.fieldName }}</th>
                    <td>
                      {{ item.defaultInputValue }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="modal-footer text-center">
            <button
              type="button"
              class="btn btn-secondary"
              v-on:click="dimissModal"
              v-text="resources['close']"
            ></button>
          </div>
        </div>
      </div>
    </form>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
  },
  data() {
    return {
      isDoashBoard: false,
      machine: {
        machineCode: "",
      },
      listConfigCustomField: null,
      selectedButton: "detail-button",
    };
  },
  methods: {
    getListConfigCustomField() {
      var self = this;
      const url = `/api/custom-field-value/list-by-object?objectType=MACHINE&objectId=${self.machine.id}`;
      axios
        .get(url)
        .then((response) => {
          self.listConfigCustomField = response.data.map((item) => ({
            propertyGroup: item.propertyGroup,
            fieldName: item.fieldName,
            defaultInputValue:
              item.customFieldValueDTOList.length !== 0
                ? item.customFieldValueDTOList[0].value
                : null,
            id: item.id,
            required: item.required,
          }));
        })
        .catch((error) => {
          console.log(error.response);
        })
        .finally(() => {
          console.log(self.listConfigCustomField, "listConfigCustomField");
        });
    },
    showDetails: function (machine, isDoashBoard) {
      console.log(machine, "machine here");
      this.machine = machine;
      this.isDoashBoard = isDoashBoard;
      this.getListConfigCustomField();
      $("#op-machine-availability-details").modal("show");
    },

    // getPic(file) {
    //     return "http://49.247.200.147" + file.saveLocation;
    // },

    dimissModal: function () {
      if (this.isDoashBoard) {
        $("#op-machine-availability-details").modal("hide");
        return (location.href = `${Common.PAGE_URL.MACHINE}`);
      }
      $("#op-machine-availability-details").modal("hide");
    },
    buttonAnimation(id) {
      const el = document.getElementById(id);
      if (el) {
        el.classList.add("header-button-primary-animation");
        setTimeout(() => {
          el.classList.remove("header-button-primary-animation");
          this.selectedButton = id;
        }, 700);
      }
    },
  },
  mounted() {},
};
</script>
<style>
.ant-select-dropdown {
  z-index: 99999999;
}
.ant-select-dropdown-menu-item-selected {
  font-weight: 400;
  background-color: #fff;
}
.custom-modal-body-user {
  padding: 23px 32px 26px 32px;
  overflow-y: auto;
  max-height: 600px;
}
.head-line {
  left: 0;
}
.custom-modal-title {
  border-radius: 6px 6px 0 0;
}
.modal-title {
  font-weight: bold;
  margin-top: 12px;
  margin-left: 30px;
  margin-bottom: 12px;
  font-size: 16px;
}
.custom-modal-title {
  border-radius: 6px 6px 0 0;
  background: #f5f8ff;
  color: #4b4b4b;
  display: flex;
  justify-content: space-between;
  position: relative;
  align-items: end;
  padding: 8px 15px 0 0;
  align-items: center;
}
.modal-title-text {
  margin-right: 15px;
}
.header-button {
  padding: 5px 40px;
  font-size: 16px;
  border: 1px solid #d0d0d0;
  background-color: #fff;
  color: #888888;
  border-radius: 3px;
  text-decoration: none !important;
  margin-right: 4px;
  font-weight: 500;
}
.header-button:hover {
  color: #3491ff;
  background-color: #f2f5fa;
  border: 1px solid #3491ff;
}
.header-button-active {
  background-color: #3491ff !important;
  color: #fff !important;
  border: 1px solid #3491ff;
}
.header-button-primary-animation {
  animation: header-button-primary 0.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
  animation-timing-function: ease-in-out;
  border: 1px solid transparent;
  outline: 1px solid transparent;
}
@keyframes header-button-primary {
  0% {
  }
  33% {
    color: #0279fe;
    outline: 3px solid #89d1fd;
  }
  66% {
    color: #0279fe;
    outline: 3px solid #89d1fd;
  }
  100% {
  }
}
.header-button-disable {
  opacity: 0.7;
  pointer-events: none;
}
</style>
