package io.kestra.runner.h2;

import io.kestra.core.junit.annotations.LoadFlows;
import io.kestra.jdbc.runner.JdbcRunnerTest;
import org.junit.jupiter.api.Test;

public class H2RunnerTest extends JdbcRunnerTest {

    @Test
    @LoadFlows({"flows/valids/trigger-multiplecondition-listener.yaml",
        "flows/valids/trigger-multiplecondition-flow-a.yaml",
        "flows/valids/trigger-multiplecondition-flow-b.yaml"})
    void multipleConditionTrigger() throws Exception {
        multipleConditionTriggerCaseTest.trigger();
    }

}
