<template>
    <top-nav-bar :title="routeInfo.title" v-if="!isFullScreen()" />
    <empty-template
        title="IAM"
        :image="{source: sourceImg, alt: t('demos.IAM.title')}"
    >
        <template #message>
            {{ $t('demos.IAM.message') }}
        </template>
        <template #buttons>
            <el-button v-if="!isFullScreen()" target="_blank" href="https://kestra.io/demo" type="primary" size="large">
                {{ $t("demos.get_a_demo_button") }}
            </el-button>
        </template>
    </empty-template>
</template>

<script setup lang="ts">
    import {useI18n} from "vue-i18n";
    import EmptyTemplate from "../layout/EmptyTemplate.vue";
    // @ts-expect-error no types in TopNavBar yet
    import TopNavBar from "../../components/layout/TopNavBar.vue";
    import sourceImg from "../../assets/errors/kestra-error.png";

    const {t} = useI18n();
    const routeInfo = {
        title: "IAM"
    };


    function isFullScreen() {
        return document.getElementsByTagName("html")[0].classList.contains("full-screen");
    }
</script>