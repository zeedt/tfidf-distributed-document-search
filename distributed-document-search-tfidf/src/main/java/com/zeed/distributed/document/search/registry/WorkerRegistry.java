package com.zeed.distributed.document.search.registry;

import com.zeed.distributed.document.search.service.MasterSelectionCallback;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class WorkerRegistry implements Watcher {

    private String currentNode;
    private static final String WORKER_NODE = "/workers";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerRegistry.class);
    @Value("${server.port}")
    private String port;
    @Autowired
    private ZooKeeper zooKeeper;
    @Autowired
    private MasterSelectionCallback masterSelectionCallback;

    @PostConstruct
    public void init() throws KeeperException, InterruptedException, IOException {
        if (this.zooKeeper.exists(WORKER_NODE, false) == null)
            this.zooKeeper.create(WORKER_NODE, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
        String currentPath = this.zooKeeper.create(WORKER_NODE + "/no_", port.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        this.currentNode = currentPath.replace(WORKER_NODE + "/", "");
        reElectMaster();
    }

    private synchronized void reElectMaster() throws KeeperException, InterruptedException, IOException {
        Stat predecessorStat = null;
        while (predecessorStat == null) {
            List<String> children = this.zooKeeper.getChildren(WORKER_NODE, false);
            Collections.sort(children);
            String master = children.get(0);
            if (master.equalsIgnoreCase(currentNode)) {
                LOGGER.info("I am the current leader");
                masterSelectionCallback.onMasterSelected(currentNode);
                return;
            }
            LOGGER.info("The current leader is => " + master);
            // Make each node monitor the node before them
            int indexOfCurrentNode = Collections.binarySearch(children, currentNode);
            String predecessorNode = children.get(indexOfCurrentNode-1);
            predecessorStat = this.zooKeeper.exists(WORKER_NODE+"/"+predecessorNode, this);
            // Add address to the service registry
            masterSelectionCallback.onWorkerSelected();
        }
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case NodeChildrenChanged:
                System.out.println("Node children changed");
                break;
            case NodeDeleted:
                try {
                    reElectMaster();
                } catch (KeeperException | IOException | InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }
}
