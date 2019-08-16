package com.xgoing.ftp.standalone;

import com.xgoing.ftp.AbstactFtpClientSupport;
import com.xgoing.ftp.exception.FtpNoConnectException;
import com.xgoing.ftp.exception.FtpRuntimeException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * StandAloneFtpClient 单机ftp客户端.
 */
public class StandAloneFtpClient extends AbstactFtpClientSupport {
    private FTPClient ftpClient;

    public StandAloneFtpClient(String host, int port, String username, String password, int weight) {
        super(host, port, username, password, weight);
    }

    @Override
    protected void init() {
        this.ftpClient = new FTPClient();
        FTPClientConfig config = new FTPClientConfig();
        config.setServerTimeZoneId(DEFAULT_TIME_ZONE);
        this.ftpClient.configure(config);
    }

    @Override
    protected int doUpload(String pathname, String filename, InputStream inputStream) {
        try {
            if (this.ftpClient.changeWorkingDirectory(pathname)) {
                boolean rest = this.ftpClient.storeFile(filename, inputStream);
                return rest ? REST_OK : REST_FAIL;
            } else {
                if (this.ftpClient.makeDirectory(pathname)) {
                    upload(pathname, filename, inputStream);
                }
            }
        } catch (Exception e) {
            throw new FtpRuntimeException(e.getCause());
        }
        return REST_FAIL;
    }

    @Override
    protected int doDownload(String pathname, String filename, OutputStream outputStream) {
        try {
            if (this.ftpClient.changeWorkingDirectory(pathname)) {
                boolean rest = this.ftpClient.retrieveFile(filename, outputStream);
                return rest ? REST_OK : REST_FAIL;
            }
        } catch (Exception e) {
            throw new FtpRuntimeException(e.getCause());
        }
        return REST_FAIL;
    }

    @Override
    protected List<String> doList(String pathname) {
        try {
            if (this.ftpClient.changeWorkingDirectory(pathname)) {
                FTPFile[] files = this.ftpClient.listFiles();
                if (files != null) {
                    List<String> filenameList = new ArrayList<String>();
                    for (int i = 0; i < files.length; i++) {
                        filenameList.add(files[i].getName());
                    }
                    return filenameList;
                }
            }
        } catch (Exception e) {
            throw new FtpRuntimeException(e);
        }
        return null;
    }

    @Override
    protected int doDelete(String pathname) {
        try {
            FTPFile[] files = this.ftpClient.listFiles(pathname);
            if (null != files && files.length > 0) {
                for (FTPFile file : files) {
                    if (file.isDirectory()) {
                        doDelete(pathname + UNIX_LIKE_SEPARATOR + file.getName());
                        // 切换到父目录，不然删不掉文件夹
                        this.ftpClient.changeWorkingDirectory(pathname.substring(0, pathname.lastIndexOf(UNIX_LIKE_SEPARATOR)));
                        this.ftpClient.removeDirectory(pathname);
                    } else {
                        if (!this.ftpClient.deleteFile(pathname + UNIX_LIKE_SEPARATOR + file.getName())) {
                            return REST_FAIL;
                        }
                    }
                }
            }
            // 切换到父目录，不然删不掉文件夹
            this.ftpClient.changeWorkingDirectory(pathname.substring(0, pathname.lastIndexOf("/")));
            this.ftpClient.removeDirectory(pathname);
            return REST_OK;
        } catch (IOException e) {
            throw new FtpRuntimeException(e);
        }
    }

    @Override
    protected int doRename(String oldPathname, String newPathname) {
        try {
            boolean rest = this.ftpClient.rename(oldPathname, newPathname);
            return rest ? REST_OK : REST_FAIL;
        } catch (Exception e) {
            throw new FtpRuntimeException(e.getCause());
        }
    }

    @Override
    protected int doMkdir(String pathname) {
        this.prepareWorkDir(pathname);
        return REST_OK;
    }

    @Override
    protected boolean checkExistDir(String pathname) {
        try {
            this.ftpClient.changeWorkingDirectory(pathname);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 准备当前工作目录（没有就创建）.
     *
     * @param pathname
     */
    private void prepareWorkDir(String pathname) {
        String fullWorkDir = this.parsePath(pathname);
        String[] dirArr = fullWorkDir.split(UNIX_LIKE_SEPARATOR);
        for (String dir : dirArr) {
            dir = dir.trim();
            if (dir.length() > 0) {
                try {
                    this.ftpClient.changeWorkingDirectory(dir);
                } catch (IOException e) {
                    try {
                        ftpClient.makeDirectory(dir);
                        ftpClient.changeWorkingDirectory(dir);
                    } catch (IOException ex) {
                        throw new FtpRuntimeException(ex);
                    }
                }
            }
        }
    }

    @Override
    protected void connectAndLogin() throws FtpNoConnectException {
        try {
            if (this.ftpClient != null && !this.ftpClient.isConnected()) {
                ftpClient.connect(this.getHost(), this.getPort());
                ftpClient.login(this.getUsername(), this.getPassword());
                int replyCode = ftpClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(replyCode)) {
                    ftpClient.disconnect();
                    throw new FtpNoConnectException("FTP server refused connection.");
                }
            }
        } catch (Exception e) {
            throw new FtpNoConnectException(e);
        }
    }

    @Override
    protected void logoutAndClose() {
        try {
            if (this.ftpClient != null && this.ftpClient.isConnected()) {
                this.ftpClient.logout();
                this.ftpClient.disconnect();
            }
        } catch (Exception e) {
            throw new FtpRuntimeException(e);
        }
    }
}
