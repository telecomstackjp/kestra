package io.kestra.core.models.hierarchies;

import io.kestra.core.utils.IdUtils;
import lombok.Getter;

@Getter
public class GraphClusterAlways extends AbstractGraph {
    public GraphClusterAlways() {
        super("always-" + IdUtils.create());
    }
}
