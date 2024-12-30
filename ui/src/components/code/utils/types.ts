type Field = {
    value: string | number | boolean | undefined;
    label: string;
    required?: boolean;
    disabled?: boolean;
};

type Main = {
    id: Field;
    namespace: Field;
    description: Field;
};

export type Fields = {
    main: Main;
};
