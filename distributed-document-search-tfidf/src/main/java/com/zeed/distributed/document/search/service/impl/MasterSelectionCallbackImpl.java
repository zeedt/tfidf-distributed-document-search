package com.zeed.distributed.document.search.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeed.distributed.document.search.registry.ServiceRegistry;
import com.zeed.distributed.document.search.service.MasterSelectionCallback;
import com.zeed.distributed.document.search.config.Constant;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MasterSelectionCallbackImpl implements MasterSelectionCallback {

    @Autowired
    private ZooKeeper zooKeeper;

    @Autowired
    private ServiceRegistry serviceRegistry;

    @Value("${server.port}")
    private Integer port;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(MasterSelectionCallbackImpl.class);

    @Override
    public void onMasterSelected(String currentNode) throws IOException, KeeperException, InterruptedException {
        zooKeeper.setData(Constant.SERVICE_REGISTRY_NODE, OBJECT_MAPPER.writeValueAsBytes("http://localhost:"+port), -1);
        serviceRegistry.updateWorkerNodesInRegistry();
    }

    @Override
    public void onWorkerSelected() {
        // TODO: Perform other things necessary
//        serviceRegistry.updateWorkerNodesInRegistry();
    }
}
