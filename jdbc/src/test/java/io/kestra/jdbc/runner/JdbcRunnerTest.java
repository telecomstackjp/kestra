package io.kestra.jdbc.runner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import io.kestra.core.junit.annotations.LoadFlows;
import io.kestra.core.models.executions.Execution;
import io.kestra.core.models.flows.State;
import io.kestra.core.runners.AbstractRunnerTest;
import io.kestra.core.runners.InputsTest;
import io.kestra.jdbc.JdbcTestUtils;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public abstract class JdbcRunnerTest extends AbstractRunnerTest {

    @Inject
    private JdbcTestUtils jdbcTestUtils;

    @Test
    @LoadFlows({"flows/valids/inputs-large.yaml"})
    void flowTooLarge() throws Exception {
        char[] chars = new char[200000];
        Arrays.fill(chars, 'a');

        Map<String, Object> inputs = new HashMap<>(InputsTest.inputs);
        inputs.put("string", new String(chars));

        Execution execution = runnerUtils.runOne(
            null,
            "io.kestra.tests",
            "inputs-large",
            null,
            (flow, execution1) -> flowIO.readExecutionInputs(flow, execution1, inputs),
            Duration.ofSeconds(120)
        );

        assertThat(execution.getTaskRunList().size(),
            greaterThanOrEqualTo(6)); // the exact number is test-run-dependent.
        assertThat(execution.getState().getCurrent(), is(State.Type.FAILED));

        // To avoid flooding the database with big messages, we re-init it
        jdbcTestUtils.drop();
        jdbcTestUtils.migrate();
    }


}
