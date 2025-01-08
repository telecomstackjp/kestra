<template>
    <div class="h-100 overflow-y-auto no-code">
        <Breadcrumbs :flow />

        <hr class="m-0">

        <Editor :creation="!props.flow.id" :metadata :schemas />
    </div>
</template>

<script setup lang="ts">
    import {onBeforeMount, computed, ref} from "vue";

    import {Schemas} from "../utils/types";

    import YamlUtils from "../../utils/yamlUtils";

    import Breadcrumbs from "./components/Breadcrumbs.vue";
    import Editor from "./segments/Editor.vue";

    const props = defineProps({flow: {type: Object, required: true}});

    const metadata = computed(() => YamlUtils.getMetadata(props.flow.source));

    import {useStore} from "vuex";
    const store = useStore();

    const schemas = ref<Schemas>({});
    onBeforeMount(async () => {
        schemas.value = await store.dispatch("plugin/loadSchemaType");
    });
</script>

<style scoped lang="scss">
@import "./styles/code.scss";
</style>
