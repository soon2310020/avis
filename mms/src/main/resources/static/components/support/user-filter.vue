<template>
    <div class="user-filter" v-show="visible">
        <div class="input-wrapper">
            <a-input style="margin-bottom: 10px;" placeholder="Input search text" v-model.trim="searchText">
                <a-icon slot="prefix" type="search"/>
            </a-input>
        </div>
        <div style="max-height: 250px; overflow-y: auto;" class="content-body">
            <div v-for="item in userFiltered" class="user-item">
                <a-col :key="item.id" @click="selectedUser(item.id)" >
                    <p-radio  v-model="checkedUser" :value="item.id">{{item.name}}</p-radio>
                </a-col>
            </div>
        </div>
    </div>
</template>

<script>
    module.exports = {
        props: {
        },
        data() {
            return {
                checkedListUser: [],
                searchText: "",
                visible: false,
                checkedUser: null,
                users: []
            };
        },
        methods: {
            showUserFilterPopup(){
                this.visible = true;
            },
            getListUser(){
                Common.getLiteListUser().then(users => {
                    this.users = users;
                });
            },
            hideUserFilterPopup(){
                this.visible = false;
            },
            selectedUser(value) {
                this.visible = false;
                let resList = this.users.filter(u => u.id === value);
                this.checkedUser=resList != null && resList.length > 0 ? resList[0] : null;
                this.$emit('recipient', this.checkedUser);
            },
        },
        watch: {
        },
        computed: {
            userFiltered: function () {
                if (!this.searchText) {
                    return this.users;
                }
                return this.users.filter(item => item.name.toUpperCase().includes(this.searchText.toUpperCase()));
            }
        },
        mounted() {
            this.getListUser();
        }
    };
</script>

<style scoped>
    .badge-require {
        display: none;
    }
</style>
