package cn.linaxhua.file_transfer.client.runnable;

import cn.linaxhua.file_transfer.client.config.ServerConfig;
import cn.linaxhua.file_transfer.client.thread_manager.SocketManager;
import cn.linaxhua.file_transfer.common.config.FileConfig;
import cn.linaxhua.file_transfer.common.entity.Structure;
import lombok.SneakyThrows;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DownloadFileSubtaskRunnable implements Callable<Boolean> {
    private BufferedInputStream inputStream = null;
    private String tempFileName = null;
    private Socket socket = null;
    private static final int BUFFER_SIZE = 8192;


    private static Map<Integer, Long> progressRate = null;
    private Integer index = null;


    public DownloadFileSubtaskRunnable(String uuid, Socket socket, Map<Integer, Long> rate, Integer index) {
        tempFileName = uuid + '-' + index;
        this.socket = socket;
        progressRate = rate;
        this.index = index;
    }

    @SneakyThrows
    @Override
    public Boolean call() {
        try {
            File tempDir=new File(FileConfig.TEMP_FILE_PATH);
            if(!tempDir.exists()){
                tempDir.mkdirs();
            }
            File file = new File(FileConfig.TEMP_FILE_PATH + "/" + tempFileName);
            Long fileExistSize = 0L;
            if (file.exists()) {
                fileExistSize = file.length();
            }else{
                file.createNewFile();
            }
            inputStream = new BufferedInputStream(socket.getInputStream());
            byte[] bytes = new byte[BUFFER_SIZE];
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(fileExistSize);
            Long sum = 0L;
            int temp;
            while ((temp = inputStream.read(bytes)) != -1) {
                randomAccessFile.write(bytes, 0, temp);
                sum += temp;
                progressRate.put(index, sum);
            }
            randomAccessFile.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (socket != null) {
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
            }
        }
    }
}
