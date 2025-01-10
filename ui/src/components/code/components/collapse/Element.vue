<template>
    <div class="d-flex my-2 p-2 rounded element">
        <div class="me-2 icon">
            <TaskIcon :cls="props.element.type" :icons only-icon />
        </div>

        <div class="flex-grow-1 label">
            {{ props.element.id }}
        </div>
    </div>
</template>

<script setup lang="ts">
    import {computed} from "vue";

    import TaskIcon from "@kestra-io/ui-libs";

    const props = defineProps({element: {type: Object, required: true}});

    import {useStore} from "vuex";
    const store = useStore();

    const icons = computed(() => store.state.plugin.icons);
</script>

<style scoped lang="scss">
@import "../../styles/code.scss";

.element {
    background-color: $code-card-color;
    border: 1px solid $code-border-color;

    & > .icon {
        width: 1.25rem;
    }

    & > .label {
        font-size: $code-font-sm;
    }
}
</style>
