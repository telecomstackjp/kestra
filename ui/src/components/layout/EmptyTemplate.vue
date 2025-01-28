<template>
    <section class="container empty-template">
        <slot />
    </section>
</template>

<style lang="scss" scoped>
    .empty-template {
            margin-top: 0;
            padding: 3rem 0;
            text-align: center;
            html.dark {
                background-image: url("../../assets/empty-page.svg#file");
                background-repeat: no-repeat;
                background-position: top center;
            }
    }
</style>