package cn.linaxhua.file_transfer.server.runnable;

import cn.linaxhua.file_transfer.common.config.FileConfig;
import cn.linaxhua.file_transfer.server.thread_manager.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class UploadFileSubtaskCallable implements Runnable {
    private Socket socket = null;
    private String uuid;
    private String tempFileName = null;
    private Integer num = null;
    private Integer index = null;
    private static final int BUFFER_SIZE = 8192;
    RandomAccessFile randomAccessFile = null;
    private ConcurrentHashMap<String, ConcurrentSkipListSet<Integer>> tasks = null;

    private static final Logger log = LoggerFactory.getLogger(UploadFileSubtaskCallable.class);


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
        File file = null;
        Long fileExistSize = 0L;
        try {
            socket.setSoTimeout(10000);
            log.info(socket + "-" + uuid + "-" + num + "-" + index);
            File tempDir = new File(FileConfig.TEMP_FILE_PATH);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            file = new File(FileConfig.TEMP_FILE_PATH + "/" + tempFileName);
            if (file.exists()) {
                fileExistSize = file.length();
            } else if (!file.createNewFile()) {
                return;
            }
            log.info("fileExistSize:" + fileExistSize);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(fileExistSize);

            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            log.info(fileExistSize + "$");
            pw.print(fileExistSize + "$");
            pw.flush();
            InputStream inputStream = new BufferedInputStream(socket.getInputStream());
            byte[] bytes = new byte[BUFFER_SIZE];
            int temp;
            while ((temp = inputStream.read(bytes)) != -1) {
                randomAccessFile.write(bytes, 0, temp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                log.info(socket + "-" + index + ": has down");
                createFinishTaskSet();
                randomAccessFile.close();
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
        if (!tasks.get(uuid).contains(index)) {
            tasks.get(uuid).add(index);
        }
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
