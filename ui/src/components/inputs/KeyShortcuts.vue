<template>
    <el-tooltip
        :content="$t('editor_shortcuts.label')"
        :hide-after="0"
        :persistent="false"
        effect="light"
    >
        <el-button @click="isShown = true" :icon="Keyboard" />
    </el-tooltip>

    <el-dialog v-model="isShown">
        <template #header>
            <div class="d-flex align-items-center gap-2 fw-normal">
                <el-icon size="30px">
                    <Keyboard />
                </el-icon>
                <span class="fs-6">
                    {{ $t("editor_shortcuts.label") }}
                </span>
            </div>
        </template>

        <div class="d-flex flex-column gap-3 fw-normal">
            <div v-for="(command, i) in commands" :key="i" class="d-flex align-items-center gap-3">
                <div class="d-flex align-items-center gap-2 keys">
                    <template v-for="(key, index) in command.keys" :key="index">
                        <el-tag>{{ key }}</el-tag>
                        <span v-if="index < command.keys.length - 1" class="fw-bold">+</span>
                    </template>
                </div>
                <div class="text-break">
                    {{ $t(command.description) }}
                </div>
            </div>
        </div>
    </el-dialog>
</template>

<script setup>
    import {ref} from "vue";
    import Keyboard from "vue-material-design-icons/Keyboard.vue";

    const isShown = ref(false);

    const commands = [
        {
            keys: ["⌘ Cmd/Ctrl", "s"],
            description: "editor_shortcuts.save_flow"
        },
        {
            keys: ["⌥ Option/Alt", "↑", "↓"],
            description: "editor_shortcuts.move_line"
        },
        {
            keys: ["⇧ Shift", "⌥ Option/Alt", "↑", "↓"],
            description: "editor_shortcuts.duplicate_line"
        },
        {
            keys: ["⌘ Cmd/Ctrl", "k", "l"],
            description: "editor_shortcuts.fold_unfold"
        },
        {
            keys: ["⌘ Cmd/Ctrl", "/"],
            description: "editor_shortcuts.comment_uncomment"
        },
        {
            keys: ["⌘ Cmd/Ctrl", "k", "c"],
            description: "editor_shortcuts.comment"
        },
        {
            keys: ["⌘ Cmd/Ctrl", "k", "u"],
            description: "editor_shortcuts.uncomment"
        }
    ];
</script>

<style scoped lang="scss">
.keys {
    min-width: 50%;
}

.el-tag {
    font-size: var(--el-tag-font-size);
    background-color: #e3dbff;
    text-transform: capitalize;
    font-weight: 700;
    border: 1px solid #d1c4ff;
    border-radius: 4px;
    box-shadow: 0 2px #d1c4ff;
    transition: all 0.2s ease;
    display: inline-block;
    padding: 6px 10px;
}

.el-tag::after {
    content: attr(data-content);
    text-transform: none;
}
</style>