package com.cszxyang.jt.spi.cluster.loadbalance;

import com.cszxyang.jt.spi.cluster.Server;
import com.cszxyang.jt.spi.protocol.Request;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLoadBalancer extends BaseLoadBalancer {

    protected Server doSelect(Request request) {
        int idx = (int) (ThreadLocalRandom.current().nextDouble() * serverList.size());
        for (int i = 0; i < serverList.size(); i++) {
            Server server = serverList.get((i + idx) % serverList.size());
            if (server.isAvailable()) {
                return server;
            }
        }
        return null;
    }
}
