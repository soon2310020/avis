<template>
    <div class="delivery-table" id="delivery-table">
        <div class="table-wrapper">
            <table class="table table-responsive-sm table-striped">
                <thead>
                <tr class="tr-sort">
                    <th class="text-left">
                        <span v-text="resources['supplier']"></span>
                    </th>
                    <th>
                        <span v-text="resources['weekly_demand']"></span>
                    </th>
                    <th>
                      <span v-text="resources['weekly_output']"></span>
                    </th>
                    <th>
                      <span v-text="resources['remaining_capacity']"></span>
                    </th>
                    <th>
                      <span v-text="resources['weekly_otd']"></span>
                    </th>
                    <th>
                      <span v-text="resources['otd_status']"></span>
                    </th>
                </tr>
                </thead>
                <tbody class="op-list" style="display:none">
                <tr v-for="(result, index, id) in results" :id="result.id">
                    <td class="text-left">{{ result.companyName }}</td>
                    <td>{{ formatNumber(result.weeklyDemand) }}</td>
                    <td>
                        <div class="otd-detail-wrapper" @click="openOtdDetail($event, result, index)" v-click-outside="hideOtdDetail">
                            <a href="javascript:void(0)">{{ formatNumber(result.weeklyOutput) }}</a>
                            <div class="delivery-detail">
                                <div class="detail-content" :id="'delivery-detail-' + index"></div>
                            </div>
                        </div>
                    </td>
                    <td>{{ formatNumber(result.remainingCapacity) }}</td>
                    <td>{{ result.weeklyOTD + '%' }}</td>
                    <td>
                        <label v-if="result.otdStatus === statusType.LOW" class="label label-success" v-text="resources['low_risk']"></label>
                        <label v-else class="label label-danger" v-text="resources['high_risk']"></label>
                    </td>
                </tr>
                </tbody>
            </table>

            <div class="no-results d-none" v-text="resources['no_results']"></div>
        </div>
        <delivery-table-detail :resource="resources" :request-param="requestParam"></delivery-table-detail>
    </div>
</template>

<script>
    module.exports = {
        props: {
            resources: Object,
            requestParam: Object
        },
        components: {
            "delivery-table-detail": httpVueLoader("/components/chart-new/delivery/delivery-table-detail.vue"),
        },
        data() {
            return {
                results: [],
                pagination: [],
                statusType: {
                    LOW: 'LOW',
                    HIGH: 'HIGH'
                },
                detailIndex:null
            };
        },
        methods: {
            formatWeeklyDemand(weeklyDemand) {
              if (weeklyDemand < 1000) {
                  return weeklyDemand;
              }
              return weeklyDemand/1000 + 'K';
            },
            setTableData(data) {
                this.results = data.details;
                this.pagination = Common.getPagingData(data);
                Common.handleNoResults('#delivery-table', this.results.length);
            },
            formatNumber(number) {
                if (!number)
                    return number;
                return Common.formatNumber(number);
            },
            async openOtdDetail(e, data, index) {
                let opList = $('.op-list-child');
                opList.hide();
                $('.delivery-detail').hide();
                if (e.target.localName !== 'a')
                    return;
                let child = await Common.vue.getChild(this.$children, 'delivery-table-detail');
                if (child != null) {
                    await child.showDeliveryDetail(data.companyId, 'delivery-detail-' + index);
                }
                this.detailIndex = index;
                let element = $('#delivery-detail-' + index).parent();
                if(element.is(":visible")){
                    element.hide();
                    return;
                }
                $(e.target).parent().find('.delivery-detail').css({
                    "left": (e.clientX - 65) + 'px',
                    "top": (e.clientY - e.layerY + 35) + 'px',
                });
                element.show();
                opList.show();
            },
            hideOtdDetail(e) {
                if (e.target.localName !== 'a') {
                    $('#delivery-detail-' + this.detailIndex).parent().hide();
                }
            }

        },

        computed: {
        },
        watch: {

        },
        mounted() {
        }
    };
</script>

<style scoped>
    .delivery-summary-item {
        width: calc(33% - 5px);
        display: flex;
        justify-content: center;
        align-items: center;
        background-color: #FDCF72;
        border: 1px solid #CDCDCD;
        padding: 15px 5px;
    }
    .summary-content {
        display: flex;
        justify-content: center;
        align-items: center;
    }
    .summary-icon {
        margin-right: 1rem;
    }
    .summary-icon img {
        width: 30px;
    }
    .summary-value {
        font-weight: 600;
    }
    #delivery-table .table-wrapper {
        max-height: 300px;
        overflow-y: auto
    }

    .otd-detail-wrapper {
        position: relative;
    }

    .delivery-detail {
        position: fixed;
        background: rgba(0, 0,0, 0.6);
        color: #FFF;
        top: 30px;
        display: none;
        border-radius: 5px;
        z-index: 1;

    }
    .delivery-detail::after {
        content: "";
        position: absolute;
        bottom: 100%;
        border-width: 8px;
        left: 60px;
        border-style: solid;
        border-color: rgba(0,0,0,0.6) transparent transparent transparent;
        transform: rotate(-180deg);
    }
    .delivery-detail .detail-content {
        max-height: 300px;
        overflow-y: auto;
    }

    .delivery-detail .table {
        background: transparent;
    }
    .delivery-detail .table th {
        width: 120px;
    }
</style>
