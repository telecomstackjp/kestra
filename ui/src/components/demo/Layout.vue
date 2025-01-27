<template>
    <EmptyTemplate>
        <img :src="image.source" :alt="image.alt" class="img">
        <h2>{{ title }}</h2>
        <p><slot name="message" /></p>
        <slot name="buttons" />
    </EmptyTemplate>
</template>

<script lang="ts" setup>
    import EmptyTemplate from "../layout/EmptyTemplate.vue";

    defineProps<{
        title: string;
        image: {
            source: string;
            alt: string;
        };
    }>();
</script>

<style lang="scss" scoped>
    .img {
        max-height: 236px;
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