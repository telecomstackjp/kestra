import {defineComponent} from "vue";
import type {RouteRecordName, RouteParams} from "vue-router";

export type Schemas = {
    $ref?: string;
    $schema?: string;
    definitions?: {
        [key: string]: object;
    };
};

export type Field = {
    component: ReturnType<typeof defineComponent>;
    value: any;
    label: string;
    required?: boolean;
    disabled?: boolean;
};

export type LabelField = Omit<Field, "value"> & {
    value: [string, string][];
};

type InputField = Field & {
    inputs: any[];
};

type VariableField = Field & {
    variables: any[];
};

type ConcurrencyField = Field & {
    root: string;
    schema: object;
};

type EditorField = Field & {
    navbar: boolean;
    input: boolean;
    lang: string;
    style: {
        height: string;
    };
};

export type Fields = {
    id: Field;
    namespace: Field;
    description: Field;
    retry: EditorField;
    labels: LabelField;
    inputs: InputField;
    outputs: EditorField;
    variables: VariableField;
    concurrency?: ConcurrencyField; // TODO: Make it not optional
    pluginDefaults: EditorField;
    disabled: Field;
};

export type Breadcrumb = {
    label: string;
    to: {
        name: RouteRecordName;
        params: RouteParams;
    };
};

export type CollapseItem = {
    title: string;
    elements?: Record<string, any>[];
};
