<template>
    <section class="container empty-template">
        <slot />
    </section>
</template>

<style lang="scss" scoped>
    .empty-template {
            margin-top: 0;
            padding-top: 10rem;
            padding-bottom: 3rem;
            text-align: center;
            background-image: url("../../assets/empty-page.svg#file");
            background-repeat: no-repeat;
            background-position: top center;
    }
</style>