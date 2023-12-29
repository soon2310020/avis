<template>
    <div class="object-filter">
        <a-popover placement="bottom" trigger="click" v-model="visible">
            <a-button style="margin-right: 10px" class="dropdown_button">
                {{ titleShow }}
                <a-icon type="caret-down"/>
            </a-button>
            <div slot="content">
                <div style="max-height: 250px; overflow-y: auto;">
                    <a-menu style="border-right: none">
                        <a-menu-item v-for="(systemNoteFunction, index) in systemNoteFunctions" @click="selectTopic(systemNoteFunction)" :key="index" class="topic-menu-item">
                            {{ systemNoteFunction.name }}
                        </a-menu-item>
                    </a-menu>
                </div>
            </div>
        </a-popover>
    </div>
</template>

<script>
    module.exports = {
        props: {
        },
        data() {
            return {
                visible: false,
                selectedTopic: '',
                systemNoteFunctions: []
            };
        },
        methods: {
            getListUserTopic() {
                axios.get('/api/topics/tags-topic-of-user').then(response => {
                    this.systemNoteFunctions = response.data.dataList;
                    this.initDefaultValue();
                }).catch(error => {
                    console.log('error tags-topic-of-user', error);
                });
            },
            selectTopic(systemNoteFunction) {
                this.$emit('selected-value', systemNoteFunction.code);
                this.selectedTopic = systemNoteFunction;
                this.visible = false;
            },
            initDefaultValue(){
                let initSystemNoteFunction = Common.getParameter('system-note-function');
                if (initSystemNoteFunction) {
                    let systemNoteFunction = this.systemNoteFunctions.filter(item => item.code === initSystemNoteFunction)[0];
                    if (systemNoteFunction) {
                        this.selectTopic(systemNoteFunction)
                    }
                }
            }
        },

        computed: {
            titleShow: function () {
                if (this.selectedTopic) {
                    return this.selectedTopic.name;
                }
                return 'Select Topic';
            }
        },
        watch: {
        },
        mounted() {
            this.getListUserTopic();
        }
    };
</script>

<style scoped>
    .first-layer-wrapper {
        position: relative;
    }

    .first-layer {
        position: absolute;
        top: 0;
    }
</style>
