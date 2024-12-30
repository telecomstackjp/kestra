<template>
    <div>
        <Breadcrumbs :flow />

        <hr class="m-0">

        <Editor :creation="!props.flow.id" :metadata />
    </div>
</template>

<script setup lang="ts">
    import {computed} from "vue";

    import YamlUtils from "../../utils/yamlUtils";

    import Breadcrumbs from "./components/Breadcrumbs.vue";
    import Editor from "./segments/Editor.vue";

    const props = defineProps({flow: {type: Object, required: true}});

    const metadata = computed(() => YamlUtils.getMetadata(props.flow.source));
</script>
