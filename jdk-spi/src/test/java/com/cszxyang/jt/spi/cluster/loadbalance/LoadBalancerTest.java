package com.cszxyang.jt.spi.cluster.loadbalance;

import com.cszxyang.jt.spi.cluster.Server;
import com.cszxyang.jt.spi.exception.CustomException;
import com.cszxyang.jt.spi.protocol.Request;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LoadBalancerTest {

    private List<Server> servers;

    private BaseLoadBalancer loadBalancer;

    @Before
    public void prepare() {
        servers = new ArrayList<>();
        servers.add(new Server("127.0.0.1", 80));
        servers.add(new Server("127.0.0.1", 9090));
        servers.add(new Server("127.0.0.1", 443));
    }

    @Test
    public void test() {
        loadBalancer = new RoundRobinLoadBalancer(servers);
        try {
            Server selectedServer = loadBalancer.select(new Request());
        } catch (CustomException e) {
            e.printStackTrace();
        }
    }
}