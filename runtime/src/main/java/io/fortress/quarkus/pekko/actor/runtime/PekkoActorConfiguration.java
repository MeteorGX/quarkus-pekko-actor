package io.fortress.quarkus.pekko.actor.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Map;

/**
 * io.fortress.quarkus.pekko.actor.runtime.PekkoActorConfiguration.java
 * <p>
 * The core configuration interface for Pekko Actor system in Quarkus, bridging Quarkus configuration with Pekko's native configuration model.
 * <p>
 * Quarkus 中 Pekko Actor 系统的核心配置接口, 用于桥接 Quarkus 配置与 Pekko 的原生配置模型
 */
@ConfigMapping(prefix = "quarkus.actor")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface PekkoActorConfiguration {

    /**
     * The unique identifier name of the Actor system
     * <p>
     * Actor 系统的唯一标识符名称
     *
     * @return Actor System Name
     */
    @WithDefault("default")
    String name();

    /**
     * Defines Pekko config loading priorities, controlling how default framework configs interact with custom settings.
     * <p>
     * 定义 Pekko 配置加载优先级, 控制默认框架配置与自定义设置的交互方式.
     *
     * @return Actor Reference
     */
    @WithDefault("Reference")
    PekkoActorReference reference();


    /**
     * Whether to reuse Quarkus's thread pool configuration?
     * <p>
     * 是否复用 Quarkus 的线程池配置?
     *
     * @return use Quarkus-ExecutorService
     */
    @WithDefault("false")
    boolean overrideExecutor();


    /**
     * Used to override or supplement base configurations in key-value pairs; keys must exactly match Pekko's native configuration paths, e.g., "pekko.actor.default-dispatcher.fork-join-executor.parallelism-max=1"
     * <p>
     * 用于以键值对形式覆盖或补充基础配置, 键名必须与 Pekko 的原生配置路径完全匹配, 例如: "pekko.actor.default-dispatcher.fork-join-executor.parallelism-max=1"
     *
     * @return Pekko Settings
     */
    Map<String, String> settings();
}
