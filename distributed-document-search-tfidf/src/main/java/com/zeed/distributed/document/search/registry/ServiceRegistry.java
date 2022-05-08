package com.zeed.distributed.document.search.registry;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeed.distributed.document.search.config.Constant;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
public class ServiceRegistry implements Watcher {

    private String currentNode;
    private List<String> allChildrenNodes = null;
    @Autowired
    private ZooKeeper zooKeeper;
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @PostConstruct
    public void init() throws KeeperException, InterruptedException, JsonProcessingException {
        if (this.zooKeeper.exists(Constant.SERVICE_REGISTRY_NODE, false) == null)
            this.zooKeeper.create(Constant.SERVICE_REGISTRY_NODE,
                    OBJECT_MAPPER.writeValueAsBytes(new ArrayList<>()), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
    }

    public synchronized void updateWorkerNodesInRegistry() {
        try {
            List<String> availableNodes = new ArrayList<>();
            List<String> children = this.zooKeeper.getChildren(Constant.WORKER_NODE, this);
            Collections.sort(children);
            for (int i = 1; i < children.size(); i++) { // Exclude the master node
                String child = children.get(i);
                if (this.zooKeeper.exists(Constant.WORKER_NODE + "/" + child, null) != null) {
                    byte[] data = this.zooKeeper.getData(Constant.WORKER_NODE + "/" + child, false, null);
                    availableNodes.add("http://localhost:" + new String(data));
                }
            }
            allChildrenNodes = availableNodes;
            try {
                LOGGER.info("Current available nodes are : " + OBJECT_MAPPER.writeValueAsString(allChildrenNodes));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent event) {
        updateWorkerNodesInRegistry();
    }

    public List<String> getAllChildrenNodes() {
        return allChildrenNodes;
    }
}
