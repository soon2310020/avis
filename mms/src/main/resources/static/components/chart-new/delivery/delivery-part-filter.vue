<template>
    <div class="part-filter">
        <a-popover v-model="visible" placement="bottom" trigger="click">
            <a :id="'part-filter-' + id" href="javascript:void(0)" @click="showAnimation()"
                style="margin-right: 10px; width: 110px" class="dropdown_button-custom">
                <span class="truncate" :class="{ 'selected-dropdown-text': visible }">{{ titleShow }}</span>
                <div>
                    <a-icon class="caret" :class="{ 'selected-dropdown-text': visible, 'caret-show': displayCaret }"
                        type="caret-down" />
                </div>

            </a>
            <div slot="content" style="padding: 8px 6px;" class="dropdown-scroll">
                <div class="content-body">
                    <template>
                        <a-input style="margin-bottom: 10px;" placeholder="Input search text" v-model.trim="partInput">
                            <a-icon slot="prefix" type="search" />
                        </a-input>
                        <div style="max-height: 250px; overflow-y: auto;">
                            <template v-for="item in partFiltered" :key="item.id">
                                <a-col style="padding: 7px;">
                                    <p-radio @change="displayCaret = true" v-model="checkedPart"
                                        :value="item.id">{{ item.name }}</p-radio>
                                </a-col>
                            </template>
                        </div>
                    </template>
                </div>
            </div>
        </a-popover>
    </div>
</template>

<script>
module.exports = {
    props: {
        optionArrayPart: Array,
        changePart: Function,
        frequent: String,
        resources: Object,
        defaultValue: Object,
        id: String,
        defaultPartId: String
    },
    data() {
        return {
            // optionArrayPart: [],
            checkedListPart: [],
            oldCheckedListPart: [],
            partInput: "",
            clicked: false,
            checkedPart: null,
            visible: false,
            displayCaret: false,
            caretTimeout: null,
            animationTimeout: null,
            allPart: {
                id: 'null',
                name: 'dashboard_all_parts'
            },
            isLoadedData: false
        };
    },
    methods: {
        selectMenu(menu) {
            this.visible = true;
            this.selectedMenu = menu;
        },
        isSelectedMenu(menu) {
            return this.selectedMenu === menu;
        },
        checkPart(item) {
            const findIndex = this.checkedListPart.findIndex(
                value => value.id == item.id
            );
            return findIndex !== -1;
        },
        showAnimation() {
            const el = document.getElementById('part-filter-' + this.id)
            if (!this.visible) {
                setTimeout(() => {
                    el.classList.add("dropdown_button-animation");
                    this.animationTimeout = setTimeout(() => {
                        el.classList.remove("dropdown_button-animation");
                        el.classList.add("selected-dropdown-text");
                        el.classList.add("selected-dropdown-button");
                    }, 1600);
                }, 1);

            } else {
                this.closeAnimation()
            }
        },
        closeAnimation() {
            const el = document.getElementById('part-filter-' + this.id)
            if (this.animationTimeout != null) {
                console.log("Here");
                console.log(this.animationTimeout);
                clearTimeout(this.animationTimeout);
                this.animationTimeout = null;
            }
            if (this.caretTimeout != null) {
                clearTimeout(this.caretTimeout);
                this.caretTimeout = null;
            }
            el.classList.remove("dropdown_button-animation");
            el.classList.remove("selected-dropdown-text");
            el.classList.remove("selected-dropdown-button");
            this.displayCaret = false
        },
        setDefaultFilter(isWatchPartList = false) {
            if (this.optionArrayPart.length > 0 && (!this.isLoadedData || isWatchPartList) && this.defaultPartId) {
                this.isLoadedData = true;
                const parts = this.optionArrayPart.filter(partData => partData.id === +this.defaultPartId);
                if (parts.length > 0) {
                    this.checkedPart = parts[0].id;
                    this.checkedListPart = [parts[0]];
                } else {
                    this.checkedPart = this.optionArrayPart[0].id;
                    this.checkedListPart = [this.optionArrayPart[0]];
                }
            }
            if (isWatchPartList && this.optionArrayPart.length > 0 && !this.optionArrayPart.map(v => v.id).includes(this.checkedPart)) {
                this.checkedPart = this.optionArrayPart[0] && this.optionArrayPart[0].id;
            }
        }
    },

    computed: {
        partFiltered() {
            const str = this.partInput;
            if (!str) {
                return this.optionArrayPart;
            }
            let options = {
                threshold: 0,
                location: 0,
                distance: 100,
                keys: ["name"]
            };
            let searcher = new Fuse(this.optionArrayPart, options);
            return searcher.search(str).sort((firstItem, secondItem) => firstItem.name.toUpperCase() > secondItem.name.toUpperCase() ? 1 : -1);

        },
        titleShow() {
            if (this.checkedListPart.length === 0) {
                return 'Part ID: ';
            }
            return this.checkedListPart[0].name;
        },

    },
    watch: {
        checkedPart(value, oldValue) {
            console.log('@watch:delivery-part-filter>checkedPart', value, oldValue)
            if (value && oldValue) {
                const part = this.optionArrayPart.filter(part => part.id === value)[0];
                this.checkedListPart = [part];
                this.changePart(part);
            }
        },
        visible(newValue) {
            if (!newValue) {
                this.closeAnimation();
            }
        },
        defaultPartId(newValue, oldValue) {
            console.log('@watch:delivery-part-filter>defaultPartId', newValue)
            this.setDefaultFilter();
        },
        optionArrayPart(newValue, oldValue) {
            console.log('@watch:delivery-part-filter>optionArrayPart', newValue)
            this.setDefaultFilter(true);
        },
        resources(newValue) {
            this.allPart.name = newValue[this.allPart.name]
        }
    },
    mounted() {
        this.setDefaultFilter(true);
    }
};
</script>

<style scoped>
.dropdown_button {
    display: flex;
    justify-content: space-between;
    padding: 0px 10px;
    align-items: center;
    width: 100px;
    background: #f2f5fa;
}

.ant-btn.active,
.ant-btn:active,
.ant-btn:focus>svg {
    fill: #fff !important;
}

.down-icon {
    fill: #564efb;
}

.ant-menu-submenu-title:hover {
    background-image: linear-gradient(#414ff7, #6a4efb) !important;
    background-color: red;
}

.first-layer-wrapper {
    position: relative;
}

.first-layer {
    position: absolute;
    top: 0;
}
</style>
