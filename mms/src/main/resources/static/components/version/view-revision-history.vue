<template>
  <div
    id="revision-history"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="title-part-chart"
    aria-hidden="fa"
  >
    <div class="modal-dialog modal-dialog-centered modal-lg" role="document">
      <div class="modal-content show-child modal-history">
        <div class="modal-header">
          <h4 class="modal-title" id="title-part-chart" v-text="resources['edit_history']"> </h4>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
          </button>
        </div>
            <div class="modal-body">
              <h5 v-if="revisionObjectType === 'USER'" class="part-title" v-text="resources['user']"></h5>
              <h5 v-else-if="revisionObjectType === 'ROLE'" class="part-title" v-text="resources['access_group']"></h5>
              <h5 v-else-if="revisionObjectType === 'COMPANY'" class="part-title" v-text="resources['company']"></h5>
              <h5 v-else-if="revisionObjectType === 'LOCATION'" class="part-title" v-text="resources['location']"></h5>
              <h5 v-else-if="revisionObjectType === 'CATEGORY'" class="part-title" v-text="resources['category']"></h5>
              <h5 v-else-if="revisionObjectType === 'PART'" class="part-title" v-text="resources['part']"></h5>
              <h5 v-else-if="revisionObjectType === 'TERMINAL'" class="part-title" v-text="resources['terminal']"></h5>
              <h5 v-else-if="revisionObjectType === 'MOLD'" class="part-title" v-text="resources['tooling']"></h5>
              <h5 v-else-if="revisionObjectType === 'COUNTER'" class="part-title" v-text="resources['counter']"></h5>
              <h5 v-else-if="revisionObjectType === 'MACHINE'" class="part-title">Machine</h5>
              <h5 v-else-if="revisionObjectType === 'CHECKLIST_MAINTENANCE'" class="part-title">Maintenance Checklist</h5>

              <div v-if="totalPage == 0">No results.</div>
              <div v-show="totalPage > 0">
                  <div class="border-table">
                  <table class="table table-striped">
                    <thead>
                      <tr>
                        <th scope="col" v-text="resources['time']"></th>
                        <th scope="col" v-text="resources['edited_by']"></th>
                        <th scope="col" v-text="resources['details']"></th>
                        <th scope="col"></th>
                      </tr>
                    </thead>
                    <tbody>
                    <template v-for="item in revisionHistory">
                      <tr>
                        <td>
                            <!--{{item.createdDateTime}}-->
                            {{formatToDateTime(item.createdAtValue)}}
                        </td>
                        <td>{{item.editedByName}}</td>
                        <td>
                          <button type="button" class="btn btn-success" @click.prevent="showViewHistoryChange(item)" v-text="resources['view']"></button>
                        </td>
                        <td>
                          <button v-show="item.currentVersion" type="button" class="btn button-disable" style="cursor: default" disabled v-text="resources['current_v']"></button>
                          <a href='#' v-if="revisionObjectType !== 'COUNTER'" v-show="!item.currentVersion" @click.prevent="restore(item)">
                            <img width="20" style="margin-right: 5px" src='/images/icon/restore.svg'/><span v-text="resources['restore']"></span>
                          </a>
                          <a href='#' v-show="!item.currentVersion" v-else-if="revisionObjectType === 'COUNTER'" :disabled='!canRestore' @click.prevent="restore(item)">
                            <img width="20" style="margin-right: 5px" src='/images/icon/restore-grey.svg'/><span v-text="resources['restore']"></span>
                          </a>
                        </td>
                      </tr>
                    </template>
                    </tbody>
                  </table>
                  </div>
                  <div class="row" v-show="totalPage > 0">
                      <div class="col-md-8">
                        <ul class="pagination" id="paging">
                          <li v-if='currentPage > 1'>
                                <a class="page-link" href="#" @click.prevent="previous()" v-text="resources['previous']"></a>
                          </li>
                          <!-- <li class="page-item"><a class="page-link" href="#">1</a></li>
                          <li class="page-item"><a class="page-link" href="#" @click.prevent="gotoPage(2)">2</a></li>
                          <li class="page-item"><a class="page-link" href="#">3</a></li>
                          <li class="page-item"><a class="page-link" href="#">Next</a></li> -->
                          <template v-for="item in range(minPage, maxPage)">
                              <li class="page-item" :class="{active: item === currentPage}">
                                <a class="page-link" href="#" @click.prevent="gotoPage(item)">{{ item }}</a>
                              </li>
                          </template>
                          <li v-if='currentPage < totalPage'>
                                <a class="page-link" href="#" @click.prevent="next()" v-text="resources['next']"></a>
                          </li>
                        </ul>
                      </div>
                    </div>
                </div>
            </div>
            <!--<div class='modal-footer card-footer-close-btn'>-->
              <!--<button type="button" data-dismiss="modal" aria-label="Close" class="btn btn-secondary">Close</button>-->
            <!--</div>-->

    </div>
        <confirm :resources="resources"></confirm>
        <view-user-history-change @open-modal-custom='openModalParent()' :resources="resources"></view-user-history-change>
        <view-role-history-change @open-modal-custom='openModalParent()' :resources="resources"></view-role-history-change>
        <view-company-history-change @open-modal-custom='openModalParent()' :resources="resources"></view-company-history-change>
        <view-machine-history-change @open-modal-custom='openModalParent()' :resources="resources"></view-machine-history-change>
        <view-location-history-change @open-modal-custom='openModalParent()' :resources="resources"></view-location-history-change>
        <view-category-history-change @open-modal-custom='openModalParent()' :resources="resources"></view-category-history-change>
        <view-part-history-change @open-modal-custom='openModalParent()' :resources="resources"></view-part-history-change>
        <view-terminal-history-change @open-modal-custom='openModalParent()' :resources="resources"></view-terminal-history-change>
        <view-mold-history-change @open-modal-custom='openModalParent()' :resources="resources"></view-mold-history-change>
        <view-counter-history-change @open-modal-custom='openModalParent()' :resources="resources"></view-counter-history-change>
        <view-checklist-history-change @open-modal-custom='openModalParent()' :resources="resources"></view-checklist-history-change>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
  },
  data() {
    return {
      revisionHistory: [],
      totalPage: 0,
      currentPage: 1,
      id: Number,
      revisionObjectType: String,
      paging: [],
      roleType: "",
      canRestore: false,
      defaultPagesToDisplay: 5,
    };
  },
  components: {
    confirm: httpVueLoader("/components/version/confirm.vue"),
    "view-user-history-change": httpVueLoader(
      "/components/version/view-user-history-change.vue"
    ),
    "view-role-history-change": httpVueLoader(
      "/components/version/view-role-history-change.vue"
    ),
    "view-company-history-change": httpVueLoader(
      "/components/version/view-company-history-change.vue"
    ),
    "view-machine-history-change": httpVueLoader(
      "/components/version/view-machine-history-change.vue"
    ),
    "view-location-history-change": httpVueLoader(
      "/components/version/view-location-history-change.vue"
    ),
    "view-category-history-change": httpVueLoader(
      "/components/version/view-category-history-change.vue"
    ),
    "view-part-history-change": httpVueLoader(
      "/components/version/view-part-history-change.vue"
    ),
    "view-terminal-history-change": httpVueLoader(
      "/components/version/view-terminal-history-change.vue"
    ),
    "view-mold-history-change": httpVueLoader(
      "/components/version/view-mold-history-change.vue"
    ),
    "view-counter-history-change": httpVueLoader(
      "/components/version/view-counter-history-change.vue"
    ),
    "view-checklist-history-change": httpVueLoader(
      "/components/version/view-checklist-history-change.vue"
    ),
  },
  computed: {
    pagesToDisplay() {
      if (this.totalPage > 0 && this.totalPage < this.defaultPagesToDisplay) {
        return this.totalPage;
      }
      return this.defaultPagesToDisplay;
    },
    minPage() {
      if (this.currentPage >= this.pagesToDisplay) {
        const pagesToAdd = Math.floor(this.pagesToDisplay / 2);
        const newMaxPage = pagesToAdd + this.currentPage;
        if (newMaxPage > this.totalPage) {
          return this.totalPage - this.pagesToDisplay + 1;
        }
        return this.currentPage - pagesToAdd;
      } else {
        return 1;
      }
    },
    maxPage() {
      if (this.currentPage >= this.pagesToDisplay) {
        const pagesToAdd = Math.floor(this.pagesToDisplay / 2);
        const newMaxPage = pagesToAdd + this.currentPage;
        if (newMaxPage < this.totalPage) {
          return newMaxPage;
        } else {
          return this.totalPage;
        }
      } else {
        return this.pagesToDisplay;
      }
    },
  },
  methods: {
    showRevisionHistories: function (id, revisionObjectType, roleType) {
      this.resetData();
      this.id = id;
      this.revisionObjectType = revisionObjectType;
      this.roleType = roleType;
      this.openModalParent();
      // role type is equipmentStatus in counter
      // role type is roleType in role
      this.canRestore = roleType === "AVAILABLE";
      this.loadRevisionHistory();
      $("#revision-history").modal({
        backdrop: "static",
        keyboard: false,
      });
      $("#revision-history").modal("show");
    },
    range(min, max) {
      let arr = [];
      for (let i = min; i <= max; i++) {
        arr.push(i);
      }
      return arr;
    },
    getComponentHistoryChange: function (item) {
      var child;
      console.log("object type: " + this.revisionObjectType);
      if ("USER" === this.revisionObjectType) {
        child = Common.vue.getChild(this.$children, "view-user-history-change");
        if (child != null) {
          child.showView(item);
        }
      } else if ("ROLE" === this.revisionObjectType) {
        child = Common.vue.getChild(this.$children, "view-role-history-change");
        if (child != null) {
          child.showView(item, this.roleType);
        }
      } else if ("COMPANY" === this.revisionObjectType) {
        child = Common.vue.getChild(
          this.$children,
          "view-company-history-change"
        );
        if (child != null) {
          child.showView(item);
        }
      } else if ("LOCATION" === this.revisionObjectType) {
        child = Common.vue.getChild(
          this.$children,
          "view-location-history-change"
        );
        if (child != null) {
          child.showView(item);
        }
      } else if ("CATEGORY" === this.revisionObjectType) {
        child = Common.vue.getChild(
          this.$children,
          "view-category-history-change"
        );
        if (child != null) {
          child.showView(item);
        }
      } else if ("PART" === this.revisionObjectType) {
        child = Common.vue.getChild(this.$children, "view-part-history-change");
        if (child != null) {
          child.showView(item);
        }
      } else if ("TERMINAL" === this.revisionObjectType) {
        child = Common.vue.getChild(
          this.$children,
          "view-terminal-history-change"
        );
        if (child != null) {
          child.showView(item);
        }
        // }else if('CHECKLIST_MAINTENANCE' === this.revisionObjectType){
      } else if (
        [
          "CHECKLIST_MAINTENANCE",
          "CHECKLIST_GENERAL",
          "CHECKLIST_REJECT_RATE",
          "CHECKLIST_QUALITY_ASSURANCE",
          "CHECKLIST_REFURBISHMENT",
          "CHECKLIST_DISPOSAL",
          "PICK_LIST",
        ].includes(this.revisionObjectType)
      ) {
        child = Common.vue.getChild(
          this.$children,
          "view-checklist-history-change"
        );
        if (child != null) {
          child.showView(item);
        }
      } else if ("MOLD" === this.revisionObjectType) {
        child = Common.vue.getChild(this.$children, "view-mold-history-change");
        if (child != null) {
          child.showView(item);
        }
      } else if ("COUNTER" === this.revisionObjectType) {
        child = Common.vue.getChild(
          this.$children,
          "view-counter-history-change"
        );
        if (child != null) {
          // roleType  = equipmentStatus
          child.showView(item, this.roleType);
        }
      } else if ("MACHINE" === this.revisionObjectType) {
        child = Common.vue.getChild(
          this.$children,
          "view-machine-history-change"
        );
        if (child != null) {
          // roleType  = equipmentStatus
          child.showView(item, this.roleType);
        }
      }
    },
    showViewHistoryChange: function (item) {
      this.hidenModal();
      this.getComponentHistoryChange(item);
    },
    resetData: function () {
      document.getElementById("paging").style.display = "none";
      (this.revisionHistory = []),
        (this.totalPage = 0),
        (paging = []),
        (this.currentPage = 1);
    },
    loadRevisionHistory: function () {
      let sef = this;
      axios
        .get(
          "/api/version/reversion?originId=" +
            this.id +
            "&revisionObjectType=" +
            this.revisionObjectType +
            "&page=" +
            this.currentPage
        )
        .then(function (response) {
          document.getElementById("paging").style.display = "flex";
          sef.revisionHistory = response.data.revisionHistories;
          sef.totalPage = response.data.totalPage;
          sef.generatePaging();
        });
    },
    restore: function (revision) {
      var child = Common.vue.getChild(this.$children, "confirm");
      var param = {
        id: revision.id,
        revisionObjectType: revision.revisionObjectType,
      };
      if (child != null) {
        // child.showConfirm(param, revision.createdDateTime);
        child.showConfirm(param, vm.formatToDateTime(revision.createdAtValue));
      }
    },
    gotoPage: function (pageNumber) {
      this.currentPage = pageNumber;
      this.loadRevisionHistory();
    },
    next: function () {
      if (this.currentPage < this.totalPage) {
        this.currentPage++;
        this.loadRevisionHistory();
      }
    },
    previous: function () {
      if (this.currentPage > 1) {
        this.currentPage--;
        this.loadRevisionHistory();
      }
    },
    generatePaging: function () {
      this.paging = new Array();

      for (let i = 1; i <= this.totalPage; i++) {
        this.paging.push({
          activate: this.currentPage === i ? true : false,
        });
      }
    },
    hidenModal: function () {
      document.getElementsByClassName("show-child")[0].style.opacity = 0;
    },
    openModalParent: function () {
      document.getElementsByClassName("show-child")[0].style.opacity = 1;
    },
  },
  mounted() {},
};
</script>
<style scoped>
.modal-dialog-centered {
  justify-content: center;
}
.modal-history {
  max-width: 650px;
}

.part-title {
  margin-bottom: 18px;
}

.table {
  /*border: 1px solid #c8ced3;*/
  border-bottom: unset;
  margin-bottom: unset;
}

.border-table {
  border: 1px solid #c8ced3;
  border-radius: 0.25rem !important;
  overflow: hidden;
  margin-bottom: 1rem;
}

.table th:not(:last-child),
.table td:not(:last-child) {
  border-right: 1px solid #c8ced3;
}

.table th:nth-last-child(2),
.table td:nth-last-child(2) {
  border-right: unset;
}

.table tr:last-child td {
  border-bottom: unset;
}

.button-disable {
  background-color: rgb(184, 184, 184);
  color: white;
}
</style>
