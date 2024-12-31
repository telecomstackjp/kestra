<template>
    <div class="tag">
        <component :is="props.icon" class="icon" />
        <span>{{ props.label }}</span>
    </div>
</template>

<script setup>
    const props = defineProps({
        icon: {
            type: Object,
            required: true
        },
        label: {
            type: String,
            required: true
        }
    });
</script>

<style scoped lang="scss">
    .tag {
        display: inline-flex;
        align-items: center;
        padding: 3px 6px;
        border-radius: 4px;
        border: 1px solid var(--ks-border-primary);
        background-color: var(--ks-tag-background);
        color: var(--ks-tag-content);
        font-size: 0.75rem;

        &:hover{
            background-color: var(--ks-tag-background-hover);
        }

        .icon {
            margin-right: 5px !important;
        }
    }
</style>
