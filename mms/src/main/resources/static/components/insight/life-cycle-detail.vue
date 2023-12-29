<template>
    <div
            id="op-life-cycle-modal"
            class="modal fade"
            tabindex="-1"
            role="dialog"
            aria-labelledby="exampleModalLongTitle"
            aria-hidden="true"
    >
        <div class="modal-dialog modal-lg-m" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" v-text="resources['end_of_life_cycle']"></h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="element-container insight-detail">
                        <div class="element-header insight-header">
                            <div class="header-title" v-text="resources['insights_detail']"></div>
                            <div class="header-action">
                                <button class="btn btn-status" v-if="statusMapping[lifeCycle.status]">{{ statusMapping[lifeCycle.status] }}</button>
                            </div>
                        </div>
                        <div class="element-body insight-body">
                            <div class="insight-item">
                                <div class="item-title" v-text="resources['insights_description']"></div>
                               <div class="item-value"> <span
                                   v-text="overEndOfLifeCycle?
                                   resources['tooling_estimated_reach_max_over']:resources['tooling_estimated_reach_max']"></span>
                               {{lifeCycle.endLifeAtDate}}</div>
                            </div>
                            <div class="insight-item">
                                <div class="item-title" v-text="resources['insight_issue_date']"></div>
                                <div class="item-value">{{lifeCycle.createdDate}}</div>
                            </div>
                            <div class="insight-item">
                                <div class="item-title" v-text="resources['insight_priority']"></div>
                                <div class="item-value">{{priorityMapping[lifeCycle.priority]}}</div>
                            </div>
                            <div class="insight-item">
                                <div class="item-title" v-text="resources['insight_status']"></div>
                                <div class="item-value">{{statusMapping[lifeCycle.status]}}</div>
                            </div>
                        </div>
                    </div>
                    <div class="element-container dynamic-data">
                        <div class="element-header">
                            <div class="header-title" v-text="resources['dynamic_data']"></div>
                            <div class="header-action">
                                <div class="time-picker-wrapper start-date-wrapper">
                                    <div class="label-title" v-text="resources['start_date']"></div>
                                    <div class="input-group date" data-target-input="nearest">
                                        <input
                                                type="text"
                                                id="start-date"
                                                class="form-control datetimepicker-input"
                                                data-target="#start-date"
                                                readonly="readonly"
                                                required
                                        />
                                        <div
                                                class="input-group-append"
                                                data-target="#start-date"
                                                data-toggle="datetimepicker"
                                        >
                                            <div class="input-group-text">
                                                <i class="fa fa-calendar"></i>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="time-picker-wrapper end-date-wrapper">
                                    <div class="label-title" v-text="resources['end_date']"></div>
                                    <div class="input-group date" data-target-input="nearest">
                                        <input
                                                type="text"
                                                id="end-date"
                                                class="form-control datetimepicker-input"
                                                data-target="#end-date"
                                                readonly="readonly"
                                                required
                                        />
                                        <div
                                                class="input-group-append"
                                                data-target="#end-date"
                                                data-toggle="datetimepicker"
                                        >
                                            <div class="input-group-text">
                                                <i class="fa fa-calendar"></i>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                            </div>
                        </div>
                        <div class="element-body">
                            <canvas id="shot-chart"></canvas>
                        </div>
                    </div>
                    <div class="element-container communication-history">
                        <div class="element-header">
                            <div class="header-title" v-text="resources['communication_history']"></div>
                        </div>
                        <div class="element-body">
                            <div class="conversation" v-show="correspondences.length > 0">
                                <div class="qa-item" :class="{ question: item.isQuestion, answer: !item.isQuestion }" v-for="(item, index) in correspondences" :key="index">
                                    <div class="typo-picture">
                                        <div class="typo-content" v-if="item.isQuestion">Q</div>
                                        <div class="typo-content" v-else>A</div>
                                    </div>
                                    <div class="item-content">
                                        <div class="item-subject">
                                            <strong v-if="index === 0">[{{ item.user.name }} - {{ item.user.company.name }}]</strong>
                                            <strong v-else-if="item.user.email.endsWith('@emoldino.com')">[eMoldino Support Team]</strong>
                                            <strong v-else-if="item.isQuestion">[{{ item.user.name }} - {{ item.user.company.name }}]</strong>
                                            <strong v-else>[eMoldino Support Team]</strong>
                                            <span class="item-time">({{ item.createdDateTime }})</span>
                                            <img v-if="item.files.length > 0 || (recentlyCorrespondences[item.id] && recentlyCorrespondences[item.id].pendingFile)"
                                                 class="rotate-90 attach-icon" src="/images/icon/attach.svg" alt="attachment">
                                        </div>
                                        <div class="item-message">
                                            {{ item.message }}
                                        </div>
                                        <div class="item-file">
                                            <a :href="file.saveLocation" v-for="(file, fileIndex) in item.files" :key="fileIndex">{{ file.fileName }}</a>
                                            <a href="javascript:void(0)"
                                               v-if="hasUploadingFile(item) && item.files.length === 0"
                                               @click="checkingUploadingFile(recentlyCorrespondences[item.id])">
                                                {{ recentlyCorrespondences[item.id].pendingFile.fileLocation }}
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="submit-form">
                                <form  class="needs-validation" @submit.prevent="submit">
                                    <div class="row">
                                        <div class="col-md-12">
                                            <div class="message-form">
                                                <div class="card-body">
                                                    <div class="form-group row">
                                                        <label class="col-md-2 col-form-label align-items-center" for="message">
                                                         <span v-text="resources['message']"></span>
                                                         <span
                                                                class="avatar-status badge-danger"></span><span
                                                                class="badge-require"></span></label>
                                                        <div class="col-md-10">
                                                            <textarea id="message" rows="7" class="form-control" v-model="reply.message" required>{{reply.message}}</textarea>
                                                        </div>
                                                    </div>

                                                    <div class="form-group row">
                                                        <label class="col-md-2 col-form-label" v-text="resources['attachment']"></label>
                                                        <div class="col-md-10 op-upload-button-wrap">
                                                            <button type="button" class="btn btn-outline-success" v-text="resources['upload_file']"></button>
                                                            <input type="file" id="file" @change="selectedFile" />
                                                            <div>
                                                                <button
                                                                        style="margin-top: 2px;text-overflow: ellipsis; max-width: 200px; overflow: hidden;"
                                                                        v-for="(file, index) in files"
                                                                        class="btn btn-outline-dark btn-sm mr-1"
                                                                        @click.prevent="deleteFile(file, index)"
                                                                >{{file.name}}</button>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="row">
                                        <div class="col-lg-12">
                                            <div class="card">
                                                <div class="card-body text-center submit-card">
                                                    <button type="submit" class="btn btn-primary" v-text="resources['submit']"></button>
                                                    <button type="button" class="btn btn-default" @click="closeDetailModal" v-text="resources['cancel']"></button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </form>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>

    module.exports = {
        name: 'life-cycle-detail',
        props: {
                    resources: Object,
                },
        data() {
            return {
                lifeCycle: {},
                correspondences: [],
                files: [],
                reply: {
                    message: null
                },
                startDate: null,
                endDate: null,
                chart: null,
                recentlyCorrespondences: {},
                statusMapping: {
                    IN_COMMUNICATION: 'In Communication',
                    RESOLVE: 'Resolved',
                    DISMISS: 'Dismissed'
                },
                todayIndex: null,
                lastLabel: null,
                priorityMapping: {
                    LOW: 'Low',
                    MEDIUM: 'Medium',
                    HIGH: 'High'
                },
              overEndOfLifeCycle:false
            };
        },
        watch: {
            startDate(value) {
                if (value && this.endDate) {
                    this.filterDynamicData();
                }
            },
            endDate(value) {
                if (value && this.startDate) {
                    this.filterDynamicData();
                }
            }
        },
        mounted(){
            let self = this;
            $('#start-date').datetimepicker({
                'ignoreReadonly': true,
                'focusOnShow': true,
                'format': "YYYY-MM-DD",
            });
            $('#end-date').datetimepicker({
                'ignoreReadonly': true,
                'focusOnShow': true,
                'format': "YYYY-MM-DD",
            });
            $("#start-date").on("change.datetimepicker", function (e) {
                self.startDate = e.date.unix();
            });
            $("#end-date").on("change.datetimepicker", function (e) {
                self.endDate = e.date.unix();
            });
            this.setupDynamicDataChart();
        },
        methods: {
            show: function(data) {
                this.lifeCycle = data;
                this.resetForm();
                this.getCommunicationHistory();
                this.filterDynamicData();
                $("#op-life-cycle-modal").modal("show");
            },
            closeDetailModal: function() {
                $("#op-life-cycle-modal").modal("hide");
            },
            getCommunicationHistory: function() {
                if (!this.lifeCycle.topicId) {
                    this.correspondences = [];
                    return;
                }
                axios.get(`/api/topics/${this.lifeCycle.topicId}/correspondences`).then((response) => {
                    if (response.data && response.data.content) {
                        this.correspondences = response.data.content;
                    }
                }).catch(function (error) {
                    console.log(error.response);
                });
            },
            submit() {
                axios.post(
                    `/api/end-life-cycle/${this.lifeCycle.id}/create-correspondence`,
                    this.requestFormData(),
                    vm.multipartHeader()
                ).then((response) => {
                    if (response.data) {
                        this.lifeCycle.topicId = response.data.topicId;
                        let correspondence = response.data;

                        if (this.files.length > 0) {
                            correspondence.pendingFile = {
                                isUploading: true,
                                fileLocation: 'Uploading...'
                            }
                        }
                        this.recentlyCorrespondences[correspondence.id] = correspondence;
                        this.resetForm();
                        this.getCommunicationHistory();
                    }
                }).catch(function (error) {
                    console.log(error.response);
                });
            },
            selectedFile: function(e) {
                this.files = [];
                var files = e.target.files;

                if (files) {
                    var selectedFiles = Array.from(files);

                    for (var i = 0; i < selectedFiles.length; i++) {
                        this.files.push(selectedFiles[i]);
                    }
                }
            },

            deleteFile: function(f, index) {
                var newFiles = [];
                for (var i = 0; i < this.files.length; i++) {
                    var file = this.files[i];
                    if (i !== index) {
                        newFiles.push(file);
                    }
                }

                this.files = newFiles;
            },
            requestFormData: function() {
                var requestFormData = new FormData();
                for (var i = 0; i < this.files.length; i++) {
                    var file = this.files[i];
                    requestFormData.append("files", file);
                }
                requestFormData.append("payload", JSON.stringify(this.reply));
                return requestFormData;
            },
            resetForm: function() {
                // $('#start-date').val(moment().format("YYYY-MM-DD"));
                // $('#end-date').val(moment().format("YYYY-MM-DD"));
                this.files = [];
                this.reply.message = null;
                $('.needs-validation').removeClass('was-validated');
            },
            getTimestampOfItem(endOfLife) {
                let year = endOfLife.year;
                let month = endOfLife.month.replace(endOfLife.year, '');
                let day = endOfLife.title.replace(endOfLife.month, '');
                return moment().year(year).month(parseInt(month) - 1).date(day).hour(0).minute(0).second(0).unix();
            },
            filterDynamicData() {
                axios.get(`/api/end-life-cycle/${this.lifeCycle.id}/graph-mold-end-life-cycle-full`).then(response => {
                    if (response.data && response.data.moldEndLifeCycleChartDataList) {
                        this.chart.data.labels = [];
                        this.chart.data.datasets[0].data = [];
                        this.chart.data.datasets[1].data = [];
                        this.chart.options.annotation.annotations = [];
                        this.chart.update();
                        if (response.data.accumulatingShot === 0 && response.data.remainingShot === 0) {
                            return;
                        }

                        // let endTimeTimestamp = response.data.endLifeAt;
                        let data = response.data.moldEndLifeCycleChartDataList;
                        let endTimestamp = response.data.endLifeAt;
                        let totalCountPoint = 400;
                        let countLeftPoint = 80;
                        let totalPortion = 5;
                        let accumulatingShot = response.data.accumulatingShot;
                        let totalShot = response.data.remainingShot + response.data.accumulatingShot;
                        let currentTimestamp = moment().unix();
                        let startTimestamp = currentTimestamp;
                        let recentTimestamp = currentTimestamp;

                        if (endTimestamp > currentTimestamp) {
                          this.overEndOfLifeCycle=false;
                            if (data.length > 0) {
                                let startEndOfLife = data[0];
                                let startTimestamp = this.getTimestampOfItem(startEndOfLife);

                                let recentEndOfLife = data[data.length - 1];
                                recentTimestamp = this.getTimestampOfItem(recentEndOfLife);
                                // setup data until current time
                                let avgOfConsuming = Math.round((recentEndOfLife.accumulatingShot - startEndOfLife.accumulatingShot) / data.length);
                                for (let tempTimestamp = recentTimestamp + 86400; tempTimestamp < currentTimestamp; tempTimestamp += 86400) {
                                    data.push({
                                        title: moment.unix(tempTimestamp).format('YYYYMMDD'),
                                        month: moment.unix(tempTimestamp).format('YYYYMM'),
                                        year: moment.unix(tempTimestamp).format('YYYY'),
                                        accumulatingShot: data[data.length - 1].accumulatingShot + avgOfConsuming,
                                        remainingShot: data[data.length - 1].remainingShot - avgOfConsuming
                                    });
                                }
                                // recalculate data point
                                let currentStepPoint = data.length / countLeftPoint;
                                let computedData = [];
                                let reachLastIndex = false;
                                if (currentStepPoint > 1) {
                                    for (let i = 0; i < countLeftPoint; i++) {
                                        let index = Math.floor(currentStepPoint * i);
                                        computedData.push(data[index]);
                                        if (index === data.length - 1) {
                                            reachLastIndex = true;
                                        }
                                    }
                                    if (!reachLastIndex) {
                                        computedData[computedData.length - 1] = data[data.length - 1];
                                    }
                                } else {
                                    computedData = data;
                                }

                                data = computedData;
                                let k = (endTimestamp - currentTimestamp) / (currentTimestamp - startTimestamp);
                                if (k > (totalPortion - 1)) {
                                    if (computedData.length < countLeftPoint) {
                                        countLeftPoint = computedData.length;
                                    }
                                } else {
                                    countLeftPoint = Math.round(totalCountPoint / (k + 1));
                                }
                            } else {
                                countLeftPoint = 0;
                            }
                            this.todayIndex = data.length - 1;


                            let countRightPoint = totalCountPoint - countLeftPoint;
                            let distanceTime = endTimestamp - currentTimestamp;
                            let stepTimestamp = distanceTime / countRightPoint;

                            let stepShot = Math.round(response.data.remainingShot / countRightPoint);
                            let lastAccumulatingShot = response.data.accumulatingShot;
                            let lastRemainingShot = response.data.remainingShot;

                            if (data.length > 0) {
                                stepShot = Math.round(data[data.length - 1].remainingShot / countRightPoint);
                                lastAccumulatingShot = data[data.length - 1].accumulatingShot;
                                lastRemainingShot = data[data.length - 1].remainingShot;
                            }
                            for (let i = 1; i < countRightPoint; i++) {
                                let tmpTimestamp = currentTimestamp + i * stepTimestamp;
                                let accumulatingShot = lastAccumulatingShot + i * stepShot;
                                let remainingShot = lastRemainingShot - i * stepShot;
                                if (i === countRightPoint - 1) {
                                    accumulatingShot = totalShot;
                                    remainingShot = 0;
                                    tmpTimestamp = endTimestamp;
                                }

                                let title = moment.unix(tmpTimestamp).format('YYYYMMDD');
                                let month = moment.unix(tmpTimestamp).format('YYYYMM');
                                let year = moment.unix(tmpTimestamp).format('YYYY');
                                data.push({
                                    title: title,
                                    year: year,
                                    month: month,
                                    accumulatingShot: accumulatingShot,
                                    remainingShot: remainingShot
                                });
                            }
                        } else {
                          this.overEndOfLifeCycle = true;
                        }

                        this.bindDataForChart(data,endTimestamp);
                    }
                });
            },
            bindDataForChart(data, endTimestamp) {
                let labels = data.map(item => item.title);
                this.lastLabel = labels[labels.length - 1];
                for(let i = 0; i < labels.length / 20; i++) {
                    labels.push('');
                }
                let accumulatingData = data.map(item => item.accumulatingShot);
                let remainingData = data.map(item => item.remainingShot);
                this.chart.data.labels = labels;
                this.chart.data.datasets[0].data = accumulatingData;
                this.chart.data.datasets[1].data = remainingData;


                let today = moment().format('YYYYMMDD');
                let annotations = [];
                if(moment().unix()<endTimestamp){

                // if (labels.includes(today)) {
                annotations.push({
                    type: 'line',
                    id: 'vline' + 1,
                    mode: 'vertical',
                    scaleID: 'x-axis-0',
                    value: today,
                    borderColor: '#000',
                    borderWidth: 2,
                    label: {
                        enabled: true,
                        position: "top",
                        content: 'Today'
                    }
                });
                // }
                }

                annotations.push({
                    type: 'line',
                    id: 'vline' + 2,
                    mode: 'vertical',
                    scaleID: 'x-axis-0',
                    value: this.lastLabel,
                    borderColor: '#000',
                    borderWidth: 2,
                    label: {
                        enabled: true,
                        position: "top",
                        // content: 'Estimated Refurbishment Time ',
                        content: moment().unix()<endTimestamp?
                            'Estimated Refurbishment Time ':'Reached End of Life Cycle ',
                        xAdjust: 50
                    }
                });
                this.chart.options.annotation.annotations = annotations;
                this.chart.update();
            },
            setupDynamicDataChart() {
                let self = this;
                let chartConfig = {
                    type: 'line',
                    data: {
                        labels: [],
                        datasets: [
                            {
                                label: 'Accumulating Shot',
                                data: [],
                                borderColor: 'rgba(0, 119, 290, 0.6)',
                                backgroundColor: 'rgba(255,255,255, 0.1)',
                                lineTension: 0,
                                pointRadius: 0
                            },
                            {
                                label: 'Remaining Shot',
                                data: [],
                                borderColor: 'orange',
                                backgroundColor: 'rgba(255,255,255, 0.1)',
                                lineTension: 0,
                                pointRadius: 0
                            }
                        ]
                    },
                    options: {
                        legend: {
                            display: true,
                            position: "bottom"
                        },
                        scales: {
                            xAxes: [{
                                scaleLabel: {
                                    display: true,
                                    labelString: 'Date'
                                },
                                ticks: {
                                    autoSkip: false,
                                    callback: function(dataLabel, index) {
                                        if (this.chart.data.datasets[0].data.length - 1 === index) {
                                            return moment(self.lastLabel, 'YYYYMM').format('YYYY.MM');
                                        }else if (self.todayIndex === index) {
                                            return moment(dataLabel, 'YYYYMM').format('YYYY.MM');
                                        }
                                        if (index % 40 ||
                                            (self.todayIndex < index && index < self.todayIndex + 20) || (index < self.todayIndex && self.todayIndex < index + 20) ||
                                            index > this.chart.data.datasets[0].data.length - 20
                                        ) {
                                            return null;
                                        }

                                        return moment(dataLabel,'YYYYMMDD').format('YYYY.MM');
                                    },
                                }
                            }],
                            yAxes: [{
                                ticks: {
                                    beginAtZero: true,
                                    min: 0
                                },
                                scaleLabel: {
                                    labelString: 'Shot',
                                    display: true
                                }
                            }]
                        },
                        annotation: {
                            drawTime: 'afterDatasetsDraw',
                            annotations: []
                        }
                    }
                };

                this.chart = new Chart($('#shot-chart'), chartConfig);

            },
            hasUploadingFile(item){
                return this.recentlyCorrespondences[item.id] && this.recentlyCorrespondences[item.id].pendingFile && this.recentlyCorrespondences[item.id].pendingFile.isUploading;
            },
            checkingUploadingFile(correspondence) {
                axios.get(`/api/topics/correspondence/${correspondence.id}`).then(response => {
                    if (response.data) {
                        let serverCorrespondence = response.data;
                        let correspondenceItem = this.correspondences.filter(item => item.id === correspondence.id)[0];
                        if (serverCorrespondence.files.length > 0) {
                            correspondence.pendingFile.isUploading = false;
                            if (correspondenceItem) {
                                correspondenceItem.files = serverCorrespondence.files;
                            }
                        }
                    }
                });
            }
        }
    };
</script>
<style>
    #op-life-cycle-modal .modal-dialog {
        max-width: 1080px;
    }
    #op-life-cycle-modal .form-group label {
        margin-right: 0;
    }
    .cost-group {
        display: flex;
    }

    .cost-group .cost-value {
        width: 70%;
    }

    .cost-group .cost-unit {
        width: calc(100% - 15px);
        margin-left: 15px;
        max-width: 250px;
    }
    .currency-unit-label {
        margin-left: 25px;
        padding-top: 5px;
    }

    .actual-time-field {
        align-items: center;
    }
</style>
