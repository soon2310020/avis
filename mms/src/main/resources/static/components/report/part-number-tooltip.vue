<template>
    <a-tooltip placement="bottom">
      <span style="cursor: pointer;">
        <span class="report-item-hyper-link">{{getTitle}}</span>
        <span v-if="totalTitle == '' && data == 1">was produced in the selected period.</span>
        <!-- <span style="color: rgb(24, 144, 255)" href>
          {{ data }}
        </span> -->
      </span>
        <template slot="title">
            <div class="part-number-tooltip">
                <table>
                    <thead>
                    <tr>
                        <th>Part ID</th>
                        <th>Part Name</th>
                        <th>Total Production</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="part in parts">
                        <td>{{ part.partCode }}</td>
                        <td>{{ part.partName }}</td>
                        <td>{{ formatNumber(part.totalProduction) }}</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </template>
    </a-tooltip>
</template>

<script>
    module.exports = {
        props: {
            parts: Array,
            data: Number,
            totalTitle: String,
        },
        data() {
            return {};
        },
        methods: {
        },

        computed: {
            getTitle(){
                console.log('props', this.parts, this.data, this.totalTitle, this.totalTitle.length)
                if (this.totalTitle.length == 0) {
                   if (this.data > 1) {
                        return `${this.data} Parts produced in the selected period.`
                    } else if (this.data == 1){
                        let partName = this.parts[0].partName;
                        if (partName.length > 20) {
                            return partName.slice(0, 20) + '... ';
                        }
                        return partName;
                    }
                    return "";
                } else {
                    return this.totalTitle;
                }
            },
        },
        mounted() {

        }
    };
</script>

<style>
.ant-tooltip {
  max-width: 500px!important;
}
</style>
<style scoped>
    table tr:first-child td, th {
        border-top: 0;
        white-space: unset !important;
    }
</style>
