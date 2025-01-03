/** @type {import('stylelint').Config} */
export default {
    extends: [
        "stylelint-config-recommended-scss",
        "stylelint-config-recommended-vue/scss"
    ],
    rules: {
        "color-no-hex": true,
        "no-descending-specificity": null,
        "scss/no-global-function-names": null,
    },
}