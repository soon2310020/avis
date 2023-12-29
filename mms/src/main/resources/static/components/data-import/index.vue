<template>
    <div class="data-import-page" v-if="permissions">
        <section class="section-steps">
            <base-steps :list="listSteps" :current="currentStep" @change="step => handleChangeStep(step.key)"></base-steps>
        </section>
        <section class="section-main">
            <step-select-type v-show="currentStep === KEY_STEP.TYPE" :resources="resources" :permissions="permissions" :list-selected="selectedTypes" @next="handleNextStep(KEY_STEP.UPLOAD, $event)"></step-select-type>
            <step-upload v-show="currentStep === KEY_STEP.UPLOAD" ref="step-upload" :resources="resources" @next="handleNextStep(KEY_STEP.RESULT, $event)" @prev="handleBackStep(KEY_STEP.TYPE, $event)"></step-upload>
            <step-show-result v-show="currentStep === KEY_STEP.RESULT" :resources="resources" :overview="result.overview" :list-validate-results="result.validate" :is-processing="isProcessing" :is-overwrite="isOverwriteExistingData" @reset="handleResetImport" @prev="handleBackStep(KEY_STEP.UPLOAD)"></step-show-result>
        </section>
    </div>
</template>

<script>
const KEY_STEP = {
    TYPE: 'TYPE',
    UPLOAD: 'UPLOAD',
    RESULT: 'RESULT'
}

module.exports = {
    name: 'DataImport',
    components: {
        'step-select-type': httpVueLoader('./step-select-type/index.vue'),
        'step-upload': httpVueLoader('./step-upload/index.vue'),
        'step-show-result': httpVueLoader('./step-show-result/index.vue')
    },
    setup(props, { refs }) {
        const DEFAULT_STEPS = ref([]) // for constant
        
        // const resources = computed(() => headerVm?.resourcesFake || {})
        const resources = ref({})
        const permissions = computed(() => headerVm?.permissions?.[Common.PERMISSION_CODE.DATA_IMPORT]?.children)
        const selectedTypes = ref([])
        const selectedFiles = ref([])
        const isOverwriteExistingData = ref(false)
        const result = reactive({
            overview: {},
            validate: []
        })

        const listSteps = ref([])
        const currentStep = ref(KEY_STEP.TYPE)
        const currentIndex = computed(() => listSteps.value.findIndex(s => s.key === currentStep.value))
        const isFirstStep = computed(() => currentIndex.value <= 0)
        const isLastStep = computed(() => currentIndex.value > listSteps.value.length - 1)


        const isProcessing = ref(false)
        const fetchImportResult = async () => {
            isProcessing.value = true
            try {
                const payload = {}
                payload.fileRefs = selectedFiles.value.map(i => ({ id: i.id, name: i.name }))
                payload.overwriteExistingData = isOverwriteExistingData.value
                payload.resourceType = selectedTypes.value.map(i => i.value)
                const response = await axios.post('/api/common/dat-imp', payload)                
                result.overview = response.data                
                result.validate = response.data.fileResults.map(item => {
                    const found = selectedFiles.value.findIndex(fileObj => fileObj.id === item.fileId)
                    console.log('found', found)
                    if (found > -1) return {
                        size: selectedFiles.value[found].size,
                        name: selectedFiles.value[found].name,
                        type: selectedFiles.value[found].type,
                        ...item
                    }
                    return { ...item, id: item.fileId }
                })

            } catch (error) {
                console.log(error)
            } finally {
                isProcessing.value = false
            }
        }

        const __refetchFiles = () => {
            refs['step-upload'].listFiles = []
            refs['step-upload'].listFilesCloned.forEach((file, index) => {
                refs['step-upload'].fetchUploadFiles(file, index)
            })
        }

        const handleChangeStep = (keyStep) => {
            const targetStepIndex = listSteps.value.findIndex(i => i.key === keyStep)
            if (currentStep.value === KEY_STEP.RESULT && keyStep === KEY_STEP.UPLOAD) __refetchFiles() // if navigate from result -> refetch files
            if (currentStep.value === KEY_STEP.RESULT && keyStep === KEY_STEP.TYPE) handleResetImport()
            console.log('targetStepIndex', targetStepIndex)
            console.log('currentStep.value', currentStep.value)
            currentStep.value = keyStep
        }
        const handleBackStep = (targetStep, payload) => {
            listSteps.value.splice(currentIndex.value, 1, { ...listSteps.value[currentIndex.value], checked: false })
            currentStep.value = listSteps.value[currentIndex.value - 1].key
            if (targetStep === KEY_STEP.TYPE) {
                console.log('@back->type', payload)
            }
            if (targetStep === KEY_STEP.UPLOAD) {
                console.log('@back->upload', payload)
                console.log(`refs['step-upload']`, refs['step-upload'])
                __refetchFiles()
            }
        }

        const handleNextStep = (targetStep, payload) => {
            listSteps.value.splice(currentIndex.value, 1, { ...listSteps.value[currentIndex.value], checked: true })
            currentStep.value = listSteps.value[currentIndex.value + 1].key
            if (targetStep === KEY_STEP.UPLOAD) {
                console.log('@next->upload', payload)
                selectedTypes.value = payload
            }
            if (targetStep === KEY_STEP.RESULT) {
                console.log('@next->result', payload)
                selectedFiles.value = payload.listFiles
                isOverwriteExistingData.value = payload.shouldOverwriting
                fetchImportResult()
            }
        }

        const handleResetImport = () => {
            listSteps.value = [...DEFAULT_STEPS.value]
            currentStep.value = KEY_STEP.TYPE
            selectedTypes.value = []
            selectedFiles.value = []
            isOverwriteExistingData.value = false
            result.overview = null;
            result.validate = []
            refs['step-upload'].cleanState()
        }

        watch(() => headerVm?.resourcesFake, newVal => {
            if (newVal) {
                resources.value = Object.assign({}, resources.value, newVal)
                DEFAULT_STEPS.value = [
                    { key: KEY_STEP.TYPE, label: newVal['type']?.capitalize(), checked: false },
                    { key: KEY_STEP.UPLOAD, label: newVal['upload']?.capitalize(), checked: false },
                    { key: KEY_STEP.RESULT, label: newVal['result']?.capitalize(), checked: false },
                ]
                listSteps.value = [...DEFAULT_STEPS.value]
            }
        }, { immediate: true })

        return {
            KEY_STEP,
            DEFAULT_STEPS,
            isOverwriteExistingData,

            resources,
            permissions,

            listSteps,
            currentStep,
            isFirstStep,
            isLastStep,

            selectedTypes,

            handleChangeStep,
            handleBackStep,
            handleNextStep,
            handleResetImport,

            result,
            isProcessing
        }
    }
}
</script>

<style scoped>
.data-import-page {}

.section-steps {
    display: flex;
    justify-content: center;
    margin: 40px 0;
}

.section-main {}

.action-bar {}
</style>
