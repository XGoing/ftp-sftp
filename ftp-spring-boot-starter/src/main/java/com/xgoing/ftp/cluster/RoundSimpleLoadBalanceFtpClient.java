package com.xgoing.ftp.cluster;

import com.xgoing.ftp.FtpClient;
import com.xgoing.ftp.exception.FtpNoConnectException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 轮训策略的负载均衡ftp/sftp客户端.
 */
public class RoundSimpleLoadBalanceFtpClient implements LoadBalanceFtpClient {
    private List<FtpClient> ftpClients;
    private int index = 0;
    private int limit = 0;
    private final Object lock = new Object();

    @Override
    public int upload(String pathname, String filename, InputStream inputStream) {
        try {
            return this.getFtpClient().upload(pathname, filename, inputStream);
        } catch (FtpNoConnectException e) {
            return this.upload(pathname, filename, inputStream);
        }
    }

    @Override
    public List<String> uploadBatch(String pathname, Map<String, InputStream> fileMap) {
        try {
            return this.getFtpClient().uploadBatch(pathname, fileMap);
        } catch (FtpNoConnectException e) {
            return this.uploadBatch(pathname, fileMap);
        }
    }

    @Override
    public int download(String pathname, String filename, OutputStream outputStream) {
        try {
            return this.getFtpClient().download(pathname, filename, outputStream);
        } catch (FtpNoConnectException e) {
            return this.download(pathname, filename, outputStream);
        }
    }

    @Override
    public int download(String pathname, String filename, OutputStream outputStream, boolean closeOutputStream) {
        try {
            return this.getFtpClient().download(pathname, filename, outputStream, closeOutputStream);
        } catch (FtpNoConnectException e) {
            return this.download(pathname, filename, outputStream, closeOutputStream);
        }
    }

    @Override
    public List<String> list(String pathname) {
        try {
            return this.getFtpClient().list(pathname);
        } catch (FtpNoConnectException e) {
            return this.list(pathname);
        }
    }

    @Override
    public int delete(String pathname) {
        try {
            return this.getFtpClient().delete(pathname);
        } catch (FtpNoConnectException e) {
            return this.delete(pathname);
        }
    }

    @Override
    public int rename(String oldPathname, String newPathname) {
        try {
            return this.getFtpClient().rename(oldPathname, newPathname);
        } catch (FtpNoConnectException e) {
            return this.rename(oldPathname, newPathname);
        }
    }

    @Override
    public int mkdir(String pathname) {
        try {
            return this.getFtpClient().mkdir(pathname);
        } catch (FtpNoConnectException e) {
            return this.mkdir(pathname);
        }
    }

    @Override
    public boolean existDir(String pathname) {
        try {
            return this.getFtpClient().existDir(pathname);
        } catch (FtpNoConnectException e) {
            return this.existDir(pathname);
        }
    }

    @Override
    public FtpClient getFtpClient() {
        if (limit == 0) {
            throw new RuntimeException("no available FtpClient!");
        }
        synchronized (lock) {
            if (index < limit) {
                return this.ftpClients.get(index++);
            } else {
                index = 0;
                return getFtpClient();
            }
        }
    }

    @Override
    public String getLoadStrategy() {
        return LOAD_STRATEGY_ROUND_SIMPLE;
    }

    @Override
    public void initFtpClients(List<FtpClient> ftpClients) {
        this.ftpClients = ftpClients;
        this.limit = this.ftpClients != null && this.ftpClients.size() > 0 ? this.ftpClients.size() : 0;
    }
}
