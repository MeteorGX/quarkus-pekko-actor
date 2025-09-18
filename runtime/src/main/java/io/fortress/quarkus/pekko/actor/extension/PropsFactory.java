package io.fortress.quarkus.pekko.actor.extension;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import jakarta.inject.Inject;
import org.apache.pekko.actor.Actor;
import org.apache.pekko.actor.Props;
import org.jboss.logging.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * io.fortress.quarkus.pekko.actor.extension.PropsFactory.java
 * <p>
 * Props Factory
 * <p>
 * Props 生成工具
 */
public final class PropsFactory {

    /**
     * Prohibition of Instantiation
     * <p>
     * 禁止实例化
     */
    private PropsFactory() {
        throw new IllegalStateException("prohibition of instantiation");
    }

    /**
     * Logger handler
     * <p>
     * 日志句柄
     */
    private static final Logger LOG = Logger.getLogger(PropsFactory.class);

    /**
     * Get the currently running Quarkus Arc container
     * <p>
     * 获取当前运行的 Quarkus Arc容器
     *
     * @return Optional
     */
    private static Optional<ArcContainer> container() {
        ArcContainer container = Arc.container();
        if (!Objects.isNull(container) && container.isRunning()) return Optional.of(container);
        return Optional.empty();
    }

    /**
     * Inject CDI dependencies into the @Inject fields of Actor instances
     * <p>
     * 为Actor实例的@Inject字段注入CDI依赖
     *
     * @param actor     AbstractActor
     * @param container Quarkus Arc Container
     * @param <T>       Actor
     * @return Actor Handler
     * @throws IllegalAccessException fields exception
     */
    private static <T extends Actor> T injectFields(T actor, ArcContainer container) throws IllegalAccessException {
        Class<?> clazz = actor.getClass();
        while (clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Object dependency = container.select(field.getType()).get();
                    field.set(actor, dependency);
                    LOG.debugf("Successfully injected dependency into field [%s] of Actor [%s]",
                            actor.self().path().name(), field.getName());
                }
            }
            clazz = clazz.getSuperclass();
        }
        return actor;
    }


    /**
     * Create Actor Props Supporting CDI Injection - Instantiate Actors via Default Constructor
     * <p>
     * 创建支持CDI注入的Actor Props - 通过默认构造器实例化Actor
     *
     * @param actor AbstractActor
     * @param <T>   Actor
     * @return Pekko Props
     */
    public static <T extends Actor> Props create(Class<T> actor) {
        if (container().isEmpty()) return Props.create(actor);
        ArcContainer container = container().get();

        return Props.create(actor, () -> {
            try {
                Constructor<T> constructor = actor.getDeclaredConstructor();
                constructor.setAccessible(true);
                T actorInstance = constructor.newInstance();
                return injectFields(actorInstance, container);
            } catch (Exception e) {
                LOG.warn("Failed to create and inject actor [ActorClass: " + actor.getName() + "]", e);
                throw new RuntimeException("Failed to create and inject actor [ActorClass: " + actor.getName() + "]", e);
            }
        });
    }

    /**
     * Create Actor Props Supporting CDI Injection - Instantiate Actors via Custom Supplier
     * <p>
     * 创建支持CDI注入的Actor Props - 通过自定义Supplier实例化Actor
     *
     * @param actor   AbstractActor
     * @param creator Supplier
     * @param <T>     Actor
     * @return Pekko Props
     */
    public static <T extends Actor> Props create(Class<T> actor, Supplier<T> creator) {
        if (container().isEmpty()) {
            // fixed: 'org.apache.pekko.japi.Creator' is deprecated
            return Props.create(actor, () -> creator.get());
        }

        ArcContainer container = container().get();
        return Props.create(actor, () -> {
            try {
                T actorInstance = creator.get();
                return injectFields(actorInstance, container);
            } catch (Exception e) {
                LOG.warn("Failed to create and inject actor [ActorClass: " + actor.getName() + "]", e);
                throw new RuntimeException("Failed to create and inject actor [ActorClass: " + actor.getName() + "]", e);
            }
        });
    }
}
