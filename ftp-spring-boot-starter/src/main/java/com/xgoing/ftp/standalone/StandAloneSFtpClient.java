package com.xgoing.ftp.standalone;

import com.jcraft.jsch.*;
import com.xgoing.ftp.AbstactFtpClientSupport;
import com.xgoing.ftp.exception.FtpNoConnectException;
import com.xgoing.ftp.exception.FtpRuntimeException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 * StandAloneFtpClient 单机sftp客户端.
 */
public class StandAloneSFtpClient extends AbstactFtpClientSupport {
    private static final String CHANNEL_SFTP = "sftp";
    private static final String CHANNEL_EXEC = "exec";
    private static final int CONNECT_TIMEOUT = 10000;
    private JSch jSch;
    private ThreadLocal<Session> ftpSessionLocal = new ThreadLocal<Session>();
    private ThreadLocal<ChannelSftp> ftpClientLocal = new ThreadLocal<ChannelSftp>();

    public StandAloneSFtpClient(String host, int port, String username, String password, int weight) {
        super(host, port, username, password, weight);
    }

    public StandAloneSFtpClient(String host, int port, String username, String password, String priKeyPath, int weight) {
        super(host, port, username, password, priKeyPath, weight);
    }

    @Override
    protected void init() {
        jSch = new JSch();
    }

    @Override
    protected void connectAndLogin() throws FtpNoConnectException {
        try {
            String priKeyPath = getPriKeyPath();
            if (priKeyPath != null && priKeyPath.trim().length() > 0) {
                priKeyPath = priKeyPath.trim();
                String passphrase = getPassword();
                if (passphrase == null || passphrase.trim().length() == 0) {
                    jSch.addIdentity(priKeyPath);
                } else {
                    jSch.addIdentity(priKeyPath, passphrase.trim());
                }
            }
            Session ftpSession = jSch.getSession(getUsername(), getHost(), getPort());
            ftpSession.setPassword(getPassword());
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            ftpSession.setConfig(config);
            ftpSession.connect();
            Channel channel = ftpSession.openChannel(CHANNEL_SFTP);
            channel.connect(CONNECT_TIMEOUT);
            ChannelSftp ftpClient = (ChannelSftp) channel;
            ftpSessionLocal.set(ftpSession);
            ftpClientLocal.set(ftpClient);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FtpNoConnectException(e);
        }
    }

    @Override
    protected void logoutAndClose() {
        try {
            ChannelSftp ftpClient = ftpClientLocal.get();
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.disconnect();
                ftpClientLocal.remove();
            }
            Session ftpSession = ftpSessionLocal.get();
            if (ftpSession != null && ftpSession.isConnected()) {
                ftpSession.disconnect();
                ftpSessionLocal.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int doUpload(String pathname, String filename, InputStream inputStream) {
        try {
            ChannelSftp ftpClient = ftpClientLocal.get();
            prepareWorkDir(pathname);
            ftpClient.put(inputStream, filename);
            return REST_OK;
        } catch (Exception e) {
            throw new FtpRuntimeException(e);
        }
    }

    @Override
    protected int doDownload(String pathname, String filename, OutputStream outputStream) {
        try {
            ChannelSftp ftpClient = ftpClientLocal.get();
            cd(pathname, ftpClient);
            ftpClient.get(filename, outputStream);
            return REST_OK;
        } catch (Exception e) {
            throw new FtpRuntimeException(e);
        }
    }

    @Override
    protected List<String> doList(String pathname) {
        try {
            ChannelSftp ftpClient = ftpClientLocal.get();
            cd(pathname, ftpClient);
            final List<String> filenameList = new ArrayList<String>();
            ftpClient.ls(ftpClient.pwd(), entry -> {
                String filename = entry.getFilename();
                if (!(filename.equals(".") || filename.equals(".."))) {
                    filenameList.add(filename);
                }
                return ChannelSftp.LsEntrySelector.CONTINUE;
            });
            return filenameList;
        } catch (Exception e) {
            throw new FtpRuntimeException(e);
        }
    }

    @Override
    protected int doDelete(String pathname) {
        try {
            ChannelSftp ftpClient = ftpClientLocal.get();
            if (isDirExist(pathname)) {
                Vector<ChannelSftp.LsEntry> vector = ftpClient.ls(pathname);
                // 文件，直接删除
                if (vector.size() == 1) {
                    ftpClient.rm(pathname);
                    // 空文件夹，直接删除
                } else if (vector.size() == 2) {
                    ftpClient.rmdir(pathname);
                } else {
                    String fileName = "";
                    // 删除文件夹下所有文件
                    for (ChannelSftp.LsEntry en : vector) {
                        fileName = en.getFilename();
                        if (".".equals(fileName) || "..".equals(fileName)) {
                            continue;
                        } else {
                            doDelete(pathname + UNIX_LIKE_SEPARATOR + fileName);
                        }
                    }
                    // 删除文件夹
                    ftpClient.rmdir(pathname);
                }
            }
            return REST_OK;
        } catch (Exception e) {
            throw new FtpRuntimeException(e);
        }
    }


    /**
     * 判断目录是否存在
     *
     * @param directory
     * @return
     */
    private boolean isDirExist(String directory) {
        try {
            ChannelSftp ftpClient = ftpClientLocal.get();
            Vector<?> vector = ftpClient.ls(directory);
            if (null == vector) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected int doRename(String source, String target) {
        try {
            ChannelSftp ftpClient = ftpClientLocal.get();
            ftpClient.rename(source, target);
            return REST_OK;
        } catch (Exception e) {
            throw new FtpRuntimeException(e);
        }
    }

    @Override
    protected int doMkdir(String pathname) {
        this.prepareWorkDir(pathname);
        return REST_OK;
    }

    @Override
    protected boolean checkExistDir(String pathname) {
        return this.isDirExist(pathname);
    }

    /**
     * 准备当前工作目录（没有就创建）.
     *
     * @param pathname
     */
    private void prepareWorkDir(String pathname) {
        ChannelSftp ftpClient = ftpClientLocal.get();
        String fullWorkDir = this.parsePath(pathname);
        String[] dirArr = fullWorkDir.split(UNIX_LIKE_SEPARATOR);
        for (String dir : dirArr) {
            dir = dir.trim();
            if (dir.length() > 0) {
                try {
                    ftpClient.cd(dir);
                } catch (SftpException e) {
                    try {
                        ftpClient.mkdir(dir);
                        ftpClient.cd(dir);
                    } catch (SftpException ex) {
                        throw new FtpRuntimeException(ex);
                    }
                }
            }
        }
    }

    /**
     * 准备当前工作目录（没有不创建）.
     *
     * @param pathname
     * @param ftpClient
     */
    private void cd(String pathname, ChannelSftp ftpClient) throws Exception {
        String fullWorkDir = this.parsePath(pathname);
        String[] dirArr = fullWorkDir.split(UNIX_LIKE_SEPARATOR);
        for (String dir : dirArr) {
            dir = dir.trim();
            if (dir.length() > 0) {
                ftpClient.cd(dir);
            }
        }
    }
}
