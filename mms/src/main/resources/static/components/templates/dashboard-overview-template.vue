<template>
    <div class="dashboard-overview-template">
        <div class="dashboard-overview__header d-flex justify-content-between mb-3">
            <div class="header-filter">
                <slot name="filter"></slot>
            </div>
            <div class="header-settings">
                <slot name="setting"></slot>
            </div>
        </div>

        <div class="dashboard-overview__content">
            <slot name="content"></slot>
        </div>

        <slot name="meta"></slot>
    </div>
</template>

<script>
module.exports = {
    name: 'DashboardOverviewTemplate',
}
</script>

<style scoepd>
/* layout */
.dashboard-overview-template {}

.dashboard-overview-template .dashboard-overview__header {}

.dashboard-overview-template .dashboard-overview__content {}

/* header */
.dashboard-overview__header .header-filter {}
.dashboard-overview__header .header-settings {}

/* content */
</style>