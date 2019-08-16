package com.xgoing.ftp.configuration;

import java.util.Objects;

/**
 * ftp(sftp)连接信息.
 */
public class FtpConnectInfo {
    //服务主机
    private String host;
    //服务端口
    private int port;
    //登录用户名
    private String username;
    //登录密码
    private String password;
    //密钥
    private String privateKeyPath;
    //是否使用privateKey
    private boolean usePrivateKey = false;
    //客户端连接服务端类型（ftp或者sftp）
    private String connectType;
    //权重
    private int weight;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getConnectType() {
        return connectType;
    }

    public void setConnectType(String connectType) {
        this.connectType = connectType;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FtpConnectInfo that = (FtpConnectInfo) o;
        return port == that.port &&
                host.equals(that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return "FtpConnectInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", privateKeyPath='" + privateKeyPath + '\'' +
                ", usePrivateKey=" + usePrivateKey +
                ", connectType='" + connectType + '\'' +
                '}';
    }
}
