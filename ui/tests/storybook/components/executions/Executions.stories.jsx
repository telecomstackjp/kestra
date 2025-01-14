import {useStore} from "vuex";
import {mockRouter} from "storybook-vue3-router";
import Executions from "../../../../src/components/executions/Executions.vue";
import fixture from "./Executions.fixture.json"

// Story configuration
export default {
    title: "Components/Executions",
    component: Executions,
    decorators: [
        () => {
            return {
                setup () {
                    const store = useStore()
                    store.commit("auth/setUser", {
                        id: "123",
                        firstName: "John",
                        lastName: "Doe",
                        email: "john.doe@example.com",
                        isAllowed: () => true,
                    })
                    store.$http = {
                        get(a) {
                            if (a.endsWith("bindings/search")) {
                                return Promise.resolve({
                                    data: fixture
                                })
                            }
                            return Promise.resolve({data: []})
                        },
                    }
                    return {}
                },
                template: "<story />"
            }
        },
        mockRouter({
            meta: ["some_meta"],
            params: ["some_param"],
            query: ["some_query"]
        }),
    ],
    parameters: {
        layout: "fullscreen"
    }
};

// Stories
export const Default = {
    args: {
        hidden: [],
        statuses: [],
        isReadOnly: false,
        embed: true,
        topbar: false,
        filter: false
    }
};