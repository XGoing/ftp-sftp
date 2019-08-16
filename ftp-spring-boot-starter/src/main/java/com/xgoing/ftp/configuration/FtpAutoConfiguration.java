package com.xgoing.ftp.configuration;

import com.xgoing.ftp.FtpClient;
import com.xgoing.ftp.cluster.LoadBalanceFtpClient;
import com.xgoing.ftp.cluster.RoundSimpleLoadBalanceFtpClient;
import com.xgoing.ftp.exception.FtpException;
import com.xgoing.ftp.standalone.StandAloneFtpClient;
import com.xgoing.ftp.standalone.StandAloneSFtpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Configuration
@ConditionalOnClass({FtpClient.class, StandAloneFtpClient.class, StandAloneSFtpClient.class})
@EnableConfigurationProperties(FtpConfigutationProperties.class)
public class FtpAutoConfiguration {
    @Autowired
    private FtpConfigutationProperties configutationProperties;

    @Bean
    @ConditionalOnMissingBean
    public FtpClient ftpClient() throws FtpException {
        this.prepareCreateClient();
        return this.createClient();
    }

    /**
     * 准备配置.
     *
     * @throws FtpException
     */
    private void prepareCreateClient() throws FtpException {
        this.preCheck();
        this.setPriKey();
        this.setConnectType();
    }

    /**
     * 前置检查.
     *
     * @throws FtpException
     */
    private void preCheck() throws FtpException {
        Set<FtpConnectInfo> connectInfoSet = this.configutationProperties.getConnectInfoSet();
        if (connectInfoSet == null || connectInfoSet.size() == 0) {
            throw new FtpException("ftp client connect info not set");
        }
        String connectType = this.configutationProperties.getConnectType();
        if (connectType != null && connectType.trim().length() > 0) {
            connectType = connectType.trim().toLowerCase();
            if (!(FtpClient.TYPE_FTP.equals(connectType) || FtpClient.TYPE_SFTP.equals(connectType))) {
                throw new FtpException("ftp client connectType not supported");
            }
        }
    }

    /**
     * 设置私钥.
     */
    private void setPriKey() {
        Set<FtpConnectInfo> connectInfoSet = this.configutationProperties.getConnectInfoSet();
        boolean usePriKey = this.configutationProperties.isUsePrivateKey();
        String priKeyPath = this.configutationProperties.getPrivateKeyPath();
        if (usePriKey && priKeyPath != null && priKeyPath.trim().length() != 0) {
            Iterator<FtpConnectInfo> it = connectInfoSet.iterator();
            while (it.hasNext()) {
                FtpConnectInfo connectInfo = it.next();
                if (connectInfo.isUsePrivateKey() && !(connectInfo.getPrivateKeyPath() != null || connectInfo.getPrivateKeyPath().trim().length() > 0)) {
                    connectInfo.setPrivateKeyPath(priKeyPath);
                }
            }
        }
    }

    /**
     * 设置连接方式.
     */
    private void setConnectType() {
        Set<FtpConnectInfo> connectInfoSet = this.configutationProperties.getConnectInfoSet();
        String connectType = this.configutationProperties.getConnectType();
        //设置连接方式与默认值
        if (connectType != null && connectType.trim().length() > 0) {
            Iterator<FtpConnectInfo> it = connectInfoSet.iterator();
            connectType = connectType.trim().toLowerCase();
            while (it.hasNext()) {
                FtpConnectInfo connectInfo = it.next();
                //全局配置覆盖局部配置
                if (connectInfo.getConnectType() == null || connectInfo.getConnectType().trim().length() == 0) {
                    connectInfo.setConnectType(connectType);
                }
                //设置sftp默认的端口
                if (FtpClient.TYPE_SFTP.equals(connectType) && connectInfo.getPort() <= 0) {
                    connectInfo.setPort(FtpClient.PORT_SFTP);
                }
            }
        }
    }

    /**
     * 创建FtpClient.
     *
     * @return
     */
    private FtpClient createClient() throws FtpException {
        boolean standalone = this.configutationProperties.isStandalone();
        Set<FtpConnectInfo> connectInfoSet = this.configutationProperties.getConnectInfoSet();
        if (standalone) {
            return this.createStandalone(connectInfoSet.stream().findFirst().get());
        } else {
            return this.createCluster(connectInfoSet);
        }
    }

    /**
     * 单机.
     *
     * @param connectInfo
     * @return
     */
    private FtpClient createStandalone(FtpConnectInfo connectInfo) {
        String connectType = connectInfo.getConnectType();
        connectType = connectType == null || connectType.trim().length() == 0 ? FtpClient.TYPE_FTP : connectType.trim().toLowerCase();
        int weight = connectInfo.getWeight();
        if (FtpClient.TYPE_SFTP.equalsIgnoreCase(connectType)) {
            if (connectInfo.isUsePrivateKey()) {
                return new StandAloneSFtpClient(connectInfo.getHost(), connectInfo.getPort(), connectInfo.getUsername(), connectInfo.getPassword(), connectInfo.getPrivateKeyPath(), weight);
            }
            return new StandAloneSFtpClient(connectInfo.getHost(), connectInfo.getPort(), connectInfo.getUsername(), connectInfo.getPassword(), weight);
        }
        return new StandAloneFtpClient(connectInfo.getHost(), connectInfo.getPort(), connectInfo.getUsername(), connectInfo.getPassword(), weight);
    }

    /**
     * 集群.
     *
     * @param connectInfoSet
     * @return
     */
    private FtpClient createCluster(Set<FtpConnectInfo> connectInfoSet) throws FtpException {
        List<FtpClient> ftpClients = new ArrayList<>(connectInfoSet.size());
        connectInfoSet.forEach(connectInfo -> ftpClients.add(createStandalone(connectInfo)));
        String balancerClass = this.configutationProperties.getBalancer();
        LoadBalanceFtpClient lbfc = null;
        try {
            if (balancerClass != null && balancerClass.trim().length() > 0) {
                balancerClass = balancerClass.trim();
                lbfc = (LoadBalanceFtpClient) Class.forName(balancerClass).newInstance();
                lbfc.initFtpClients(ftpClients);
            } else {
                lbfc = new RoundSimpleLoadBalanceFtpClient();
                lbfc.initFtpClients(ftpClients);
            }
            return lbfc;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new FtpException(e);
        }
    }
}
