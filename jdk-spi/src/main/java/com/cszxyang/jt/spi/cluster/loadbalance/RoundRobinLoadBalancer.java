package com.cszxyang.jt.spi.cluster.loadbalance;

import com.cszxyang.jt.spi.cluster.Server;
import com.cszxyang.jt.spi.protocol.Request;
import com.cszxyang.jt.spi.util.MathUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer extends BaseLoadBalancer {

    private final AtomicInteger idx = new AtomicInteger(0);

    public RoundRobinLoadBalancer() {
    }

    public RoundRobinLoadBalancer(List<Server> serverList) {
        super(serverList);
    }

    protected Server doSelect(Request request) {
        int index = getNextNonNegative();
        for (int i = 0; i < serverList.size(); i++) {
            Server server = serverList.get((i + index) % serverList.size());
            if (server.isAvailable()) {
                return server;
            }
        }
        return null;
    }

    // get non-negative int
    private int getNextNonNegative() {
        return MathUtil.getNonNegative(idx.incrementAndGet());
    }
}
