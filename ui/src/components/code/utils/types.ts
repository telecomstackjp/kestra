import {defineComponent} from "vue";

export type Schemas = {
    $ref?: string;
    $schema?: string;
    definitions?: {
        [key: string]: object;
    };
};

export type Field = {
    component: ReturnType<typeof defineComponent>;
    value: string | number | boolean | undefined;
    label: string;
    required?: boolean;
    disabled?: boolean;
};

type ConcurrencyField = Field & {
    root: string;
    schema: object;
};

type Main = {
    id: Field;
    namespace: Field;
    description: Field;
};

type General = {
    retry: Field;
    labels: Field;
    inputs: Field;
    outputs: Field;
    variables: Field;
    concurrency: ConcurrencyField;
    pluginDefaults: Field;
    disabled: Field;
};

export type Fields = {
    main: Main;
    general: General;
};

export type Breadcrumb = {
    label: string;
    to: {
        name?: string;
    };
};

export type CollapseItem = {
    title: string;
};

export type Sections = {
    main: CollapseItem[];
    segments: CollapseItem[];
};
