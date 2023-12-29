<template>
    <div>
        <ul class="components-tab">
            <li @click="setComponentTab('cta-button')" :active="componentTab === 'cta-button' ? true : false">Cta Button</li>
            <li @click="setComponentTab('xychart')" :active="componentTab === 'xychart' ? true : false">XY Chart</li>
            <li @click="setComponentTab('pie-chart')" :active="componentTab === 'pie-chart' ? true : false">Pie Chart</li>
            <li @click="setComponentTab('dropdown')" :active="componentTab === 'dropdown' ? true : false">Dropdown</li>
          <li @click="setComponentTab('time-picker')" :active="componentTab === 'time-picker' ? true : false">Time Picker</li>
            <li @click="setComponentTab('date-picker')" :active="componentTab === 'date-picker' ? true : false">Date Picker</li>
            <li @click="setComponentTab('tab-button')" :active="componentTab === 'tab-button' ? true : false">Tab Button</li>
            <li @click="setComponentTab('icon-button')" :active="componentTab === 'icon-button' ? true : false">Icon Button</li>
            <li @click="setComponentTab('toggle-button')" :active="componentTab === 'toggle-button' ? true : false">Toggle Button</li>


            <li @click="setComponentTab('action-bar-button')" :active="componentTab === 'action-bar-button' ? true : false">Action Bar Button</li>

            <li @click="setComponentTab('double-dropdown')" :active="componentTab === 'double-dropdown' ? true : false">Double Dropdown</li>
        </ul>
        <cta-button-test v-if="componentTab === 'cta-button'"></cta-button-test>
        <overall-xy-chart-test v-if="componentTab === 'xychart'"></overall-xy-chart-test>
        <pie-chart-test v-if="componentTab === 'pie-chart'"></pie-chart-test>

       <dropdown-test v-if="componentTab === 'dropdown'"></dropdown-test>
      <time-picker-test v-if="componentTab === 'time-picker'"></time-picker-test>
         <date-picker-test v-if="componentTab === 'date-picker'"></date-picker-test>

      <tab-button-component-test v-if="componentTab === 'tab-button'"></tab-button-component-test>

        <icon-button-test v-if="componentTab==='icon-button'"></icon-button-test>

         <toggle-button-test v-if="componentTab === 'toggle-button'"></toggle-button-test>

      <action-bar-button-test v-if="componentTab === 'action-bar-button'"></action-bar-button-test>

        <double-dropdown-test v-if="componentTab === 'double-dropdown'"></double-dropdown-test>

    </div>
</template>

<script>
module.exports = {
    components: {
        'cta-button-test': httpVueLoader('/components/common-test-page/cta-button-test.vue'),
        'overall-xy-chart-test': httpVueLoader('/components/common-test-page/overall-xy-chart-test.vue'),
        'pie-chart-test': httpVueLoader('/components/common-test-page/pie-chart-test.vue'),
        'dropdown-test': httpVueLoader('/components/common-test-page/dropdown-test.vue'),
        'time-picker-test': httpVueLoader('/components/common-test-page/time-picker-test.vue'),
        'date-picker-test': httpVueLoader('/components/common-test-page/date-picker-test.vue'),

      'tab-button-component-test': httpVueLoader('/components/common-test-page/tab-button-component-test.vue'),

      'icon-button-test': httpVueLoader('/components/common-test-page/icon-button-test.vue'),
    'toggle-button-test': httpVueLoader('/components/common-test-page/toggle-button-test.vue'),

      'action-bar-button-test': httpVueLoader('/components/common-test-page/table-actionbar-button-test.vue'),
      'double-dropdown-test': httpVueLoader('/components/common-test-page/double-dropdown-test.vue'),

    },
    data() {
        return {
            componentTab: 'cta-button',



        };
    },
    methods: {
        setComponentTab(param) {
            this.componentTab = param;
        },
    },
};
</script>

<style>
p {
    margin: 0;
}

.components-tab {
    height: 40px;
    display: flex;
    margin: 0;
    padding: 0;
    margin-top: 10px;
    margin-bottom: 20px;
}
.components-tab > li {
    display: flex;
    align-items: center;
    list-style: none;
    color: darkslategray;
    padding: 7px;
    margin-right: 10px;
    border-radius: 5px;
    cursor: pointer;
    font-size: 20px;
}
.components-tab > li:hover,
.components-tab > li[active] {
    text-decoration: underline;
}

.common-component-desc {
    margin-left: 10px;
    margin-bottom: 20px;
}

.common-component-content {
    padding-bottom: 50px;
}

.common-component-content > .sub-title {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 10px;
    margin-top: 10px;
}

.common-component-content > .sub-title > span {
    color: #579ffb;
    text-decoration: underline;
    margin-left: 10px;
}

.common-component-content > .props-tab {
    display: flex;
    margin: 0;
    padding: 0;
    margin-top: 40px;
    margin-bottom: 20px;
    flex-wrap: wrap;
    height: auto;
}
.common-component-content > .props-tab > li {
    display: flex;
    align-items: center;
    list-style: none;
    color: darkslategray;
    padding: 7px;
    margin-right: 10px;
    border-radius: 5px;
    cursor: pointer;
}
.common-component-content > .props-tab > li:hover,
.common-component-content > .props-tab > li[active] {
    text-decoration: underline;
}

.common-component-content > .sub-content {
    padding-bottom: 40px;
    max-height: 250px;
    overflow-y: scroll;
    border: solid 3px #bbb;
    padding: 0px 10px;
    border-radius: 10px;
}

.props-select-row {
    display: flex;
    align-items: center;
    margin-top: 2px;
    border-top: solid 1px #ddd;
    border-bottom: solid 1px #ddd;
    padding-bottom: 20px;
    padding-top: 20px;
}
.props-select-row > * {
    margin-right: 20px !important;
}
.props-select-row > p:first-of-type {
    min-width: 220px;
}
.props-select-row > p:first-of-type > span {
    color: #fff;
    background-color: darkslategray;
    opacity: 0.8;
    height: 40px;
    border-radius: 5px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0px 20px;
}
.props-select-row > p:last-of-type {
    width: fit-content;
    display: flex;
    flex-direction: column;
}
.props-select-row > p:last-of-type > span {
    margin-bottom: 10px;
}
</style>
