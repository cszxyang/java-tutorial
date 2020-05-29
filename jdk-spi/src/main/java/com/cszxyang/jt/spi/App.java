package com.cszxyang.jt.spi;

import com.cszxyang.jt.spi.cluster.Server;
import com.cszxyang.jt.spi.cluster.loadbalance.LoadBalancer;
import com.cszxyang.jt.spi.cluster.loadbalance.LoadBalancerfactory;
import com.cszxyang.jt.spi.exception.CustomException;
import com.cszxyang.jt.spi.protocol.Request;

public class App {
    public static void main(String[] args) throws CustomException {
        LoadBalancer loadBalancer = LoadBalancerfactory.getLoadBalancer();
        for (int i = 0; i < 100; i++) {
            Server select = loadBalancer.select(new Request());
            System.out.println(select);
        }
    }
}