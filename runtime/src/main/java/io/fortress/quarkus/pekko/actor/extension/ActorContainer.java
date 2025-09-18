package io.fortress.quarkus.pekko.actor.extension;

import org.apache.pekko.actor.*;
import org.apache.pekko.event.LoggingAdapter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * io.fortress.quarkus.pekko.actor.extension.ActorContainer.java
 * <p>
 * Actor Global Management Bean Container
 * <p>
 * Actor全局管理Bean容器
 */
public final class ActorContainer {

    /**
     * An instance of the Pekko ActorSystem, to which all managed Actors belong
     * <p>
     * Pekko Actor 系统实例，所有托管的 Actor 都属于该系统
     */
    private final ActorSystem system;

    /**
     * Log Adapter, Used to record logs related to container operations
     * <p>
     * 日志适配器, 用于记录容器操作相关日志
     */
    private final LoggingAdapter loggingAdapter;

    /**
     * Store the mapping of Actor names to ActorRefs, using ConcurrentHashMap to ensure thread safety
     * <p>
     * 存储 Actor 名称到 ActorRef 的映射, 使用 ConcurrentHashMap 保证线程安全
     */
    private final Map<String, ActorRef> actors;


    public ActorContainer(ActorSystem system) {
        this.system = system;
        this.loggingAdapter = system().log();
        this.actors = new ConcurrentHashMap<>();
    }

    public ActorContainer(ActorSystem system, int capacity) {
        this.system = system;
        this.loggingAdapter = system().log();
        this.actors = new ConcurrentHashMap<>(capacity);
    }

    public ActorContainer(ActorSystem system, Map<String, ActorRef> actors) {
        this.system = system;
        this.loggingAdapter = system().log();
        this.actors = actors;
    }


    /**
     * Get the ActorSystem instance associated with the container
     * <p>
     * 获取容器关联的 ActorSystem 实例
     *
     * @return ActorSystem
     */
    public ActorSystem system() {
        return system;
    }

    /**
     * Get an unmodifiable collection of Actor Name-ActorRef key-value pairs
     * <p>
     * 获取不可修改的 Actor Name-ActorRef 键值对集合
     *
     * @return UnmodifiableMap
     */
    public Map<String, ActorRef> actors() {
        return Collections.unmodifiableMap(actors);
    }

    /**
     * Get an unmodifiable ActorRef collection
     * <p>
     * 获取不可修改的 ActorRef 集合
     *
     * @return UnmodifiableCollection
     */
    public Collection<ActorRef> values() {
        return Collections.unmodifiableCollection(actors.values());
    }

    /**
     * Get an unmodifiable collection of Actor Name-ActorRef key-value pairs
     * <p>
     * 获取不可修改的 Actor Name-ActorRef 键值对集合
     *
     * @return UnmodifiableSet
     */
    public Set<Map.Entry<String, ActorRef>> entrySet() {
        return Collections.unmodifiableSet(actors.entrySet());
    }

    /**
     * Iterate through all Actors in the container and perform a custom operation on each Actor.
     * <p>
     * 遍历容器中所有 Actor, 对每个 Actor 执行自定义操作
     *
     * @param action Callback
     */
    public void forEach(BiConsumer<String, ActorRef> action) {
        actors.forEach(action);
    }


    /**
     * Get the number of currently managed Actors in the container
     * <p>
     * 获取容器中当前管理的 Actor 数量
     *
     * @return ActorSize
     */
    public int size() {
        return actors.size();
    }

    /**
     * Determine whether the container is empty.
     * <p>
     * 判断容器是否为空
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return actors.isEmpty();
    }

    /**
     * 判断容器中是否包含指定名称的 ActorRef
     *
     * @param name ActorName
     * @return boolean
     */
    public boolean containsKey(String name) {
        return actors.containsKey(name);
    }

    /**
     * Determine whether the container contains the specified associated ActorRef
     * <p>
     * 判断容器中是否包含指定关联的 ActorRef
     *
     * @param value ActorRef
     * @return boolean
     */
    public boolean containsValue(ActorRef value) {
        return actors.containsValue(value);
    }


    /**
     * Find ActorRef by Name
     * <p>
     * 根据名称查找 ActorRef
     *
     * @param name ActorName
     * @return ActorRefOptional
     */
    public Optional<ActorRef> get(String name) {
        return Optional.ofNullable(actors.get(name));
    }


    /**
     * Remove the Actor with the specified name and stop it to release resources
     * <p>
     * 移除指定名称的 Actor,并停止该 Actor 以释放资源
     *
     * @param name ActorName
     */
    public void remove(String name) {
        ActorRef ref = actors.remove(name);
        if (Objects.isNull(ref)) {
            loggingAdapter.warning("Attempted to remove non-existent Actor [{}]", name);
        } else {
            // stop
            loggingAdapter.info("Actor [{}] has been removed from the container and stopped, path: {}", name, ref.path());
            system.stop(ref);
        }
    }

    /**
     * Stop all Actors in the container and clear all internal ActorRefs
     * <p>
     * 停止容器中所有 Actor, 并清空内部所有 ActorRef
     */
    public void clear() {
        // stop all Actor
        actors.values().forEach(ref -> {
            loggingAdapter.info("Actor [{}] has been stopped", ref.path().name());
            system.stop(ref);
        });
        // clear
        actors.clear();
        loggingAdapter.info("Container has been cleared, number of currently managed Actors: 0");
    }

    /**
     * Force delete the internal ActorRef of the ActorSystem, regardless of whether it is in the container list; this operation will iterate through all elements, so caution is required
     * <p>
     * 强制删除 ActorSystem 内部 ActorRef, 不管是否在容器列表中; 这里会遍历所有元素, 需要谨慎操作
     *
     * @param ref ActorRef
     */
    public void forget(ActorRef ref) {
        for (Map.Entry<String, ActorRef> entry : actors.entrySet()) {
            if (entry.getValue().equals(ref)) {
                actors.remove(entry.getKey());
            }
        }
        loggingAdapter.info("Actor [{}] has been stopped", ref.path().name());
        system.stop(ref);
    }


    /**
     * Create an Actor based on its name and Props, and register it in the container
     * <p>
     * 根据名称和 Props 创建 Actor, 并注册到容器中
     *
     * @param name  ActorName
     * @param props ActorProps
     * @return ActorRef
     */
    public ActorRef actorOf(String name, Props props) {
        return actors.compute(name, (key, oldActorRef) -> {
            if (!Objects.isNull(oldActorRef)) {
                system.stop(oldActorRef);
                loggingAdapter.info("Old instance of Actor [{}] has been stopped, preparing to create a new instance", name);
            }
            // create & register
            ActorRef newActorRef = system.actorOf(props, name);
            loggingAdapter.info("Actor [{}] created successfully, path: {}", name, newActorRef.path());
            return newActorRef;
        });
    }


    /**
     * 根据名称、Actor 类和构造参数创建 Actor 注册到容器中
     *
     * @param name  ActorName
     * @param actor ActorClass
     * @param <T>   Actor
     * @return ActorRef
     */
    public <T extends Actor> ActorRef actorOf(String name, Class<T> actor) {
        return actorOf(name, Props.create(actor));
    }


    /**
     * 根据名称、Actor 类和工厂方法创建 Actor, 并注册到容器中
     *
     * @param name    ActorName
     * @param actor   ActorClass
     * @param creator Factory
     * @param <T>     Actor
     * @return ActorRef
     */
    public <T extends Actor> ActorRef actorOf(String name, Class<T> actor, Supplier<T> creator) {
        return actorOf(name, Props.create(actor, () -> creator.get()));
    }


    /**
     * Create an Actor based on its name, the Actor class, and constructor parameters, and register it in the container.
     * <p>
     * 向指定名称的 Actor 发送消息, 并指定发送者, 如果返回为 Optional.empty 代表不存在注册的 Actor
     *
     * @param name    ActorName
     * @param message ActorMessage
     * @param sender  ActorSender
     * @return ActorOptional
     */
    public Optional<ActorRef> tell(String name, Object message, ActorRef sender) {
        ActorRef ref = actors.get(name);
        if (Objects.isNull(ref)) {
            loggingAdapter.warning("Failed to send message to non-existent Actor [{}]", name);
            return Optional.empty();
        }
        ref.tell(message, sender);
        loggingAdapter.debug("Message sent to Actor [{}]: {}", name, message);
        return Optional.of(ref);
    }


    /**
     * Send a message to the Actor with the specified name
     * <p>
     * 向指定名称的 Actor 发送消息
     *
     * @param name    ActorName
     * @param message ActorMessage
     * @return ActorOptional
     */
    public Optional<ActorRef> tell(String name, Object message) {
        return tell(name, message, ActorRef.noSender());
    }


    /**
     * Forward the message to the Actor with the specified name, while retaining the original sender's information
     * <p>
     * 转发消息到指定名称的 Actor, 保留原始发送者信息
     *
     * @param name    ActorName
     * @param message ActorMessage
     * @param ctx     ActorContext
     * @return ActorOptional
     */
    public Optional<ActorRef> forward(String name, Object message, ActorContext ctx) {
        ActorRef ref = actors.get(name);
        if (Objects.isNull(ref)) {
            loggingAdapter.warning("Failed to forward message to non-existent Actor [{}]", name);
            return Optional.empty();
        }
        ref.forward(message, ctx);
        loggingAdapter.debug("Message forwarded to Actor [{}]: {}", name, message);
        return Optional.of(ref);
    }


    /**
     * Monitor an Actor with a specified name, and you will receive a Terminated message when the Actor terminates
     * <p>
     * 监控指定名称的 Actor, 当 Actor 终止时会收到 Terminated 消息
     *
     * @param name ActorName
     * @param ctx  ActorContext
     * @return ActorOptional
     */
    public Optional<ActorRef> watch(String name, ActorContext ctx) {
        ActorRef ref = actors.get(name);
        if (Objects.isNull(ref)) {
            loggingAdapter.warning("Failed to watch non-existent Actor [{}]", name);
            return Optional.empty();
        }
        ctx.watch(ref);
        loggingAdapter.info("Started watching Actor [{}]", name);
        return Optional.of(ref);
    }


    /**
     * Unbind lifecycle tracking for the Actor with the specified name
     * <p>
     * 解绑指定名称的 Actor 生命周期跟踪
     *
     * @param name ActorName
     * @param ctx  ActorContext
     * @return ActorOptional
     */
    public Optional<ActorRef> unwatch(String name, ActorContext ctx) {
        ActorRef ref = actors.get(name);
        if (Objects.isNull(ref)) {
            loggingAdapter.warning("Failed to unwatch non-existent Actor [{}]", name);
            return Optional.empty();
        }
        ctx.unwatch(ref);
        loggingAdapter.info("Stopped watching Actor [{}]", name);
        return Optional.of(ref);
    }


    /**
     * Broadcast a message to all Actors in the container and specify the sender
     * <p>
     * 向容器中所有 Actor 广播消息, 并指定发送者
     *
     * @param message ActorMessage
     * @param sender  ActorSender
     */
    public void broadcast(Object message, ActorRef sender) {
        int count = actors.size();
        loggingAdapter.info("Started broadcasting message to {} Actors: {}", count, message);
        actors.forEach((name, ref) -> ref.tell(message, sender));
    }


    /**
     * Broadcast a message to all Actors in the container
     * <p>
     * 向容器中所有 Actor 广播消息
     *
     * @param message ActorMessage
     */
    public void broadcast(Object message) {
        broadcast(message, ActorRef.noSender());
    }


    /**
     * Generate an Actor managed by the Quarkus-CDI container
     * <p>
     * 生成支持 Quarkus-CDI 容器管理的 Actor
     *
     * @param name  ActorName
     * @param actor ActorClass
     * @param <T>   Actor
     * @return ActorRef
     */
    public <T extends Actor> ActorRef injectOf(String name, Class<T> actor) {
        return actorOf(name, PropsFactory.create(actor));
    }

    /**
     * Generate an Actor managed by the Quarkus-CDI container
     * <p>
     * 生成支持 Quarkus-CDI 容器管理的 Actor
     *
     * @param name    ActorName
     * @param actor   ActorClass
     * @param creator ActorSupplier
     * @param <T>     Actor
     * @return ActorRef
     */
    public <T extends Actor> ActorRef injectOf(String name, Class<T> actor, Supplier<T> creator) {
        return actorOf(name, PropsFactory.create(actor, creator));
    }

}
