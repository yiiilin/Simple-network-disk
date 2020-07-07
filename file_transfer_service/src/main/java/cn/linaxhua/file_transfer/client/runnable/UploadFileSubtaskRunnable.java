package cn.linaxhua.file_transfer.client.runnable;

import cn.linaxhua.file_transfer.client.config.ServerConfig;
import cn.linaxhua.file_transfer.client.vo.TransferLog;
import lombok.SneakyThrows;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UploadFileSubtaskRunnable implements Callable<Integer> {
    BufferedOutputStream outputStream = null;
    private String fileAbsolutePath = null;
    private Long start = null;
    private Integer fileSize = null;
    private Socket socket = null;
    private static final int BUFFER_SIZE = 8192;
    private String serverAddress = ServerConfig.SERVER_URL;
    private TransferLog log = null;


    private static Map<Integer, Long> progressRate = null;
    private Integer index = null;

    private Lock lock = new ReentrantLock();

    public UploadFileSubtaskRunnable(String filePath, Long start, Integer size, Socket socket, Map<Integer, Long> rate, Integer index) {
        this.fileAbsolutePath = filePath;
        this.start = start;
        this.fileSize = size;
        this.socket = socket;
        progressRate = rate;
        this.index = index;
    }

    @Override
    public Integer call() {
        try {
            InputStream inputStream = socket.getInputStream();
            int len = 0;
            byte[] bs = new byte[1024];
            StringBuilder sb = new StringBuilder();
            while ((len = inputStream.read(bs)) != -1) {
                sb.append(new String(bs, 0, len, "UTF-8"));
                if ('$' == sb.charAt(sb.length() - 1)) {
                    break;
                }
            }
            Long offset = Long.parseLong(sb.toString().substring(0, sb.length() - 1));
            start += offset;
            outputStream = new BufferedOutputStream(socket.getOutputStream());
            byte[] bytes = new byte[BUFFER_SIZE];
            File file = new File(fileAbsolutePath);
            RandomAccessFile outputFile = new RandomAccessFile(file, "r");
            outputFile.seek(start);
            long end = start + fileSize;
            long position = start;
            outputFile.seek(position);
            Long sum = 0L;
            while (position < end) {
                int tempSize = (position + BUFFER_SIZE) > end ? (int) (end - position) : BUFFER_SIZE;
                outputFile.read(bytes, 0, tempSize);
                position += tempSize;
                writeBytes(bytes, tempSize, 0);
                sum += tempSize;
                progressRate.put(index, sum);
            }
            outputFile.close();
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (socket != null) {
                try {
                    socket.shutdownInput();
                    socket.shutdownOutput();
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void writeBytes(byte[] bytes, int size, int times) throws InterruptedException {
        try {
            if (times > 100) {
                return;
            }
            outputStream.write(bytes, 0, size);
            outputStream.flush();
            times++;
        } catch (IOException e) {
            synchronized (this) {
                wait(1000 * 5);
            }
            writeBytes(bytes, size, times);
        }
    }
}
