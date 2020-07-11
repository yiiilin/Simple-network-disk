package cn.linaxhua.file_transfer.client.runnable;

import cn.linaxhua.file_transfer.client.config.ServerConfig;
import cn.linaxhua.file_transfer.client.vo.TransferLog;
import cn.linaxhua.file_transfer.common.entity.Structure;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Integer serverPort = null;
    private Integer fileMaxSize = null;
    private Structure structure = null;
    private Integer socketNum = null;
    private Long end = null;
    private Socket socket = null;
    private static final int BUFFER_SIZE = 8192;
    private String serverAddress = ServerConfig.SERVER_URL;
    private TransferLog log = null;

    private static final Logger logger = LoggerFactory.getLogger(FileHandleRunnable.class);


    private static Map<Integer, Long> progressRate = null;
    private Integer index = null;

    private Lock lock = new ReentrantLock();

    public UploadFileSubtaskRunnable(String filePath, Integer serverPort, Integer fileMaxSize, Structure structure, Map<Integer, Long> rate, Integer index, Integer socketNum) throws IOException {
        this.fileAbsolutePath = filePath;
        this.serverPort = serverPort;
        this.structure = structure;
        this.socketNum = socketNum;
        this.fileMaxSize = fileMaxSize;
        if (index == socketNum - 1) {
            this.end = structure.getSize();
        } else {
            this.end = (index + 1) * (long) fileMaxSize;
        }
        this.socket = socket;
        progressRate = rate;
        this.index = index;
    }

    @Override
    public Integer call() {
        try {
            logger.info("create upload socket index=" + index);
            Socket socket = new Socket(serverAddress, serverPort);
            Long start = (long) index * (long) fileMaxSize;
            String firstStr = getFirstString(structure, 0, socketNum, index, start, 0L);
            sendMsg(socket, firstStr);

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
            long position = start;
            outputFile.seek(position);
            Long sum = 0L;
            sum += offset;
            progressRate.put(index, sum);
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

    /**
     * 获取第一次操作字符串
     *
     * @param structure 文件信息
     * @param type      0表示上传，1表示下载
     * @param num       表示分片个数
     * @param index     表示第几个分片，从0开始
     * @param start     表示从什么位置开始上传或下载
     * @param end       表示从什么位置结束
     * @return t-u-n-i-s-e$
     */
    public String getFirstString(Structure structure, Integer type, Integer num, Integer index, Long start, Long end) {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append("-");
        sb.append(structure.getUuid());
        sb.append("-");
        sb.append(num);
        sb.append("-");
        sb.append(index);
        sb.append("-");
        sb.append(start);
        sb.append("-");
        sb.append(end);
        sb.append("$");
        return sb.toString();
    }

    public void sendMsg(Socket socket, String msg) throws IOException {
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.print(msg);
        pw.flush();
    }
}
