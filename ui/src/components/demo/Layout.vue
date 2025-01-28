<template>
    <EmptyTemplate>
        <img :src="image.source" :alt="image.alt" class="img">
        <div class="message-block">
            <div class="enterprise-tag">
                {{ $t('demos.enterprise_edition') }}
            </div>
            <h2>{{ title }}</h2>
            <p><slot name="message" /></p>
            <slot name="buttons" />
        </div>
    </EmptyTemplate>
</template>

<script lang="ts" setup>
    import {onMounted, nextTick} from "vue";
    import {useStore} from "vuex";
    import EmptyTemplate from "../layout/EmptyTemplate.vue";

    const store = useStore();

    onMounted(() => {
        store.commit("doc/setDocPath", "<reset>")
        nextTick(() => {
            store.commit("doc/setDocPath", "")
            store.commit("misc/setContextInfoBarOpenTab", "docs")
        })
    });

    defineProps<{
        title: string;
        image: {
            source: string;
            alt: string;
        };
    }>();
</script>

<style lang="scss" scoped>
    @import "@kestra-io/ui-libs/src/scss/color-palette.scss";

    .img {
        width: 400px;
    }

    .message-block{
        text-align: left;
        width: 400px;
        margin: 0 auto;
        .enterprise-tag::before,
        .enterprise-tag::after{
            content: "";
            display: block;
            position: absolute;
            border-radius: 1rem;
        }

        .enterprise-tag::before{
            z-index: -2;
            background-image: linear-gradient(138.8deg, #CCE8FE 5.7%, #CDA0FF 27.03%, #8489F5 41.02%, #CDF1FF 68.68%, #B591E9 94%);
            top: -2px;
            bottom: -2px;
            left: -2px;
            right: -2px;
        }

        .enterprise-tag::after{
            z-index: -1;
            background: $base-gray-200;
            top: -1px;
            bottom: -1px;
            left: -1px;
            right: -1px;
        }

        .enterprise-tag{
            position: relative;
            background: $base-gray-200;
            border: 1px solid transparent;
            padding: 0 1rem;
            border-radius: 1rem;
            display: inline-block;
            z-index: 2;
        }

        h2 {
            margin-top: 1rem;
            line-height: 30px;
            font-size: 20px;
            font-weight: 600;
        }

        p {
            line-height: 22px;
            font-size: 14px;
        }
    }

</style>