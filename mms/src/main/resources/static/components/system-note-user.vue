<template>
    <div class="system-note-user-filter" :id="id">
        <a-popover placement="bottom" trigger="click" v-model="isShowFirstLayer">
            <div slot="content">
                <div class="search-user-menu"  :class="{'view-user': viewMode === viewModes.USER}">
                    <div class="search-user-container" style="width: 270px">
                        <div class="search-box">
                            <a-input placeholder="Input search text" v-model.trim="searchText" @keypress="selectClosestUser">
                                <a-icon slot="prefix" type="search"/>
                            </a-input>
                        </div>
                        <ul v-if="viewMode === viewModes.COMPANY">
                            <li
                                    v-for="(companyCategory, companyCategoryIndex) in companyCategories" :key="companyCategoryIndex">
                                <div class="dropdown-item dropdown-submenu search-user-sub-menu" v-if="companyCategory.companyTypeText != null">
                                    <div class="company">
                                        <div class="company-title">
                                            {{ companyCategory.companyTypeText }}
                                        </div>
                                        <div class="dropdown-icon">
                                            <img src="/images/icon/next.svg" alt="next icon" />
                                        </div>
                                    </div>
                                    <div class='dropdown-menu'>
                                        <div class='dropdown-item'
                                             v-for="(company, companyIndex) in companyCategory.companies"
                                             :key="companyIndex"
                                             @click="selectCompany(companyCategoryIndex, companyIndex)"
                                        >
                                            {{ company.companyName }}
                                        </div>
                                    </div>
                                </div>
                            </li>
                        </ul>
                        <template v-if="viewMode === viewModes.USER">
                            <div class="btn-back" v-if="isFromCompany === true"><span @click="backToCompanyView()">Back</span></div>
                            <ul class="list-user">
                                <li v-for="(user, index) in userFiltered" :key="index" @click="selectUser(user)">
                                    <div class="dropdown-item mx-0 px-0 d-flex user-item-wrapper" :class="{closest: index === activeIndex}" @mouseover="activeIndex = index">
                                        <div class="d-flex align-items-center">
<!--                                            <img :src="user.gravatar" class="img-avatar mr-1" style="height: 40px; min-width: 40px" />-->
                                            <img src="/images/icon/user-avatar.png" class="img-avatar mr-1" style="height: 40px; min-width: 40px" />
                                        </div>
                                        <div class="ml-1">
                                            <p class="py-0 my-0" style="font-size: 17px; font-weight: 500; text-overflow: ellipsis; width: 200px;white-space: nowrap;overflow: hidden;">{{ user.name }}</p>
                                            <span style="font-size: 13px; display: inline-block; text-overflow: ellipsis; width: 200px;white-space: nowrap;overflow: hidden !important;">{{user.email}}</span>
                                        </div>
                                    </div>
                                </li>
                            </ul>
                        </template>
                    </div>
                </div>
            </div>
        </a-popover>
    </div>
</template>

<script>
    module.exports = {
        props: {
            id: String,
            resources: Object,
        },
        data() {
            return {
                activeIndex: 0,
                isShow: false,
                users: [],
                companyCategories: [],
                searchText: '',
                viewModes: {
                    USER: 'USER',
                    COMPANY: 'COMPANY',
                    dialogBack: false
                },
                viewMode: 'COMPANY',
                companyCategoryIndex: 0,
                companyIndex: 0,
                isFromCompany: false,
                textFromTextarea: null,
                isFromTextarea: false,
                isShowFirstLayer: false,
            }
        },
        methods: {
            showSearchUserPopup(text, e){
                if (!text) {
                    this.viewMode = this.viewModes.COMPANY;
                }else {
                    this.viewMode = this.viewModes.USER;
                    this.isFromTextarea = true;
                }
                this.textFromTextarea = text;
                this.isShow = true;
                let childrenLengthOfContent = $('#' + this.id).parent().find('.system-note-shadow').find('div').length;
                let offsetTop = childrenLengthOfContent*20 + 20;
                if (offsetTop > e.target.offsetHeight) {
                    offsetTop = e.target.offsetHeight;
                }
                $('#' + this.id).css('top', offsetTop + 'px');
                this.isShowFirstLayer = true;
              setTimeout(() => {
                $('.ant-popover').each((i, e) => {
                  if (e.querySelector('.search-user-menu')) {
                    e.style.zIndex = '10000';
                  }
                });
              }, 100);
            },
            hideSearchUserPopup(){
                this.viewMode = this.viewModes.COMPANY;
                this.isFromCompany = false;
                this.companyCategoryIndex = 0;
                this.companyIndex = 0;
                this.searchText = null;
                this.isShow = false;
                this.isShowFirstLayer = false;
            },
            getListUser(){
                Common.getLiteListUser().then(users => {
                   this.users = users;
                    this.users.forEach(user => {
                        let companyCategory = this.companyCategories.filter(item => item.companyType === user.companyType)[0];
                        if (!companyCategory) {
                            companyCategory = {
                                companyType: user.companyType,
                                companyTypeText: user.companyTypeText,
                            };
                            this.companyCategories.push(companyCategory);
                        }
                        if (!companyCategory.companies) {
                            companyCategory.companies = [];
                        }
                        let company = companyCategory.companies.filter(item => item.companyName === user.companyName)[0];
                        if (!company) {
                            company = {
                                companyName: user.companyName,
                                companyId: user.companyId,
                            };
                            companyCategory.companies.push(company);
                        }
                        if (!company.users) {
                            company.users = [];
                        }
                        company.users.push(user);
                    });
                    this.companyCategories.sort((first, second) => {
                        if (!first.companyTypeText || !second.companyTypeText) {
                            return 1;
                        }
                        let firstName = first.companyTypeText.toUpperCase();
                        let secondName = second.companyTypeText.toUpperCase();
                        return firstName > secondName? 1: -1;
                    });
                });
            },
            backToCompanyView(){
                this.viewMode = this.viewModes.COMPANY;
                this.searchText = null;
            },
            selectCompany(companyCategoryIndex, companyIndex) {
                this.companyIndex = companyIndex;
                this.companyCategoryIndex = companyCategoryIndex;
                this.viewMode = this.viewModes.USER;
                this.isFromCompany = true;
            },
            selectUser(user) {
                this.isShow = false;
                this.$emit('select-user',user);
                this.isShowFirstLayer = false;
                this.searchText = null
            },
            findClosestUser() {
                if (this.textFromTextarea) {
                    this.isShowFirstLayer = false;
                    this.searchText = null;
                    if (this.userFiltered.length > 0) {
                        this.$emit('select-user',this.userFiltered[0], true);
                    }
                } else {
                    this.hideSearchUserPopup();
                }
            },
            selectClosestUser(e) {
                if (e.code === 'Enter' && this.viewMode === this.viewModes.USER && this.userFiltered.length > 0) {
                    this.isShowFirstLayer = false;
                    this.$emit('select-user',this.userFiltered[0]);
                    setTimeout(() => {
                        this.searchText = null;
                    }, 10);
                }
            }
        },
        mounted() {

            this.getListUser();
        },
        watch: {
            searchText(value) {
                // if (!value) {
                //     return;
                // }
                if (this.isFromCompany) {
                    this.isFromCompany = false;
                }
                this.isFromTextarea = false;
                (!value) ? (this.viewMode = this.viewModes.COMPANY) : (this.viewMode = this.viewModes.USER)
                
                // if (this.viewMode !== this.viewModes.USER){
                //     this.viewMode = this.viewModes.USER;
                // }
            },
        },
        computed: {
            userFiltered() {
                if (this.isFromCompany) {
                    return this.companyCategories[this.companyCategoryIndex].companies[this.companyIndex].users;
                }
                if (this.searchText) {
                    return this.users.filter(user => user.name.toUpperCase().includes(this.searchText.toUpperCase()));
                }
                if (this.textFromTextarea) {
                    return this.users.filter(user => user.name.toUpperCase().includes(this.textFromTextarea.toUpperCase()));
                }
                return this.users;
            }
        }
    }
</script>

<style scoped>
    .search-user-menu.view-user .list-user li .dropdown-item {
        padding: 0.5rem 0.25rem;
    }
    .system-note-user-filter {
        position: absolute;
    }
    .user-item-wrapper:hover {
        color: #000;
    }
</style>