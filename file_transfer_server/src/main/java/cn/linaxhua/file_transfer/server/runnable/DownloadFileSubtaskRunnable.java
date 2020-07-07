package cn.linaxhua.file_transfer.server.runnable;

import cn.linaxhua.file_transfer.common.util.NetUtil;
import cn.linaxhua.file_transfer.common.config.FileConfig;
import lombok.SneakyThrows;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DownloadFileSubtaskRunnable implements Runnable {
    private Socket socket = null;
    private String uuid = null;
    private Integer index = null;
    private Long start = null;
    private Long end = null;

    private BufferedOutputStream outputStream = null;
    private InputStream inputStream = null;

    private static final int BUFFER_SIZE = 8192;


    public DownloadFileSubtaskRunnable(Socket socket, String uuid, Integer index, Long start, Long end) {
        this.socket = socket;
        this.uuid = uuid;
        this.index = index;
        this.start = start;
        this.end = end;
    }

    @SneakyThrows
    @Override
    public void run() {
        try {
            File file = new File(FileConfig.EXIST_FILE_PATH + "/" + uuid);
            RandomAccessFile outputFile = new RandomAccessFile(file, "r");
            outputFile.seek(start);
            outputStream = new BufferedOutputStream(socket.getOutputStream());
            byte[] bytes = new byte[BUFFER_SIZE];
            long position = start;
            outputFile.seek(position);
            while (position < end) {
                int tempSize = (position + BUFFER_SIZE) > end ? (int) (end - position) : BUFFER_SIZE;
                outputFile.read(bytes, 0, tempSize);
                position += tempSize;
                outputStream.write(bytes, 0, tempSize);
                outputStream.flush();
            }
            outputFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();
        }
    }
}
