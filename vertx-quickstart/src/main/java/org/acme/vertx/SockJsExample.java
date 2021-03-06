package org.acme.vertx;

import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class SockJsExample {

    @Inject Vertx vertx;

    public void init(@Observes Router router) {
        BridgeOptions opts = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddress("ticks"));

        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        sockJSHandler.bridge(opts);
        router.route("/eventbus/*").handler(sockJSHandler);

        AtomicInteger counter = new AtomicInteger();
        vertx.setPeriodic(1000,
            ignored -> vertx.eventBus().publish("ticks", counter.getAndIncrement()));
    }

}
