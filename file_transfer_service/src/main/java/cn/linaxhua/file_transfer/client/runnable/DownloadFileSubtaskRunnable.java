package cn.linaxhua.file_transfer.client.runnable;

import cn.linaxhua.file_transfer.client.config.ServerConfig;
import cn.linaxhua.file_transfer.common.config.FileConfig;
import cn.linaxhua.file_transfer.common.util.NetUtil;
import cn.linaxhua.file_transfer.common.util.SpringContextUtil;
import lombok.SneakyThrows;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DownloadFileSubtaskRunnable implements Callable<Boolean> {
    BufferedInputStream inputStream = null;
    private String tempFileName = null;
    private Integer socketPort = null;
    private static final int BUFFER_SIZE = 8192;

    private static String serverAddress = ServerConfig.SERVER_URL;

    private Socket socket = null;

    private static Map<Integer, Long> progressRate = null;
    private Integer index = null;

    private Lock lock = new ReentrantLock();

    public DownloadFileSubtaskRunnable(String uuid, Integer num, Integer port, Map<Integer, Long> rate, Integer index) {
        tempFileName = uuid + '-' + num;
        socketPort = port;
        progressRate = rate;
        this.index = index;
    }

    @SneakyThrows
    @Override
    public Boolean call() {
        try {
            connect(0);
            File file = new File(FileConfig.TEMP_FILE_PATH + "/" + tempFileName);
            if (file.exists()) {
                return false;
            }
            if (!file.createNewFile()) {
                return false;
            }
            inputStream = new BufferedInputStream(socket.getInputStream());
            byte[] bytes = new byte[BUFFER_SIZE];
            OutputStream fileOutputStream = new DataOutputStream(new FileOutputStream(file));
            Long sum = 0L;
            int temp;
            while ((temp = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, temp);
                sum += temp;
                progressRate.put(index, sum);
            }
            inputStream.close();
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (socket != null) {
                socket.close();
            }
            wakeUp();
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

    private void connect(int times) throws InterruptedException {
        try {
            if (times % 5 == 0) {
                socket = new Socket();
            }
            socket.connect(new InetSocketAddress(serverAddress, socketPort));
        } catch (Exception e) {
            synchronized (this) {
                wait(1000 * 5);
            }
            times++;
            connect(times);
        }
    }
}
