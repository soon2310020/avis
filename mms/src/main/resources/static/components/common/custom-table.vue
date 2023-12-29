<template>
    <div :class="classProps" :style="styleProps">
        <slot></slot>
    </div>
</template>

<script>
module.exports = {
    props: {
        classProps: {
            default: 'table-style-1',
            type: String,
        },
        styleProps: {
            default: '',
            type: String,
        },
    },
};
</script>

<style>
/* table-style-1 => ai-hub에 들어간 테이블 디자인 => 디자이너: tea lee => td 내부 항목은 div로 감싸야 함.  */
.table-style-1 > table > tbody > tr {
    border-right: solid 6px #fff;
    border-left: solid 6px #fff;
}

.table-style-1 > table > tbody > tr:nth-of-type(2n) {
    background-color: rgba(214, 218, 222, 0.41);
}

.table-style-1 > table > tbody > tr:first-of-type > td > div {
    margin-top: 10px;
}

.table-style-1 > table > thead > tr {
    border-bottom: 1px solid #c8ced3;
}

.table-style-1 > table th {
    font-size: 16px;
    height: 38px;
    padding-left: 24px;
}
.table-style-1 > table td > div {
    margin-left: 24px;
    font-size: 16px;
    height: 38px;
    display: flex;
    align-items: center;
}
.table-style-1 > table th {
    color: #454545;
}
</style>
