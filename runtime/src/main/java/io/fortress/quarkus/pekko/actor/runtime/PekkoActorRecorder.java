package io.fortress.quarkus.pekko.actor.runtime;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.fortress.quarkus.pekko.actor.extension.ActorContainer;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import org.apache.pekko.actor.ActorSystem;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logmanager.LogContext;
import org.jboss.logmanager.Logger;
import scala.concurrent.ExecutionContext;

import java.util.Map;
import java.util.Objects;

/**
 * io.fortress.quarkus.pekko.actor.runtime.PekkoActorRecorder.java
 * <p>
 * Handles runtime initialization of Pekko ActorSystem in Quarkus, responsible for loading, creating and cleaning up ActorSystem to align with Quarkus lifecycle.
 * <p>
 * 用于在Quarkus中处理Pekko ActorSystem的运行时初始化, 负责ActorSystem的加载创建和清理工作, 使其与Quarkus的生命周期保持一致.
 */
@Recorder
public class PekkoActorRecorder {

    /**
     * RuntimeValue object storing resolved Pekko configuration (calculated at build time, used at runtime)
     * <p>
     * 存储已解析的Pekko配置的RuntimeValue对象(在构建时计算,运行时使用)
     */
    private final RuntimeValue<PekkoActorConfiguration> configurationRuntime;

    /**
     * Constructor that initializes the logger and passes configuration, called by the Quarkus extension framework during build phase (configuration pre-resolved).
     * <p>
     * 初始化记录器并传入配置的构造函数, 在构建阶段由Quarkus扩展框架调用(配置已预先解析)
     *
     * @param configurationRuntime PekkoActorConfiguration Runtime
     */
    public PekkoActorRecorder(RuntimeValue<PekkoActorConfiguration> configurationRuntime) {
        this.configurationRuntime = configurationRuntime;
    }


    /**
     * Initialize Pekko ActorSystem and register it with Quarkus's shutdown lifecycle; main entry point for creating ActorSystem on app startup.
     * <p>
     * 初始化Pekko ActorSystem并将其注册到Quarkus的关闭生命周期中, 应用启动时创建ActorSystem的主要入口点.
     *
     * @param shutdownContext Quarkus ShutdownContext
     * @return ActorSystemRuntime
     */
    public RuntimeValue<ActorSystem> createActorSystem(ShutdownContext shutdownContext) {
        PekkoActorConfiguration configuration = configurationRuntime.getValue();
        ActorSystem system = createActorSystem(configuration);

        // register shutdown callback
        shutdownContext.addLastShutdownTask(() -> {
            system.terminate();
            system.getWhenTerminated().toCompletableFuture().join();// wait
        });

        // print configs
        system.log().info("ActorSystem name: {}", system.name());
        system.log().info("ActorSystem reference: {}", configuration.reference().toString());
        system.log().info("ActorSystem override executor: {}", configuration.overrideExecutor() ? "Yes" : "No");
        configuration.settings().forEach((key, value) -> system.log().info("Actor config: {} = {}", key, value));

        // return actor system handler
        return new RuntimeValue<>(system);
    }


    /**
     * Create an Actor management container
     * <p>
     * 创建 Actor 管理容器
     *
     * @param systemRuntimeValue Actor System runtime
     * @param shutdownContext    Quarkus ShutdownContext
     * @return ActorContainerRuntime
     */
    public RuntimeValue<ActorContainer> createActorContainer(RuntimeValue<ActorSystem> systemRuntimeValue, ShutdownContext shutdownContext) {
        ActorContainer container = new ActorContainer(systemRuntimeValue.getValue());
        shutdownContext.addShutdownTask(() -> container.clear());
        return new RuntimeValue<>(container);
    }


    /**
     * Create an Actor System instance
     * <p>
     * 创建 Actor System 实例
     *
     * @param configuration PekkoActorConfiguration
     * @return ActorSystem
     */
    public ActorSystem createActorSystem(PekkoActorConfiguration configuration) {
        Config originalConfig = ConfigFactory.load();
        switch (configuration.reference()) {
            case Application:
                originalConfig = originalConfig.withFallback(ConfigFactory.defaultApplication());
                break;
            case Overrides:
                originalConfig = originalConfig.withFallback(ConfigFactory.defaultOverrides());
                break;
            case ReferenceUnresolved:
                originalConfig = originalConfig.withFallback(ConfigFactory.defaultReferenceUnresolved());
                break;
            default:
                originalConfig = originalConfig.withFallback(ConfigFactory.defaultReference());
        }
        Map<String, String> settings = configuration.settings();

        // set slf4 logger
        if (!settings.containsKey("pekko.logging-filter")) {
            settings.put("pekko.use-slf4j", "on");
        }

        // set quarkus logger-level
        if (!settings.containsKey("pekko.log-level")) {
            Logger rootLogger = LogContext.getLogContext().getLogger("");
            settings.put("pekko.loglevel", rootLogger.getLevel().toString());
        }


        // use Quarkus ExecutorService
        if (configuration.overrideExecutor()) {
            // merge configuration
            originalConfig = ConfigFactory.parseMap(settings).withFallback(originalConfig);

            // ClassLoader
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            // ExecutionContext
            ArcContainer container = Arc.container();
            if (!Objects.isNull(container) && container.isRunning()) {
                ExecutionContext context = scala.concurrent.ExecutionContext.fromExecutorService(Arc.container().getExecutorService());
                return ActorSystem.create(configuration.name(), originalConfig, classLoader, context);
            } else {
                return ActorSystem.create(configuration.name(), originalConfig, classLoader);
            }
        } else {
            return ActorSystem.create(configuration.name(), originalConfig);
        }


    }

}
