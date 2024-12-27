<template>
    <el-breadcrumb class="p-4">
        <el-breadcrumb-item
            v-for="(breadcrumb, index) in breadcrumbs"
            :key="index"
        >
            <router-link :to="breadcrumb.to">
                {{ breadcrumb.label }}
            </router-link>
        </el-breadcrumb-item>
    </el-breadcrumb>
</template>

<script setup lang="ts">
    import {computed} from "vue";

    import {useI18n} from "vue-i18n";
    const {t} = useI18n({useScope: "global"});

    const props = defineProps({flow: {type: Object, required: true}});

    const breadcrumbs = computed(() => {
        return [
            {
                label: props.flow.id ?? t("create_flow"),
                to: {},
            },
        ];
    });
</script>
