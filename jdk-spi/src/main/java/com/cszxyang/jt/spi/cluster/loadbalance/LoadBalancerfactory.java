package com.cszxyang.jt.spi.cluster.loadbalance;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

public class LoadBalancerfactory {
    private LoadBalancerfactory() {
    }

    public static LoadBalancer getLoadBalancer() {
        ServiceLoader<LoadBalancer> loadBalancerList = ServiceLoader.load(LoadBalancer.class);
        final Optional<LoadBalancer> loadBalancer = StreamSupport.stream(loadBalancerList.spliterator(), false)
                .findFirst();
        return loadBalancer.orElse(null);
    }
}
