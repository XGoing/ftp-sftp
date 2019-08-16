package com.xgoing.ftp.cluster;

import com.xgoing.ftp.FtpClient;

import java.util.List;

/**
 * 具备负载均衡的FtpClient.
 */
public interface LoadBalanceFtpClient extends FtpClient {
    /**
     * 简单轮训.
     */
    String LOAD_STRATEGY_ROUND_SIMPLE = "round-simple";
    /**
     * 加权轮训.
     */
    String LOAD_STRATEGY_ROUND_WEIGHT = "round-weight";

    /**
     * 获取负载均衡策略.
     *
     * @return
     */
    String getLoadStrategy();

    /**
     * 初始化FtpClient集群.
     *
     * @param ftpClients
     */
    void initFtpClients(List<FtpClient> ftpClients);

    /**
     * 获取FtpClient.
     *
     * @return
     */
    FtpClient getFtpClient();

    default int getWeight() {
        return 0;
    }
}
