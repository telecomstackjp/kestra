<template>
    <el-collapse v-model="expanded" class="mt-3 wrapper">
        <el-collapse-item
            v-for="(item, index) in props.items"
            :key="index"
            :name="item.title"
            :title="item.title"
            :class="{creation: props.creation}"
        >
            <template #icon>
                <Creation
                    v-if="creation"
                    :section="(item as Segment).section"
                />
            </template>
            <slot name="content" />
        </el-collapse-item>
    </el-collapse>
</template>

<script setup lang="ts">
    import {PropType, ref} from "vue";

    import {CollapseItem, Segment} from "../../utils/types";

    import Creation from "./buttons/Creation.vue";

    const props = defineProps({
        items: {
            type: Array as PropType<CollapseItem[] | Segment[]>,
            required: true,
        },
        creation: {type: Boolean, default: false},
    });
    const expanded = ref([]);
</script>

<style scoped lang="scss">
@import "../../styles/code.scss";

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
            padding: 0.5rem 0;
        }

        .el-collapse-item__header {
            justify-content: space-between;

            &.is-active {
                color: $code-primary;
            }
        }
    }
}
</style>
