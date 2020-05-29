package com.cszxyang.jt.spi.cluster.loadbalance;

import com.cszxyang.jt.spi.cluster.Server;
import com.cszxyang.jt.spi.exception.CustomException;
import com.cszxyang.jt.spi.protocol.Request;
import org.apache.commons.collections4.CollectionUtils;
import java.util.List;

public abstract class BaseLoadBalancer implements LoadBalancer {

    protected List<Server> serverList;

    public BaseLoadBalancer() {
    }

    public BaseLoadBalancer(List<Server> serverList) {
        this.serverList = serverList;
    }

    public Server select(Request request) throws CustomException {
        if (CollectionUtils.isEmpty(serverList)) {
            throw new CustomException("empty server list");
        }
        Server server = null;
        if (serverList.size() == 1) {
            return serverList.get(0).isAvailable() ? serverList.get(0) : null;
        } else if (serverList.size() > 1) {
            server = doSelect(request);
        }
        if (server != null) {
            return server;
        }
        throw new CustomException(this.getClass().getSimpleName() +
                " No available servers for call request:" + request);
    }

    /**
     * 从 serverList 中选择
     *
     * @param request 请求
     * @return 选中的服务器
     */
    protected abstract Server doSelect(Request request);
}
