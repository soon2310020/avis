<template>
    <div>
        <p class="common-component-desc"></p>
        <div class="common-component-content">
            <cta-button
                :active="activeProps"
                :disabled="disabledProps"
                :color-type="colorTypeProps"
                :button-type="buttonTypeProps"
                :size="sizeProps"
                >{{ inputText }}</cta-button
            >

            <div class="sub-content" style="margin-top: 30px">
                <!-- context text -->
                <div class="props-select-row">
                    <p>Enter text for the button.</p>
                    <input v-model="inputText" />
                </div>

                <!-- button type props -->
                <div class="props-select-row">
                    <p><span>button-type</span></p>
                    <select v-model="buttonTypeProps">
                        <option value="">== button type props ==</option>
                        <option value="">default</option>
                        <option value="dropdown">dropdown</option>
                        <option value="date-picker">date-picker</option>
                        <option value="upload">upload</option>
                        <option value="export">export</option>
                        <option value="text">text</option>
                        <option value="text dropdown">text dropdown</option>
                    </select>
                    <p>
                        <b>{{ buttonTypeProps }}</b>
                    </p>
                </div>

                <!-- color type props -->
                <div class="props-select-row">
                    <p><span>color-type</span></p>
                    <select v-model="colorTypeProps">
                        <option value="">== color type props ==</option>
                        <option value="blue">blue</option>
                        <option value="blue-fill">blue-fill</option>
                        <option value="red">red</option>
                        <option value="red-fill">red-fill</option>
                        <option value="green">green</option>
                        <option value="green-fill">green-fill</option>
                        <option value="white">white</option>
                    </select>
                    <p>
                        <b>{{ colorTypeProps }}</b> <span style="color: #e34537">(Select the button-type as <b>default</b>)</span>
                    </p>
                </div>

                <!-- active props -->
                <div class="props-select-row">
                    <p><span>active</span></p>
                    <select v-model="activeProps">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        <b>{{ activeProps }}</b>
                    </p>
                </div>

                <!-- disabled props -->
                <div class="props-select-row">
                    <p><span>disabled</span></p>
                    <select v-model="disabledProps">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        <b>{{ disabledProps }}</b>
                    </p>
                </div>

                <!-- size type props -->
                <div class="props-select-row">
                    <p><span>size</span></p>
                    <select v-model="sizeProps">
                        <option value="">== size props ==</option>
                        <option value="">default</option>
                        <option value="small">small</option>
                    </select>
                    <p>
                        <b>{{ sizeProps }}</b>
                    </p>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
module.exports = {
    components: {
        'cta-button': httpVueLoader('/components/common/cta-button.vue'),
    },
    data() {
        return {
            inputText: 'text',
            activeProps: false,
            disabledProps: false,
            colorTypeProps: '',
            buttonTypeProps: '',
            sizeProps: '',
        };
    },
};
</script>
