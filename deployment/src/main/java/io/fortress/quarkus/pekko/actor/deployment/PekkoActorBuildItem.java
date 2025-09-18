package io.fortress.quarkus.pekko.actor.deployment;

import io.fortress.quarkus.pekko.actor.extension.ActorContainer;
import io.quarkus.builder.item.SimpleBuildItem;
import io.quarkus.runtime.RuntimeValue;
import org.apache.pekko.actor.ActorSystem;

/**
 * io.fortress.quarkus.pekko.actor.deployment.PekkoActorBuildItem.java
 * <p>
 * For passing runtime-managed {@link ActorSystem} instances between Quarkus build steps or to other dependent extensions.
 * <p>
 * 用于在Quarkus构建步骤之间或向其他依赖扩展传递由运行时管理的{@link ActorSystem}运行时实例
 */
public final class PekkoActorBuildItem extends SimpleBuildItem {

    /**
     * Actor System Runtime
     */
    private final RuntimeValue<ActorSystem> system;

    /**
     * Actor Container Runtime
     */
    private final RuntimeValue<ActorContainer> container;

    /**
     * @param system Already created ActorSystem runtime
     */
    public PekkoActorBuildItem(RuntimeValue<ActorSystem> system, RuntimeValue<ActorContainer> container) {
        this.system = system;
        this.container = container;
    }

    /**
     * @return ActorSystem
     */
    public RuntimeValue<ActorSystem> getSystem() {
        return system;
    }

    /**
     * @return ActorContainer
     */
    public RuntimeValue<ActorContainer> getContainer() {
        return container;
    }
}
