package cn.linaxhua.file_transfer.server.thread_manager;

import cn.linaxhua.file_transfer.common.config.FileConfig;
import cn.linaxhua.file_transfer.server.runnable.DownloadFileSubtaskRunnable;
import cn.linaxhua.file_transfer.server.runnable.UploadFileSubtaskCallable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

@Component
@Slf4j
public class FileHandler {
    private ExecutorService executor;
    private Integer port;
    private
    ServerSocket serverSocket = null;

    public static final ConcurrentHashMap<String, ConcurrentSkipListSet<Integer>> finishedTasks = new ConcurrentHashMap<>();


    public FileHandler() {
        this.executor = SocketManager.getExcutorService();
        port = SocketManager.getTransferPort();
    }


    public void start() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                InputStream inputStream = socket.getInputStream();
                byte[] bytes = new byte[1024];
                StringBuilder sb = new StringBuilder();
                int len = 0;
                while ((len = inputStream.read(bytes)) != -1) {
                    sb.append(new String(bytes, 0, len, Charset.forName("UTF-8")));
                    if ('$' == sb.charAt(sb.length() - 1)) {
                        /**
                         * t-u-n-i-s-e$
                         * t 0表示上传，1表示下载
                         * u UUID传文件的唯一id
                         * n 表示分片个数
                         * i 表示第几个分片，从0开始
                         * s 表示从什么位置开始上传或下载
                         * e 表示从什么位置结束
                         */
                        log.info(sb.toString());
                        String[] strings = sb.toString().substring(0, sb.length() - 1).split("-");
                        Boolean isUpload = true;
                        String uuid;
                        Integer socketNums = 0;
                        Integer index = 0;
                        Long start = 0L;
                        Long end = 0L;
                        if ("1".equals(strings[0])) {
                            isUpload = false;
                        }
                        uuid = strings[1];
                        socketNums = Integer.parseInt(strings[2]);
                        index = Integer.parseInt(strings[3]);
                        start = Long.parseLong(strings[4]);
                        end = Long.parseLong(strings[5]);
                        if (isUpload) {
                            executor.submit(new UploadFileSubtaskCallable(socket, uuid, socketNums, index,end-start));
                        } else {
                            executor.submit(new DownloadFileSubtaskRunnable(socket, uuid, index, start, end));
                        }
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
