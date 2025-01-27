<template>
    <Bar
        :data="parsedData"
        :options="options"
        :total="total"
        :plugins="plugins"
        :duration="duration"
    />
</template>

<script setup>
    import {computed} from "vue";
    import {useI18n} from "vue-i18n";
    import moment from "moment";
    import {Bar} from "vue-chartjs";

    import {useMediaQuery} from "@vueuse/core";

    import Utils from "../../../../../utils/utils.js";
    import {getScheme} from "../../../../../utils/scheme.js";
    import {defaultConfig, getFormat} from "../../../../../utils/charts.js";

    const {t} = useI18n({useScope: "global"});

    const props = defineProps({
        data: {
            type: Object,
            required: true,
        },
        plugins: {
            type: Array,
            required: false,
            default: () => [],
        },
        total: {
            type: Number,
            required: false,
            default: undefined,
        },
        duration: {
            type: Boolean,
            required: false,
            default: true,
        },
        onClick: {
            type: Function,
            required: false,
            default: () => () => {
            },
        },
    });

    const isSmallScreen = useMediaQuery("(max-width: 610px)");

    const parsedData = computed(() => {
        let datasets = props.data.reduce(function (accumulator, value) {
            Object.keys(value.executionCounts).forEach(function (state) {
                if (accumulator[state] === undefined) {
                    accumulator[state] = {
                        label: state,
                        backgroundColor: getScheme(state),
                        yAxisID: "y",
                        data: [],
                    };
                }

                accumulator[state].data.push(value.executionCounts[state]);
            });

            return accumulator;
        }, Object.create(null));

        return {
            labels: props.data.map((r) =>
                moment(r.startDate).format(getFormat(r.groupBy)),
            ),
            datasets: props.duration
                ? [
                    {
                        type: "line",
                        label: t("duration"),
                        fill: false,
                        pointRadius: 0,
                        borderWidth: 0.75,
                        borderColor: "#A2CDFF",
                        yAxisID: "yB",
                        data: props.data.map((value) => {
                            return value.duration.avg === 0
                                ? 0
                                : Utils.duration(value.duration.avg);
                        }),
                    },
                    ...Object.values(datasets),
                ]
                : Object.values(datasets),
        };
    });

    const emit = defineEmits(["click"]);

    const options = computed(() =>
        defaultConfig({
            barThickness: isSmallScreen.value ? 8 : 12,
            skipNull: true,
            borderSkipped: false,
            borderColor: "transparent",
            borderWidth: 2,
            plugins: {
                barLegend: {
                    containerID: "executions",
                },
                tooltip: {
                    enabled: true,
                    filter: (value) => value.raw,
                    callbacks: {
                        label: (value) => {
                            const {label, yAxisID} = value.dataset;
                            return `${label.toLowerCase().capitalize()}: ${value.raw}${yAxisID === "yB" ? "s" : ""}`;
                        },
                    },
                },
            },
            scales: {
                x: {
                    title: {
                        display: true,
                        text: t("date"),
                    },
                    grid: {
                        display: false,
                    },
                    position: "bottom",
                    display: true,
                    stacked: true,
                    ticks: {
                        maxTicksLimit: isSmallScreen.value ? 5 : 8,
                        callback: function (value) {
                            const label = this.getLabelForValue(value);

                            if (
                                moment(label, ["h:mm A", "HH:mm"], true).isValid()
                            ) {
                                // Handle time strings like "1:15 PM" or "13:15"
                                return moment(label, ["h:mm A", "HH:mm"]).format(
                                    "h:mm A",
                                );
                            } else if (moment(new Date(label)).isValid()) {
                                // Handle date strings
                                const date = moment(new Date(label));
                                const isCurrentYear =
                                    date.year() === moment().year();
                                return date.format(
                                    isCurrentYear ? "MM/DD" : "MM/DD/YY",
                                );
                            }

                            // Return the label as-is if it's neither a valid date nor time
                            return label;
                        },
                    },
                },
                y: {
                    title: {
                        display: !isSmallScreen.value,
                        text: t("executions"),
                    },
                    grid: {
                        display: false,
                    },
                    display: true,
                    position: "left",
                    stacked: true,
                    ticks: {
                        maxTicksLimit: isSmallScreen.value ? 5 : 8,
                    },
                },
                yB: {
                    title: {
                        display: props.duration && !isSmallScreen.value,
                        text: t("duration"),
                    },
                    grid: {
                        display: false,
                    },
                    display: props.duration,
                    position: "right",
                    ticks: {
                        maxTicksLimit: isSmallScreen.value ? 5 : 8,
                        callback: function (value) {
                            return `${this.getLabelForValue(value)}s`;
                        },
                    },
                },
            },
            onClick: (e, elements) => {
                emit("click", e, elements);
            },
        }),
    );
</script>