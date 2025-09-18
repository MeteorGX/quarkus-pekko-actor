# Quarkus Pekko Actor

[![Build](https://github.com/MeteorGX/quarkus-pekko-actor/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/MeteorGX/quarkus-pekko-actor/actions?query=workflow%3ABuild)

[![License](https://img.shields.io/github/license/MeteorGX/quarkus-pekko-actor)](http://www.apache.org/licenses/LICENSE-2.0)

A Quarkus extension for integrating Apache Pekko Actors, enabling reactive and distributed actor-based applications with
Quarkus's container-first approach.

## Features

- Seamless integration of Apache Pekko Actors with Quarkus
- Support for reactive programming models
- Optimized for container environments (Docker, Kubernetes)
- Native image compilation support via GraalVM
- Simplified configuration and deployment

## Prerequisites

- Java 17 or higher
- Maven 3.8+ or Gradle 7.5+
- Quarkus 3.26.3+
- Apache Pekko 1.2.0+

## Installation

Add the following dependency to your Quarkus project's `pom.xml`:

```xml

<dependency>
    <groupId>io.fortress.quarkus</groupId>
    <artifactId>pekko-actor</artifactId>
    <version>{version}</version>
</dependency>
```

## Usage

1. **Create an Actor**:

```java
import org.apache.pekko.actor.AbstractActor;

public class MyActor extends AbstractActor {

    final LoggingAdapter log = context().system().log();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    log.info("hello: {}", msg);
                })
                .build();
    }
}
```

2. **Create Quarkus Main**:

```java
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * <a href="https://cn.quarkus.io/guides/lifecycle">Quarkus Document</a>
 */
@QuarkusMain
public class ExtensionApplication {
    public static void main(String[] args) {
        Quarkus.run(args);
    }
}
```

3. **Initialize Actor System**:

```java
import io.fortress.quarkus.pekko.actor.extension.ActorContainer;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;

import java.time.Duration;

@ApplicationScoped
public class ActorBootstrap {

    @Inject
    ActorContainer container;

    /**
     * Started Event
     */
    void onStart(@Observes StartupEvent ev) {
        ActorRef ref = container.injectOf("MyActor", MyActor.class);

        ActorSystem system = container.system();
        system.scheduler().scheduleWithFixedDelay(
                Duration.ofSeconds(3),
                Duration.ofSeconds(3),
                ref,
                "MeteorCat",
                system.dispatcher(),
                ActorRef.noSender()
        );
        system.registerOnTermination(timeId::cancel);
    }
}
```

## Configuration

```properties
#####################################################################
# simple actor
#####################################################################
quarkus.application.name=fortress-actor
quarkus.actor.name=${quarkus.application.name}
quarkus.actor.override-executor=true
quarkus.actor.reference=reference
# quarkus.actor.settings.pekko.actor.provider=local
#####################################################################
# cluster with protobuf-3
# require: pekko-remote, pekko-cluster or pekko-cluster-sharding
#####################################################################
quarkus.actor.settings.pekko.actor.provider=cluster
quarkus.actor.settings.pekko.remote.artery.enabled=on
quarkus.actor.settings.pekko.remote.artery.canonical.hostname=127.0.0.1
quarkus.actor.settings.pekko.remote.artery.canonical.port=2550
quarkus.actor.settings.pekko.actor.serializers.proto=org.apache.pekko.remote.serialization.ProtobufSerializer
quarkus.actor.settings.pekko.actor.serialization-bindings."com.google.protobuf.Message"=proto
quarkus.actor.settings.pekko.cluster.seed-nodes.0=pekko://${quarkus.actor.name}@127.0.0.1:2550
# quarkus.actor.settings.pekko.cluster.seed-nodes.1=pekko://${quarkus.actor.name}@127.0.0.1:2551
quarkus.actor.settings.pekko.cluster.downing-provider-class=org.apache.pekko.cluster.sbr.SplitBrainResolverProvider
quarkus.actor.settings.pekko.cluster.split-brain-resolver.active-strategy=keep-majority
```

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.