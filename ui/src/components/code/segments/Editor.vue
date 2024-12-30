<template>
    <div class="p-4">
        <InputText
            v-for="(field, index) in fields.main"
            :key="index"
            v-model="field.value"
            :label="field.label"
            :required="field.required"
            :disabled="field.disabled"
        />
    </div>
</template>

<script setup lang="ts">
    import {ref} from "vue";

    import {Fields} from "../utils/types";

    import InputText from "../components/inputs/InputText.vue";

    import {useI18n} from "vue-i18n";
    const {t} = useI18n({useScope: "global"});

    const props = defineProps({
        creation: {type: Boolean, default: false},
        metadata: {type: Object, required: true},
    });

    const fields = ref<Fields>({
        main: {
            id: {
                value: props.creation ? undefined : props.metadata.id,
                label: t("flow_id"),
                required: true,
                disabled: !props.creation,
            },
            namespace: {
                value: props.creation ? undefined : props.metadata.namespace,
                label: t("namespace"),
                required: true,
                disabled: !props.creation,
            },
            description: {
                value: props.metadata.description ?? undefined,
                label: t("description"),
            },
        },
    });
</script>
