<template>
    <div class="p-4">
        <component
            v-for="(field, index) in fields.main"
            :key="index"
            :is="field.component"
            v-model="field.value"
            v-bind="trimmed(field)"
        />

        <Collapse :items="sections.main">
            <template #content>
                <component
                    v-for="(field, index) in fields.general"
                    :key="index"
                    :is="field.component"
                    v-model="field.value"
                    v-bind="trimmed(field)"
                />
            </template>
        </Collapse>

        <hr class="m-0 mt-3">

        <Collapse :items="sections.segments" creation />
    </div>
</template>

<script setup lang="ts">
    import {ref, shallowRef} from "vue";

    import {Field, Fields, Segment, Sections} from "../utils/types";

    import Collapse from "../components/collapse/Collapse.vue";
    import InputText from "../components/inputs/InputText.vue";
    import InputSwitch from "../components/inputs/InputSwitch.vue";

    const CONCURRENCY = "io.kestra.core.models.flows.Concurrency";

    import {useI18n} from "vue-i18n";
    const {t} = useI18n({useScope: "global"});

    const props = defineProps({
        creation: {type: Boolean, default: false},
        flow: {type: Object, required: true},
        metadata: {type: Object, required: true},
        schemas: {type: Object, required: true},
    });

    const trimmed = (field: Field) => {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const {component, value, ...rest} = field;

        return rest;
    };

    const fields = ref<Fields>({
        main: {
            id: {
                component: shallowRef(InputText),
                value: props.creation ? undefined : props.metadata.id,
                label: t("no_code.fields.main.flow_id"),
                required: true,
                disabled: !props.creation,
            },
            namespace: {
                component: shallowRef(InputText),
                value: props.creation ? undefined : props.metadata.namespace,
                label: t("no_code.fields.main.namespace"),
                required: true,
                disabled: !props.creation,
            },
            description: {
                component: shallowRef(InputText),
                value: props.metadata.description,
                label: t("no_code.fields.main.description"),
            },
        },
        general: {
            retry: {
                component: shallowRef(InputText),
                value: props.metadata.retry,
                label: t("no_code.fields.general.retry"),
            },
            labels: {
                component: shallowRef(InputText), // TODO: Change component
                value: props.metadata.labels,
                label: t("no_code.fields.general.labels"),
            },
            inputs: {
                component: shallowRef(InputText), // TODO: Change component
                value: props.metadata.inputs,
                label: t("no_code.fields.general.inputs"),
            },
            outputs: {
                component: shallowRef(InputText),
                value: props.metadata.outputs,
                label: t("no_code.fields.general.outputs"),
            },
            variables: {
                component: shallowRef(InputText), // TODO: Change component
                value: props.metadata.variables,
                label: t("no_code.fields.general.variables"),
            },
            concurrency: {
                component: shallowRef(InputSwitch), // TODO: To improve slot content
                value: props.metadata.concurrency,
                label: t("no_code.fields.general.concurrency"),
                schema: props.schemas?.definitions?.[CONCURRENCY] ?? {},
                root: "concurrency",
            },
            pluginDefaults: {
                component: shallowRef(InputText),
                value: props.metadata.pluginDefaults,
                label: t("no_code.fields.general.plugin_defaults"),
            },
            disabled: {
                component: shallowRef(InputSwitch),
                value: props.metadata.disabled,
                label: t("no_code.fields.general.disabled"),
            },
        },
    });

    const getSectionTitle = (
        label: string,
        section: string,
        array = [],
    ): Segment => {
        const title = t(`no_code.sections.${label}`);
        return {title: `${title} (${array.length})`, section};
    };
    const sections = ref<Sections>({
        main: [{title: t("no_code.sections.general")}],
        segments: [
            getSectionTitle("tasks", "tasks", props.flow.tasks),
            getSectionTitle("triggers", "triggers", props.flow.triggers),
            getSectionTitle("error_handlers", "errors", props.flow.errors),
        ],
    });
</script>
