<template>
    <Layout
        :title="t(`demos.apps.title`)"
        :image="{source: sourceImg, alt: t(`demos.apps.title`)}"
    >
        <template #message>
            {{ $t(`demos.apps.message`) }}
        </template>
        <template #buttons>
            <el-button target="_blank" href="https://kestra.io/demo" type="primary" size="large">
                {{ $t("demos.get_a_demo_button") }}
            </el-button>
        </template>
    </Layout>
</template>

<script setup lang="ts">
    import {useI18n} from "vue-i18n";
    import Layout from "./Layout.vue";
    import sourceImg from "../../assets/demo/apps.png";

    const {t} = useI18n();
</script>