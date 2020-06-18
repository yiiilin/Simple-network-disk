package cn.linaxhua.file_transfer.client.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Vector;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TransferLog {
    private static final String FILE_NAME = "文件名";
    private static final String FILE_SIZE = "大小";
    private static final String TYPE = "类型";
    private static final String STATUS = "状态";
    private static final String HAS_DOWNLOAD_SIZE = "已下载";
    private static final String TIME_LEFT = "剩余时间";
    private static final String TRANSFER_SPEED = "传输速度";
    private static final String LAST_CONNECT_TIME = "完成时间";

    public static final String[] UNITS = {"B", "KB", "MB", "GB", "TB"};
    public static final Vector COLUMNS = new Vector();

    static {
        COLUMNS.add(FILE_NAME);
        COLUMNS.add(FILE_SIZE);
        COLUMNS.add(TYPE);
        COLUMNS.add(STATUS);
        COLUMNS.add(HAS_DOWNLOAD_SIZE);
        COLUMNS.add(TIME_LEFT);
        COLUMNS.add(TRANSFER_SPEED);
        COLUMNS.add(LAST_CONNECT_TIME);
    }

    //文件名
    private String fileName;
    //文件大小 字节
    private String fileSize;
    //操作类型
    private String type;
    //文件状态 已完成 进行中
    private String status;
    //已下载文件字节数
    private String hasDownloadSize;
    //剩余时间 秒
    private String timeLeft;
    //传输速度 字节每秒
    private String transferSpeed;
    //最后连接时间  时间戳
    private String lastConnectTime;

    public Vector getVector() {
        Vector vector = new Vector();
        vector.add(fileName);
        vector.add(fileSize);
        vector.add(type);
        vector.add(status);
        vector.add(hasDownloadSize);
        vector.add(timeLeft);
        vector.add(transferSpeed);
        vector.add(lastConnectTime);
        return vector;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(fileName);
        stringBuilder.append(" ");
        stringBuilder.append(fileSize);
        stringBuilder.append(" ");
        stringBuilder.append(type);
        stringBuilder.append(" ");
        stringBuilder.append(status);
        stringBuilder.append(" ");
        stringBuilder.append(hasDownloadSize);
        stringBuilder.append(" ");
        stringBuilder.append(timeLeft);
        stringBuilder.append(" ");
        stringBuilder.append(transferSpeed);
        stringBuilder.append(" ");
        stringBuilder.append(lastConnectTime);
        stringBuilder.append('\n');
        return stringBuilder.toString();
    }

}
