<template>
    <div class="modal fade" id="progress-modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true" style="top: 30%;">
        <div class="modal-dialog" role="document" style="width: 350px">
            <div class="modal-content" style="border: none">
                <div class="modal-header">
                    <h5 class="modal-title" id="exampleModalLabel" style="font-weight: bold" v-text="resources['exporting_report']"></h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close" @click="stop">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body" style="height: 100px;">
                    <div style="text-align: center">
                        <p style="color: #615EA9;" v-text="resources['loading']">...</p>
                    </div>
                    <div id="myProgress">
                        <div id="myBar" style="display: inline-block"></div>
                        <div style="display: inline-block; width: 24px; height: 24px; background: #615EA9; margin-left: -16px; border-radius: 50%;"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    module.exports = {
        props: {
            stop: Function,
            progressStatus: Object,
            resources: Object
        },
        data() {
            return {
                currentIntervalIds: []
            };
        },
        methods: {
            progressBarRun: function () {
                let progressModal = $('#progress-modal');
                progressModal.modal('show');
                this.currentIntervalIds.forEach(id => {
                    clearInterval(id);
                });
                let self = this;
                let flag = 0;
                if (flag === 0) {
                    flag = 1;
                    var elem = document.getElementById("myBar");
                    elem.style.width = "0%";
                    var width = 1;
                    var id = setInterval(frame, 40);

                    self.currentIntervalIds = [];
                    self.currentIntervalIds.push(id);

                    function frame() {
                        if (width >= 100) {
                            clearInterval(id);
                            flag = 0;
                            progressModal.modal('hide');
                        } else {
                            if (self.progressStatus.isStoppedExport) {
                                progressModal.modal('hide');
                                self.progressStatus.currentIntervalIds.forEach(id => {
                                    clearInterval(id);
                                });
                            }
                            if(self.progressStatus.isCallingDone) {
                                width = width + 2;
                            }
                            if(width < 30) {
                                width = width + 0.5;
                            } else if(width >= 30 && width < 50) {
                                width = width + 0.3;
                            } else if(width >= 50 && width < 75) {
                                width = width + 0.1;
                            } else if(width >=75 && width < 85) {
                                width = width + 0.05;
                            } else if(width >=85 && width < 99.5) {
                                width = width + 0.01;
                            }
                            elem.style.width = width + "%";
                        }
                    }
                }
            },
        }
    };
</script>
<style scoped>
    #myProgress {
        width: 80%;
        background-color: white;
        height: 32px;
        border: 1px solid #615EA9;
        border-radius: 24px;
        margin: 0 auto;
        margin-top: 10px;
    }

    #myBar {
        border-top-left-radius: 24px;
        border-bottom-left-radius: 24px;
        width: 1%;
        height: 24px;
        background-color: #263163;
        max-width: 92%;
        margin-top: 3px;
        margin-left: 4px;
    }
</style>
