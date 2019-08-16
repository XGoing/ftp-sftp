package com.xgoing.ftp;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * FtpClient.
 */
public interface FtpClient {
    /**
     * 类unix分隔符（不使用系统系统属性或者File.separator）.
     */
    String UNIX_LIKE_SEPARATOR = "/";
    /**
     * 时区.
     */
    String DEFAULT_TIME_ZONE = "Asia/Shanghai";
    /**
     * 普通ftp.
     */
    String TYPE_FTP = "ftp";
    /**
     * 安全sftp.
     */
    String TYPE_SFTP = "sftp";
    /**
     * ftp默认端口.
     */
    int PORT_FTP = 21;
    /**
     * sftp默认端口.
     */
    int PORT_SFTP = 22;
    /**
     * 失败.
     */
    int REST_FAIL = 0;
    /**
     * 成功.
     */
    int REST_OK = 1;

    /**
     * 上传文件.
     *
     * @param pathname    根目录下的子目录.
     * @param filename    文件名
     * @param inputStream 文件输入流
     * @return 0:失败；1：成功.
     */
    int upload(String pathname, String filename, InputStream inputStream);

    /**
     * 批量上传文件.
     *
     * @param pathname 根目录下的子目录.
     * @param fileMap  文件名与文件流Map表
     * @return 成功上传的文件名列表
     */
    List<String> uploadBatch(String pathname, Map<String, InputStream> fileMap);

    /**
     * 下载文件.
     *
     * @param pathname     根目录下的子目录.
     * @param filename     文件名
     * @param outputStream 文件输出流
     * @return 0:失败；1：成功.
     */
    int download(String pathname, String filename, OutputStream outputStream);

    /**
     * 下载文件.
     *
     * @param pathname          根目录下的子目录.
     * @param filename          文件名
     * @param outputStream      文件输出流
     * @param closeOutputStream 是否自动关闭输出流.
     * @return 0:失败；1：成功.
     */
    int download(String pathname, String filename, OutputStream outputStream, boolean closeOutputStream);

    /**
     * 获取指定文件目下的文件名列表..
     *
     * @param pathname 根目录下的子目录.
     * @return 文件名列表.
     */
    List<String> list(String pathname);

    /**
     * 删除指定目录文件.
     *
     * @param pathname 资源路径.
     * @return 0:失败；1：成功.
     */
    int delete(String pathname);

    /**
     * 全复制源然后删除源(target如果为目录则目录必须之前不存在).
     *
     * @param source 源.
     * @param target 目标.
     * @return 0:失败；1：成功.
     */
    int rename(String source, String target);


    /**
     * 递归创建目录.
     *
     * @param pathname 目录名.
     * @return 0:失败；1：成功.
     */
    int mkdir(String pathname);

    /**
     * 目录是否存在.
     *
     * @param pathname 目录名.
     * @return
     */
    boolean existDir(String pathname);

    /**
     * 权重.
     *
     * @return
     */
    int getWeight();
}
