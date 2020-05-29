package com.cszxyang.jt.spi.cluster.loadbalance;

import com.cszxyang.jt.spi.cluster.Server;
import com.cszxyang.jt.spi.exception.CustomException;
import com.cszxyang.jt.spi.protocol.Request;

public interface LoadBalancer {
    Server select(Request request) throws CustomException;
}