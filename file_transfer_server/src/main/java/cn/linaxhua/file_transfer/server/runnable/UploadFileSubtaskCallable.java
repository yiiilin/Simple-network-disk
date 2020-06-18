package cn.linaxhua.file_transfer.server.runnable;

import cn.linaxhua.file_transfer.common.util.NetUtil;
import cn.linaxhua.file_transfer.common.config.FileConfig;
import cn.linaxhua.file_transfer.server.thread_manager.PortManager;
import cn.linaxhua.file_transfer.common.util.SpringContextUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UploadFileSubtaskCallable implements Callable<Boolean> {
    BufferedInputStream inputStream = null;
    private String tempFileName = null;
    private Integer socketPort = null;
    private static final int BUFFER_SIZE = 8192;

    private Lock lock = new ReentrantLock();

    public UploadFileSubtaskCallable(String uuid, Integer num, Integer port) {
        tempFileName = uuid + '-' + num;
        socketPort = port;
    }

    @SneakyThrows
    @Override
    public Boolean call() {
        ServerSocket serverSocket = null;
        Socket accept = null;
        try {
            File file = new File(FileConfig.TEMP_FILE_PATH + "/" + tempFileName);
            if (file.exists()) {
                return false;
            }
            if (!file.createNewFile()) {
                return false;
            }
            serverSocket = new ServerSocket(socketPort);
            accept = serverSocket.accept();
            inputStream = new BufferedInputStream(accept.getInputStream());
            byte[] bytes = new byte[BUFFER_SIZE];
            OutputStream fileOutputStream = new DataOutputStream(new FileOutputStream(file));
            int temp;
            while ((temp = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, temp);
            }
            inputStream.close();
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
            canPortRelease();
            PortManager.releasePort(socketPort);
            wakeUp();
        }
    }

    private void canPortRelease() {
        try {
            if (!NetUtil.isPortAvailable(socketPort)) {
                synchronized (this) {
                    wait(10 * 1000);
                }
                canPortRelease();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
