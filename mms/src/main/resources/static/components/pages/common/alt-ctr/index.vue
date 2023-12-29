<template>
    <center-template resource-id="CM9030" :resources="resources" :permission="permission">
        <template v-slot:header> {{ permission.name }} </template>
        <template v-slot:tabs>
            <base-card-tab-list :list-tabs="listTabs" :current-tab="currentTab" size="small" @change="tab => handleChangeTab(tab)"></base-card-tab-list>
        </template>
        <template v-slot:content>
            <component :is="currentTab" :resources="resources" :current-tab-data="currentTabData"></component>
        </template>
    </center-template>
</template>

<script>
const getHash = (itemUrl) => itemUrl.split('#')[1];
module.exports = {
    name: 'AlertCenterPage',
    components: {
        'center-template': httpVueLoader('/components/templates/center-template.vue'),
    },
    setup() {
        const resourceId = 'CM9030' // key for alert center
        
        const resources = ref(null)
        const permission = ref(null)

        const listTabs = computed(() => {
            return Object.keys(permission.value.children).map(key => ({
                url: permission.value?.children?.[key]?.url,
                key: getHash(permission.value?.children?.[key]?.url),
                title: permission.value?.children?.[key]?.name,
                iconSrc: permission.value?.children?.[key]?.icon,
                permission: permission.value?.children?.[key]?.children, // children permission

                // identical
                menuId: resourceId,
                submenuId: key,
                itemPermissions: permission.value?.children?.[key]?.items // feature enabled
            }))
        })
        const currentTab = ref('')
        const currentTabData = computed(() => listTabs.value.filter(i => i.key === currentTab.value)?.[0]) // tracking selected tab

        const handleChangeTab = (tab) => {
            console.log(tab)
            const tabKey = tab ? tab.key : listTabs.value[0].key // default tab
            currentTab.value = tabKey
            window.location.hash = `#${tabKey}`
        }

        const getResources = async () => {
            try {
                const messages = await Common.getSystem('messages')
                resources.value = JSON.parse(messages)
            } catch (error) {
                console.log(error)
            }
        }

        const getPermission = async () => {
            try {
                permission.value = await Common.getPermissionList(resourceId)
                const hash = getHash(window.location.hash)
                handleChangeTab(hash)
            } catch (error) {
                console.log(error)
            }
        }

        // 
        getPermission()
        getResources()

        watchEffect(() => console.log('currentTab', currentTab.value))

        return {
            resources,
            permission,

            listTabs,
            currentTab,
            currentTabData,
            handleChangeTab
        }
    }
};
</script>
