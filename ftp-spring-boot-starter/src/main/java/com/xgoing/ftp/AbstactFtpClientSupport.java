package com.xgoing.ftp;

import com.xgoing.ftp.exception.FtpNoConnectException;
import com.xgoing.ftp.exception.FtpRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * abstract FtpClient.
 */
public abstract class AbstactFtpClientSupport implements FtpClient {
    private static final String PATH_REX = ".*([\\\\|/]*).*";
    //登录用户名
    private String username;
    //登录密码
    private String password;
    //sftp密钥
    private String priKeyPath;
    //ftp server host
    private String host;
    //ftp service port
    private int port;
    //权重(1-100)
    private int weight;
    //当前权重
    private int currentWeight;

    public AbstactFtpClientSupport(String host, int port, String username, String password, int weight) {
        this(host, port, username, password, null, weight);
    }

    public AbstactFtpClientSupport(String host, int port, String username, String password, String priKeyPath, int weight) {
        this.username = username;
        this.password = password;
        this.priKeyPath = priKeyPath;
        this.host = host;
        this.port = port;
        if (weight >= 1 && weight <= 100) {
            this.weight = weight;
            this.currentWeight = weight;
        }
        init();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPriKeyPath() {
        return priKeyPath;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    /**
     * 初始化.
     */
    protected abstract void init();

    /**
     * 建立连接与登录.
     * <p>在无法正常建立连接或登录的情况下</>
     */
    protected abstract void connectAndLogin() throws FtpNoConnectException;

    /**
     * 登出与断开连接.
     */
    protected abstract void logoutAndClose() throws FtpNoConnectException;

    @Override
    public int upload(String pathname, String filename, InputStream inputStream) {
        assertEmpty(pathname, filename, inputStream);
        pathname = this.parsePath(pathname);
        connectAndLogin();
        try {
            return doUpload(pathname, filename, inputStream);
        } catch (Exception e) {
            throw e;
        } finally {
            logoutAndClose();
        }
    }

    @Override
    public List<String> uploadBatch(String pathname, Map<String, InputStream> fileMap) {
        assertEmpty(pathname, fileMap);
        connectAndLogin();
        try {
            List<String> rest = new ArrayList<String>(fileMap.size());
            for (Map.Entry<String, InputStream> entry : fileMap.entrySet()) {
                String filename = entry.getKey();
                int ret = upload(pathname, filename, entry.getValue());
                if (ret == REST_OK) {
                    rest.add(filename);
                }
            }
            return rest;
        } catch (Exception e) {
            throw e;
        } finally {
            logoutAndClose();
        }
    }

    protected abstract int doUpload(String pathname, String filename, InputStream inputStream);

    @Override
    public int download(String pathname, String filename, OutputStream outputStream) {
        return this.download(pathname, filename, outputStream, true);
    }

    @Override
    public int download(String pathname, String filename, OutputStream outputStream, boolean closeOutputStream) {
        assertEmpty(pathname, filename, outputStream);
        pathname = this.parsePath(pathname);
        connectAndLogin();
        try {
            return doDownload(pathname, filename, outputStream);
        } catch (Exception e) {
            throw e;
        } finally {
            logoutAndClose();
            if (closeOutputStream) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new FtpRuntimeException(e);
                }
            }
        }
    }

    protected abstract int doDownload(String pathname, String filename, OutputStream outputStream);

    @Override
    public List<String> list(String pathname) {
        assertEmpty(pathname);
        pathname = this.parsePath(pathname);
        connectAndLogin();
        try {
            return doList(pathname);
        } catch (Exception e) {
            throw e;
        } finally {
            logoutAndClose();
        }
    }

    protected abstract List<String> doList(String pathname);

    @Override
    public int delete(String pathname) {
        assertEmpty(pathname);
        pathname = this.parsePath(pathname);
        connectAndLogin();
        try {
            return doDelete(pathname);
        } catch (Exception e) {
            throw e;
        } finally {
            logoutAndClose();
        }
    }

    protected abstract int doDelete(String pathname);

    @Override
    public int rename(String source, String target) {
        assertEmpty(source, target);
        source = this.parsePath(source);
        target = this.parsePath(target);
        connectAndLogin();
        try {
            return doRename(source, target);
        } catch (Exception e) {
            throw e;
        } finally {
            logoutAndClose();
        }
    }

    protected abstract int doRename(String source, String target);

    @Override
    public int mkdir(String pathname) {
        assertEmpty(pathname);
        pathname = this.parsePath(pathname);
        connectAndLogin();
        try {
            return doMkdir(pathname);
        } catch (Exception e) {
            throw e;
        } finally {
            logoutAndClose();
        }
    }

    protected abstract int doMkdir(String pathname);

    @Override
    public boolean existDir(String pathname) {
        assertEmpty(pathname);
        pathname = this.parsePath(pathname);
        connectAndLogin();
        try {
            return checkExistDir(pathname);
        } catch (Exception e) {
            throw e;
        } finally {
            logoutAndClose();
        }
    }

    protected abstract boolean checkExistDir(String pathname);

    @Override
    public int getWeight() {
        return this.weight;
    }

    /**
     * 验证参数是否为空.
     *
     * @param args
     */
    protected void assertEmpty(Object... args) {
        for (int i = 0; i < args.length; i++) {
            Object obj = args[i];
            if (obj == null) {
                throw new FtpRuntimeException("args index " + i + " is null!");
            }
            if (obj instanceof String && ((String) obj).trim().length() == 0) {
                throw new FtpRuntimeException("args index " + i + " is empty!");
            }
            if (obj instanceof Map<?, ?> && ((Map<?, ?>) obj).size() == 0) {
                throw new FtpRuntimeException("args index " + i + " is empty!");
            }
            if (obj instanceof Collection<?> && ((Collection<?>) obj).size() == 0) {
                throw new FtpRuntimeException("args index " + i + " is empty!");
            }
        }
    }

    /**
     * 目标目录格式转换.
     *
     * @param path
     * @return
     */
    protected String parsePath(String path) {
        if (path == null || !path.matches(PATH_REX)) {
            throw new FtpRuntimeException("path error");
        }
        String rest = path.replaceAll("\\\\+", UNIX_LIKE_SEPARATOR).replaceAll(UNIX_LIKE_SEPARATOR + "+", UNIX_LIKE_SEPARATOR);
        if (rest.startsWith(UNIX_LIKE_SEPARATOR)) {
            rest = rest.substring(1);
        }
        if (rest.endsWith(UNIX_LIKE_SEPARATOR)) {
            rest = rest.substring(0, rest.length() - 1);
        }
        return rest;
    }

    public int getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(int currentWeight) {
        this.currentWeight = currentWeight;
    }
}
