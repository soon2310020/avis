<template>
    <div>
        <a-modal
            :class="'ant-modal-custom'"
            v-model="visible"
            :body-style="{ padding: '10',borderRadius:'unset !important' }"
            width="815px"
            :closable="false"
            :footer="null"
            centered
        >
            <div class="d-modal-header">
                <span class="modal-title">My Completion Orders</span>
                <div
                    class="data-close-button close-button"
                    @click="visible = false"
                    aria-hidden="true">
                    <span class="icon-close-black"></span>
                </div>
            </div>
            <div class="d-modal-body">
            </div>

        </a-modal>
    </div>

</template>
<script>
module.exports = {
    props: {
        resources: Object,
    },
    components: {},
    data() {
        return {
            visible: false,
        }
    },
    methods: {
        showCompletionOrder(object) {
            this.visible = true;
        }
    },
    created() {

    },
    mounted() {

    }
}


</script>
<style scoped>
@import '/css/data-modal-style.css';
</style>