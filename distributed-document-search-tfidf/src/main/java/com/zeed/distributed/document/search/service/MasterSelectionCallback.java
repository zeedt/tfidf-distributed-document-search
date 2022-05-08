package com.zeed.distributed.document.search.service;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;

public interface MasterSelectionCallback {

    void onMasterSelected(String currentNode) throws IOException, KeeperException, InterruptedException;

    void onWorkerSelected() throws KeeperException, InterruptedException, IOException;

}
