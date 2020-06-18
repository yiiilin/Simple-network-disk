package cn.linaxhua.file_transfer.server.runnable;

import cn.linaxhua.file_transfer.common.util.NetUtil;
import cn.linaxhua.file_transfer.common.config.FileConfig;
import cn.linaxhua.file_transfer.server.thread_manager.PortManager;
import cn.linaxhua.file_transfer.common.util.SpringContextUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DownloadFileSubtaskRunnable implements Runnable {
    private ServerSocket serverSocket = null;
    BufferedOutputStream outputStream = null;
    private String uuid = null;
    private Long start = null;
    private Long size = null;
    private Integer socketPort = null;
    private static final int BUFFER_SIZE = 8192;

    private Lock lock = new ReentrantLock();


    public DownloadFileSubtaskRunnable(String uuid, Long start, Long size, Integer port) {
        this.uuid = uuid;
        this.start = start;
        this.size = size;
        this.socketPort = port;
    }

    @SneakyThrows
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(socketPort);
            Socket socket = serverSocket.accept();
            outputStream = new BufferedOutputStream(socket.getOutputStream());
            byte[] bytes = new byte[BUFFER_SIZE];
            File file = new File(FileConfig.EXIST_FILE_PATH + "/" + uuid);
            RandomAccessFile outputFile = new RandomAccessFile(file, "r");
            outputFile.seek(start);
            long end = start + size;
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
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
            canPortRelease();
            PortManager.releasePort(socketPort);
            wakeUp();
        }
    }

    private void canPortRelease() throws InterruptedException {
        if (!NetUtil.isPortAvailable(socketPort)) {
            synchronized (this) {
                wait(10 * 1000);
            }
            canPortRelease();
        }
    }

    private synchronized void wakeUp() {
        lock.lock();
        try {
            notifyAll();
        } finally {
            lock.unlock();
        }
    }
}
