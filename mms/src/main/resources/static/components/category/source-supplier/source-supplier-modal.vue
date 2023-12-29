<template>
  <base-dialog
    :visible.sync="isVisible"
    :title="modalTitle"
    @close="handleCloseDialog"
    dialog-classes="modal-lg"
    :body-styles="{
      'padding': '20px 26px',
    }"
  >
    <div
      v-if="isListSuppliers"
      class="list-suppliers"
    >

      <!-- Filter sect -->
      <div class="sect sect-1">
        <div class="custom-col">
          <div
            class="form-item"
            style="margin-bottom: 25px"
          >
            <label>Product</label>
            <div style="position: relative">
              <common-select-button
                type="secondary"
                :selected="selectedProduct"
                :get-label="(product) => product.name"
                :is-active="showListProductsDropdown"
                @click.stop="handleShowListProducts"
              >
                Select Product
              </common-select-button>
              <common-popover
                :is-visible="showListProductsDropdown"
                :position="{ top: 'calc(100% + 6px)', left: '-15px' }"
                @close="showListProductsDropdown = false"
              >
                <common-dropdown
                  :style="{ position: 'static' }"
                  placeholder="Select Product"
                  :items="listProducts"
                  :click-handler="handleSelectProduct"
                  :searchbox="true"
                ></common-dropdown>
              </common-popover>
            </div>
          </div>
          <div class="form-item">
            <label>Part</label>
            <div style="position: relative">
              <common-select-button
                type="secondary"
                :selected="selectedPart"
                :get-label="(part) => part.name"
                :is-active="showListPartsDropdown"
                @click.stop="handleShowListParts"
              >
                Select Part
              </common-select-button>
              <common-popover
                :is-visible="showListPartsDropdown"
                :position="{ top: 'calc(100% + 6px)', left: '-15px' }"
                @close="showListPartsDropdown = false"
              >
                <common-dropdown
                  :style="{ position: 'static' }"
                  placeholder="Select Part"
                  :items="listParts"
                  :click-handler="handleSelectPart"
                  :searchbox="true"
                ></common-dropdown>
              </common-popover>
            </div>
          </div>
        </div>
        <div class="custom-col">
          <div
            class="form-item"
            style="margin-bottom: 25px"
          >
            <label>Production Demand</label>
            <input
              type="text"
              :value="productionDemandFormatted"
              readonly
            />
          </div>
          <div class="form-item">
            <label>Unfulfilled Demand</label>
            <input
              type="text"
              :value="unfulfilledDemandFormatted"
              readonly
            />
          </div>
        </div>
        <div
          class="custom-col"
          style="display: flex; align-self: flex-start; justify-content: flex-end;"
        >
          <!-- datepicker -->
          <week-picker-popup
            @on-close="handleChangeDate"
            :current-date="timeState"
            :date-range="timeRange"
            :button-type="'secondary'"
          ></week-picker-popup>
        </div>
      </div>
      <!-- End filter sect -->

      <!-- List supplier sect -->
      <div
        class="sect sect-2"
        style="margin-top: 30px"
      >
        <source-supplier-table
          :data="listSuppliers"
          @click-cell="handleClickCell"
          @change="handleChangeProductionDemand"
        ></source-supplier-table>
      </div>
      <!-- End list supplier sect -->

      <div
        class="sect sect-3"
        style="display: flex; justify-content: flex-end; margin-top: 70px"
      >
        <a
          href="javascript:void(0)"
          class="btn-custom btn-outline-custom-primary"
          @click="handleCloseDialog"
          style="margin-right: 6px"
        >Cancel</a>
        <a
          href="javascript:void(0)"
          class="btn-custom btn-custom-primary"
          @click="handleSubmit"
        >Save</a>
      </div>
    </div>
    <div
      v-else-if="isDetailSupplier"
      class="detail-supplier"
    >
      <a
        href="javascript:void(0)"
        class="back-button"
        @click="handleNavigateBack()"
      >
        <span class="icon back-arrow"></span>
        <span class="back">Back to Supplier List</span>
      </a>
      <company-details-data
        :resources="resources"
        :company="selectedSupplier"
        :is-doash-board="false"
        :style-custom="{ padding: '0', 'margin-top': '15px' }"
      ></company-details-data>
    </div>
    <div v-else-if="isListToolings">
      <!-- Filter sect -->
      <div class="sect sect-1">
        <div class="custom-col">
          <div
            class="form-item"
            style="margin-bottom: 25px"
          >
            <label>Product</label>
            <div style="position: relative">
              <common-select-button
                type="secondary"
                :selected="selectedProduct"
                :get-label="(product) => product.name"
                @click.stop="handleShowListProducts"
              >
                Select Product
              </common-select-button>
              <common-popover
                :is-visible="showListProductsDropdown"
                :position="{ top: 'calc(100% + 6px)', left: '-15px' }"
                @close="showListProductsDropdown = false"
              >
                <common-dropdown
                  :style="{ position: 'static' }"
                  placeholder="Select Product"
                  :items="listProducts"
                  :click-handler="handleSelectProduct"
                  :searchbox="true"
                ></common-dropdown>
              </common-popover>
            </div>
          </div>
          <div class="form-item">
            <label>Part</label>
            <div style="position: relative">
              <common-select-button
                type="secondary"
                :selected="selectedPart"
                :get-label="(part) => part.name"
                @click.stop="handleShowListParts"
              >
                Select Part
              </common-select-button>
              <common-popover
                :is-visible="showListPartsDropdown"
                :position="{ top: 'calc(100% + 6px)', left: '-15px' }"
                @close="showListPartsDropdown = false"
              >
                <common-dropdown
                  :style="{ position: 'static' }"
                  placeholder="Select Part"
                  :items="listParts"
                  :click-handler="handleSelectPart"
                  :searchbox="true"
                ></common-dropdown>
              </common-popover>
            </div>
          </div>
        </div>
        <div class="custom-col">
          <div
            class="form-item"
            style="margin-bottom: 25px"
          >
            <label>Production Demand</label>
            <input
              type="text"
              :value="productionDemandFormatted"
              readonly
            />
          </div>
          <div class="form-item">
            <label>Unfulfilled Demand</label>
            <input
              type="text"
              :value="unfulfilledDemandFormatted"
              readonly
            />
          </div>
        </div>
        <div
          class="custom-col"
          style="display: flex; align-self: flex-start; justify-content: flex-end;"
        >
          <!-- datepicker -->
          <week-picker-popup
            :style="{'width': 'fit-content', 'margin-left': 'auto'}"
            :current-date="timeState"
            :date-range="timeRange"
            :button-type="'secondary'"
            @on-close="handleChangeDate"
          ></week-picker-popup>
        </div>
      </div>
      <!-- End filter sect -->
      <div class="sect sect-2">
        <div class="navigation-bar">
          <a
            href="javascript:void(0)"
            class="back-button"
            @click="handleNavigateBack()"
          >
            <span class="icon back-arrow"></span>
          </a>
          <div
            class="custom-breadcrumb"
            style="display: flex;"
          >
            Company/&nbsp;<a
              href="javascript:void(0)"
              @click="handleNavigateBack()"
            >{{selectedSupplierName}}</a>
            / Toolings
          </div>
        </div>
        <tooling-table :data="listToolings"></tooling-table>
        <div
          class="tooling-pagination"
          style="display: flex; justify-content: flex-end;"
        >
          <span class="tooling-pagination__page-number">{{ `${currentPage} of ${totalPage}` }}</span>
          <div class="paging__arrow">
            <a
              href="javascript:void(0)"
              class="paging-button"
              :class="{ 'inactive-button': currentPage <= 1 }"
              @click="toolingPagination.currentPage--"
            >
              <img
                src="/images/icon/category/paging-arrow.svg"
                alt=""
              />
            </a>
            <a
              href="javascript:void(0)"
              class="paging-button"
              @click="toolingPagination.currentPage++"
              :class="{ 'inactive-button': currentPage >= totalPage }"
            >
              <img
                src="/images/icon/category/paging-arrow.svg"
                style="transform: rotate(180deg)"
                alt=""
              />
            </a>
          </div>
        </div>
      </div>
    </div>
  </base-dialog>
</template>


<script src="/components/category/source-supplier/source-supplier-modal.js"></script>

<style scoped>
.sect-1 {
  display: flex;
  align-items: center;
  column-gap: 20px;
}

.sect-1 .custom-col {
  flex: 1;
}

.form-item {
  display: flex;
  align-items: center;
}

.form-item label {
  width: 150px;
  margin-bottom: 0;
}

.form-item input {
  border: none;
  background-color: #fbfcfd;
  color: #4b4b4b;
}

.form-item input:focus-visible {
  outline: none;
}

.dropdown-list li label {
  width: 100%;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 15px;
}

.icon.back-arrow {
  display: inline-block;
  width: 16.5px;
  height: 26.6px;
  mask-image: url("/images/icon/category/back-arrow.svg");
  mask-repeat: no-repeat;
  mask-position: center;
  mask-size: contain;
  -webkit-mask-image: url("/images/icon/category/back-arrow.svg");
  -webkit-mask-repeat: no-repeat;
  -webkit-mask-position: center;
  -webkit-mask-size: contain;
  background-color: #3491ff;
  transition: 200ms;
}
.icon.back-arrow:hover {
  background-color: #3585e5;
}

.navigation-bar {
  margin-top: 25px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.paging-button {
  padding: 6px 8px;
  border-radius: 3px;
  background-color: #3491ff;
  margin-left: 15px;
  display: inline-flex;
}
.paging-button.inactive-button {
  background-color: #c4c4c4;
  pointer-events: none;
}
.tooling-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
  margin-bottom: 65px
}
</style>
