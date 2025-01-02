<template>
    <el-tooltip :content="$t('shortcuts')" transition="" :hide-after="0" :persistent="false" effect="light">
        <el-button @click="visible = true" :icon="Keyboard" />
    </el-tooltip>
    <el-dialog
        v-model="visible"
        draggable
    >
        <template #header>
            <div class="d-flex align-items-center gap-2 fw-normal">
                <el-icon size="30px">
                    <Keyboard />
                </el-icon>
                <span class="fs-6"> {{ $t("shortcuts") }} </span>
            </div>
        </template>
        <div class="d-flex flex-column gap-3 fw-normal">
            <div class="d-flex align-items-center gap-2">
                <div class="d-flex align-items-center gap-2 keys">
                    <el-tag>⌥ Option/Alt</el-tag>
                    + <el-tag>↑</el-tag>/<el-tag>↓</el-tag>
                </div>
                <div class="text-break">
                    {{ $t("move line") }}
                </div>
            </div>
            <div class="d-flex align-items-center gap-2">
                <div class="d-flex align-items-center gap-2 keys">
                    <el-tag>⇧ Shift</el-tag>
                    + <el-tag>⌥ Option/Alt</el-tag>
                    + <el-tag>↑</el-tag>/<el-tag>↓</el-tag>
                </div>
                <div class="text-break">
                    {{ $t("duplicate line") }}
                </div>
            </div>
            <div class="d-flex align-items-center gap-2">
                <div class="d-flex align-items-center gap-2 keys">
                    <el-tag>⌘ Cmd/Ctrl</el-tag>
                    + <el-tag>s</el-tag>
                </div>
                <div class="text-break">
                    {{ $t("save flow") }}
                </div>
            </div>
            <div class="d-flex align-items-center gap-2">
                <div class="d-flex align-items-center gap-2 keys">
                    <el-tag>⌘ Cmd/Ctrl</el-tag>
                    + <el-tag>/</el-tag>
                </div>
                <div class="text-break">
                    {{ $t("comment/uncomment") }}
                </div>
            </div>
            <div class="d-flex align-items-center gap-2">
                <div class="d-flex align-items-center gap-2 keys">
                    <el-tag>⌘ Cmd/Ctrl</el-tag>
                    + <el-tag>k</el-tag> + <el-tag>c</el-tag>
                </div>
                <div class="text-break">
                    {{ $t("comment") }}
                </div>
            </div>
            <div class="d-flex align-items-center gap-2">
                <div class="d-flex align-items-center gap-2 keys">
                    <el-tag>⌘ Cmd/Ctrl</el-tag>
                    + <el-tag>k</el-tag> + <el-tag>u</el-tag>
                </div>
                <div class="text-break">
                    {{ $t("uncomment") }}
                </div>
            </div>
            <div class="d-flex align-items-center gap-2">
                <div class="d-flex align-items-center gap-2 keys">
                    <el-tag>⌘ Cmd/Ctrl</el-tag>
                    + <el-tag>k</el-tag> + <el-tag>l</el-tag>
                </div>
                <div class="text-break">
                    {{ $t("fold/unfold code") }}
                </div>
            </div>
        </div>
    </el-dialog>
</template>

<script setup>
    import {ref} from "vue";
    const visible = ref(false);
    import Keyboard from "vue-material-design-icons/Keyboard.vue";
</script>

<style scoped lang="scss">

.keys{
    min-width: 50%;
}

.el-tag {
    font-size: var(--el-tag-font-size);
    background-color: #e3dbff;
    text-transform: capitalize;
    font-weight: 700;
}

.el-tag::after {
    content: attr(data-content);
}

.el-tag[data-content*='⌘ Cmd'], 
.el-tag[data-content*='Ctrl'], 
.el-tag[data-content*='⌥ Option'], 
.el-tag[data-content*='Alt'], 
.el-tag[data-content*='⇧ Shift'] {
    text-transform: none;
}
</style>

<style lang="scss">
.el-dialog__headerbtn {
    font-size: 1.5rem;
}
</style>
