package com.zeed.distributed.search.entry.config;


import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@Configuration
public class ZookeeperConfig implements Watcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperConfig.class);

    @Value("${zookeeper.host.url:localhost:2181}")
    private String zookeeperUrl;

    @Value("${zookeeper.timeout:300000}")
    private Integer zookeeperTimeout;

    CountDownLatch connectionLatch = new CountDownLatch(1);

    @Bean
    public ZooKeeper getZookeeper() throws IOException, InterruptedException {

        ZooKeeper zooKeeper = new ZooKeeper(zookeeperUrl, zookeeperTimeout, this);
        connectionLatch.await();
        return zooKeeper;
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                if (event.getState().equals(Event.KeeperState.SyncConnected)) {
                    LOGGER.info("Zookeeper connected");
                    connectionLatch.countDown();
                }
                break;

        }
    }
}
