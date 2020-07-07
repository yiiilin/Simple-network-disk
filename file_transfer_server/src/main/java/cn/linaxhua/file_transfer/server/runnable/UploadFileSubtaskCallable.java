package cn.linaxhua.file_transfer.server.runnable;

import cn.linaxhua.file_transfer.common.util.NetUtil;
import cn.linaxhua.file_transfer.common.config.FileConfig;
import cn.linaxhua.file_transfer.server.thread_manager.FileHandler;
import jdk.internal.util.xml.impl.Input;
import lombok.SneakyThrows;
import org.apache.catalina.connector.OutputBuffer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UploadFileSubtaskCallable implements Runnable {
    private Socket socket = null;
    private String uuid;
    private String tempFileName = null;
    private Integer num = null;
    private Integer index = null;
    private static final int BUFFER_SIZE = 8192;
    RandomAccessFile randomAccessFile = null;
    private ConcurrentHashMap<String, ConcurrentSkipListSet<Integer>> tasks = null;


    public UploadFileSubtaskCallable(Socket socket, String uuid, Integer num, Integer index) {
        this.socket = socket;
        this.uuid = uuid;
        tempFileName = uuid + '-' + index;
        this.num = num;
        this.index = index;
        tasks = FileHandler.finishedTasks;
    }

    @Override
    public void run() {
        try {
            File tempDir = new File(FileConfig.TEMP_FILE_PATH);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            File file = new File(FileConfig.TEMP_FILE_PATH + "/" + tempFileName);
            Long fileExistSize = 0L;
            if (file.exists()) {
                fileExistSize = file.length();
            }
            if (!file.createNewFile()) {
                return;
            }
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(fileExistSize);

            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            pw.print(fileExistSize + "$");
            pw.flush();
            InputStream inputStream = new BufferedInputStream(socket.getInputStream());
            byte[] bytes = new byte[BUFFER_SIZE];
            int temp;
            while ((temp = inputStream.read(bytes)) != -1) {
                randomAccessFile.write(bytes, 0, temp);
            }
            randomAccessFile.close();
            createFinishTaskSet();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void createFinishTaskSet() {
        if (tasks.get(uuid) == null) {
            tasks.put(uuid, new ConcurrentSkipListSet<>());
        }
        tasks.get(uuid).add(index);
        if (FileHandler.finishedTasks.get(uuid).size() == num) {
            mergeFiles();
        }
    }

    private void mergeFiles() {
        String fileName = uuid;
        File existDir = new File(FileConfig.EXIST_FILE_PATH);
        if (!existDir.exists()) {
            existDir.mkdir();
        }
        File saveFile = new File(FileConfig.EXIST_FILE_PATH + "/" + fileName);
        List<File> transferFiles = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            transferFiles.add(new File(FileConfig.TEMP_FILE_PATH + "/" + fileName + "-" + i));
        }
        try {
            FileChannel resultFileChanel = new FileOutputStream(saveFile, true).getChannel();
            for (int i = 0; i < num; i++) {
                FileChannel transferFileChannel = new FileInputStream(transferFiles.get(i)).getChannel();
                resultFileChanel.transferFrom(transferFileChannel, resultFileChanel.size(), transferFileChannel.size());
                transferFileChannel.close();
            }
            resultFileChanel.close();
            for (File tempFile : transferFiles) {
                tempFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
