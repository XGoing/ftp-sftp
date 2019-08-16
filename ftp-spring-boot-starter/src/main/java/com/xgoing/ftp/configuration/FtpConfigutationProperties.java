package com.xgoing.ftp.configuration;

import com.xgoing.ftp.FtpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "ftp.client")
public class FtpConfigutationProperties {
    //是否单机模式
    private boolean standalone = true;
    //集群模式下自定义的负载均衡器
    private String balancer;
    //客户端连接服务端类型（ftp或者sftp）
    private String connectType = FtpClient.TYPE_FTP;
    //密钥路径
    private String privateKeyPath;
    //是否使用privateKey
    private boolean usePrivateKey;
    //连接信息列表
    private Set<FtpConnectInfo> connectInfoSet;

    public boolean isStandalone() {
        return standalone;
    }

    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

    public String getBalancer() {
        return balancer;
    }

    public void setBalancer(String balancer) {
        this.balancer = balancer;
    }

    public String getConnectType() {
        return connectType;
    }

    public void setConnectType(String connectType) {
        this.connectType = connectType;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public boolean isUsePrivateKey() {
        return usePrivateKey;
    }

    public void setUsePrivateKey(boolean usePrivateKey) {
        this.usePrivateKey = usePrivateKey;
    }

    public Set<com.xgoing.ftp.configuration.FtpConnectInfo> getConnectInfoSet() {
        return connectInfoSet;
    }

    public void setConnectInfoSet(Set<com.xgoing.ftp.configuration.FtpConnectInfo> connectInfoSet) {
        this.connectInfoSet = connectInfoSet;
    }
}
