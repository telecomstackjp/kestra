<template>
    <el-collapse v-model="expanded" class="mt-3 wrapper">
        <el-collapse-item
            v-for="(item, index) in props.items"
            :key="index"
            :name="item.title"
            :title="item.title"
        >
            <component :is="item.component" />
        </el-collapse-item>
    </el-collapse>
</template>

<script setup lang="ts">
    import {ref} from "vue";

    import {CollapseItem} from "../utils/types";

    const props = defineProps<{ items: CollapseItem[] }>();
    const expanded = ref([]);
</script>

<style scoped lang="scss">
@import "../styles/code.scss";

.wrapper {
    & * {
        font-size: $code-font-sm;
    }

    :deep(*) {
        --el-collapse-header-bg-color: initial;
        --el-collapse-header-text-color: #{$code-gray-700};
        --el-collapse-content-bg-color: initial;

        .el-collapse-item__header,
        .el-collapse-item__content {
            padding-left: 0;
        }
    }
}
</style>
