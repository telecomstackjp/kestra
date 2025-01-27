<template>
    <section class="container empty-template">
        <img :src="image.source" :alt="image.alt" class="img">
        <h2>{{ title }}</h2>
        <p><slot name="message" /></p>
        <slot name="buttons" />
    </section>
</template>

<script lang="ts" setup>
    defineProps<{
        title: string;
        image: {
            source: string;
            alt: string;
        };
    }>()
</script>

<style lang="scss" scoped>
    .empty-template {
            margin-top: 0;
            padding-top: 10rem;
            padding-bottom: 3rem;
            text-align: center;
            background-image: url("../../assets/empty-page.svg#file");
            background-repeat: no-repeat;
            background-position: top center;
    }

    .img {
        background: url("../../assets/errors/kestra-error.png") no-repeat center;
        background-size: contain;
        max-height: 156px;
    }

    h2 {
        line-height: 30px;
        font-size: 20px;
        font-weight: 600;
    }

    p {
        line-height: 22px;
        font-size: 14px;
    }
</style>