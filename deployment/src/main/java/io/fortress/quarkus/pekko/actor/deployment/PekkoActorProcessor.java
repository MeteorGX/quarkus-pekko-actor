package io.fortress.quarkus.pekko.actor.deployment;

import io.fortress.quarkus.pekko.actor.extension.ActorContainer;
import io.fortress.quarkus.pekko.actor.runtime.PekkoActorRecorder;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.runtime.RuntimeValue;
import jakarta.inject.Singleton;
import org.apache.pekko.actor.ActorSystem;

/**
 * io.fortress.quarkus.pekko.actor.deployment.PekkoActorProcessor.java
 * <p>
 * Quarkus deployment processor for Pekko extensions, handling build-phase tasks to initialize ActorSystem and wrap it as a global Bean.
 * <p>
 * Pekko扩展的Quarkus部署处理器, 处理构建阶段的任务将ActorSystem初始化以及包装成全局 Bean
 */
class PekkoActorProcessor {

    /**
     * Identifier for Pekko extension features in Quarkus (for feature activation and logging)
     * <p>
     * Quarkus 中标识 Pekko 扩展功能(用于功能激活和日志记录)
     */
    private static final String FEATURE = "pekko-actor";


    /**
     * Register pekko-Actor
     * <p>
     * 注册 pekko-actor 扩展功能
     *
     * @return FeatureBuildItem
     */
    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }


    /**
     * Build process for initializing Pekko ActorSystem and exposing it as a Quarkus synthetic bean
     * <p>
     * 初始化Pekko ActorSystem并将其作为Quarkus合成Bean暴露的构建流程
     *
     * @param recorder        PekkoActorRecorder
     * @param syntheticBeans  BeanCreator
     * @param shutdownContext Quarkus  ShutdownContext
     * @return Pekko Runtime
     */
    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    PekkoActorBuildItem initializeActorSystemBean(PekkoActorRecorder recorder,
                                                  BuildProducer<SyntheticBeanBuildItem> syntheticBeans,
                                                  ShutdownContextBuildItem shutdownContext
    ) {
        RuntimeValue<ActorSystem> holder = recorder.createActorSystem(shutdownContext);
        syntheticBeans.produce(
                SyntheticBeanBuildItem.configure(ActorSystem.class)
                        .runtimeValue(holder)
                        .setRuntimeInit()
                        .done());

        RuntimeValue<ActorContainer> container = recorder.createActorContainer(holder, shutdownContext);
        syntheticBeans.produce(
                SyntheticBeanBuildItem.configure(ActorContainer.class)
                        .runtimeValue(container)
                        .setRuntimeInit()
                        .done()
        );

        // Expose ActorSystem global runtime
        return new PekkoActorBuildItem(holder, container);
    }

}
