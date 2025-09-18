package io.fortress.quarkus.pekko.actor.deployment;

import io.fortress.quarkus.pekko.actor.extension.ActorContainer;
import io.fortress.quarkus.pekko.actor.extension.PropsFactory;
import io.fortress.quarkus.pekko.actor.runtime.PekkoActorConfiguration;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.common.constraint.Assert;
import jakarta.inject.Inject;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.Props;
import org.apache.pekko.testkit.TestKit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

class PekkoActorProcessorTest {

    /**
     * Register the test framework
     * <p>
     * 注册测试框架
     */
    @RegisterExtension
    static final QuarkusUnitTest quarkusUnitTest = new QuarkusUnitTest()
            .withConfigurationResource("application.properties");


    /**
     * Load runtime Actor configuration
     * <p>
     * 加载运行时的 Actor 配置
     */
    @Inject
    PekkoActorConfiguration configuration;


    /**
     * Load runtime ActorSystem
     * <p>
     * 加载运行时的 Actor System
     */
    @Inject
    ActorSystem actorSystem;

    /**
     * Verify Pekko configuration injection works correctly
     * <p>
     * 验证Pekko配置是否能正确注入
     */
    @Test
    public void testPekkoActorConfiguration() {
        Assert.assertNotNull(configuration);
    }

    /**
     * Verify Pekko ActorSystem injection works correctly
     * <p>
     * 验证Pekko ActorSystem是否能正确注入
     */
    @Test
    public void testActorSystem() {
        Assert.assertNotNull(actorSystem);
    }


    /**
     * basic test Actor
     */
    public static class BasicTestActor extends AbstractActor {

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(String.class, msg -> {
                        // 若存在发送方，则将消息回显回去
                        if (ActorRef.noSender() != sender()) {
                            sender().tell(msg, self());
                        }
                    })
                    .build();
        }
    }

    /**
     * test basic actor node
     */
    @Test
    public void testBasicTestActor() {
        ActorRef address = actorSystem.actorOf(Props.create(BasicTestActor.class), "BasicTestActor");
        TestKit testKit = new TestKit(actorSystem);

        String message = "hello.world";
        address.tell(message, testKit.testActor());

        String response = testKit.expectMsg(Duration.create(3, TimeUnit.SECONDS), message);
        Assert.assertNotNull(response);
        Assert.assertTrue(message.equals(response));
    }


    /**
     * inject test actor
     */
    public static class InjectTestActor extends AbstractActor {

        @Inject
        PekkoActorConfiguration configuration;

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(String.class, msg -> {
                        Assert.assertNotNull(configuration);
                        Assert.assertTrue(configuration.name().equals(msg));
                        getContext().getSystem().log().info("Inject ActorSystem: {}", configuration.name());

                        if (ActorRef.noSender() != sender()) {
                            sender().tell(msg, self());
                        }
                    })
                    .build();
        }
    }


    /**
     * test support @Inject actor
     */
    @Test
    public void testInjectActor() {
        ActorRef address = actorSystem.actorOf(PropsFactory.create(InjectTestActor.class), "InjectTestActor");
        TestKit testKit = new TestKit(actorSystem);

        String message = configuration.name();
        address.tell(message, testKit.testActor());

        String response = testKit.expectMsg(Duration.create(3, TimeUnit.SECONDS), message);
        Assert.assertNotNull(response);
        Assert.assertTrue(message.equals(response));
    }


    /**
     * Load runtime ActorContainer
     */
    @Inject
    ActorContainer container;


    /**
     * Verify ActorContainer injection works correctly
     */
    @Test
    public void testContainer() {
        Assert.assertNotNull(container);
    }


    /**
     * Test create actor in container
     */
    @Test
    public void testBasicTestActorContainer() {
        TestKit testKit = new TestKit(container.system());
        ActorRef basicTestActorInContainer = container.actorOf("BasicTestActorInContainer", BasicTestActor.class);

        String message = "hello.world";
        basicTestActorInContainer.tell(message, testKit.testActor());
        String basicTestActorInContainerResponse = testKit.expectMsg(Duration.create(3, TimeUnit.SECONDS), message);
        Assert.assertNotNull(basicTestActorInContainerResponse);
        Assert.assertTrue(message.equals(basicTestActorInContainerResponse));

        // support @Inject
        message = configuration.name();
        ActorRef InjectTestActorInContainer = container.injectOf("InjectTestActorInContainer", InjectTestActor.class);
        InjectTestActorInContainer.tell(message, testKit.testActor());
        String injectTestActorInContainerResponse = testKit.expectMsg(Duration.create(3, TimeUnit.SECONDS), message);
        Assert.assertNotNull(injectTestActorInContainerResponse);
        Assert.assertTrue(message.equals(injectTestActorInContainerResponse));
    }

}
