<template>
    <div class="delivery-summary-item">
        <div class="summary-content">
            <div class="summary-icon">
                <img :src="icon" alt="delivery-summary-icon" />
            </div>
            <div class="summary-value">
                {{ title }}: {{ value }}
            </div>
        </div>
    </div>
</template>

<script>
    module.exports = {
        props: {
            icon: String,
            title: String,
            value: [String, Number],
            resources: Object
        },
        data() {
            return {

            };
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
</style>
