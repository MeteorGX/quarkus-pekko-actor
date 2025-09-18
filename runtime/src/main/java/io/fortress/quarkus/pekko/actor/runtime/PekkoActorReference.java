package io.fortress.quarkus.pekko.actor.runtime;


import com.typesafe.config.ConfigFactory;

/**
 * io.fortress.quarkus.pekko.actor.runtime.PekkoActorReference.java
 * <p>
 * An enumeration of Pekko configuration loading types, corresponding to the four default loading methods provided by {@link ConfigFactory}, for switching base configurations across scenarios.
 * <p>
 * Enum to ConfigFactory method mapping:
 * - Reference: Pekko's default reference config ({@link ConfigFactory#defaultReference()})
 * - Application: App-specific custom config ({@link ConfigFactory#defaultApplication()})
 * - Overrides: Override config (highest priority, temp overrides, {@link ConfigFactory#defaultOverrides()})
 * - ReferenceUnresolved: Unresolved default reference config (needs manual resolve(), {@link ConfigFactory#defaultReferenceUnresolved()})
 * <p>
 * Pekko配置加载类型的枚举, 对应{@link ConfigFactory}提供的四种默认配置加载方式, 用于在不同场景下切换基础配置。
 * <p>
 * 枚举值与ConfigFactory方法的映射关系：
 * - Reference: 加载Pekko框架的默认参考配置（{@link ConfigFactory#defaultReference()}）；
 * - Application: 加载应用程序特定的自定义配置（{@link ConfigFactory#defaultApplication()}）；
 * - Overrides: 加载覆盖配置（优先级最高，用于临时覆盖默认配置，{@link ConfigFactory#defaultOverrides()}）；
 * - ReferenceUnresolved: 加载未解析的默认参考配置（需要手动调用resolve()方法，{@link ConfigFactory#defaultReferenceUnresolved()}）。
 */
public enum PekkoActorReference {

    /**
     * Corresponds to {@link ConfigFactory#defaultReference()}, Loads Pekko framework's built-in default reference configuration.
     * <p>
     * 对应{@link ConfigFactory#defaultReference()}, 加载Pekko框架内置的默认参考配置。
     */
    Reference,

    /**
     * Corresponds to {@link ConfigFactory#defaultApplication()}, Loads application-level custom configuration (e.g., application.conf).
     * <p>
     * 对应{@link ConfigFactory#defaultApplication()}, 加载应用级别的自定义配置（例如application.conf）。
     */
    Application,

    /**
     * Corresponds to {@link ConfigFactory#defaultOverrides()}, Loads override configuration (higher priority than Reference and Application).
     * <p>
     * 对应{@link ConfigFactory#defaultOverrides()}, 加载覆盖配置（优先级高于Reference和Application）。
     */
    Overrides,

    /**
     * Corresponds to {@link ConfigFactory#defaultReferenceUnresolved()}, Loads unresolved default reference configuration (requires manual resolution later).
     * <p>
     * 对应{@link ConfigFactory#defaultReferenceUnresolved()}, 加载未解析的默认参考配置（需要后续手动解析）。
     */
    ReferenceUnresolved
}
