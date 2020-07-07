package cn.linaxhua.file_transfer.client.runnable;

import cn.linaxhua.file_transfer.client.config.ServerConfig;
import cn.linaxhua.file_transfer.client.thread_manager.SocketManager;
import cn.linaxhua.file_transfer.client.view.panel.JTreePanel;
import cn.linaxhua.file_transfer.client.view.panel.TransferListPanel;
import cn.linaxhua.file_transfer.client.vo.TransferLog;
import cn.linaxhua.file_transfer.common.config.FileConfig;
import cn.linaxhua.file_transfer.common.entity.Structure;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FileHandleRunnable implements Runnable {
    private Structure structure = null;
    //操作类型   1为下载   2为上传
    private Integer handleType = null;
    private static Integer FILE_MAX_SIZE;
    private ThreadPoolExecutor executor;
    private ThreadPoolExecutor statistcsExecutor;
    private String path;
    private JTreePanel jTreePanel;
    private Integer index;
    TransferLog transferLog = new TransferLog();
    Map<Integer, Long> taskStatistcs = new HashMap<>();

    private static final String serverAddress = ServerConfig.SERVER_URL;
    private static final Integer serverPort = SocketManager.getTransferPort();

    /**
     * 第一个Integer表示记录编号，即任务编号
     * 第二个Map是分片下载进度统计，其中Integer是第几个下载任务，Long是下载的字节数
     */
    private static ConcurrentHashMap<Integer, Map<Integer, Long>> progressRate = new ConcurrentHashMap<>();


    public FileHandleRunnable(Structure structure, Integer handleType, ThreadPoolExecutor executor, String path, Integer fileMaxSize, JTreePanel jTreePanel, ThreadPoolExecutor statistcsExecutor, TransferLog transferLog, int index) throws IOException {
        this.structure = structure;
        this.handleType = handleType;
        this.executor = executor;
        this.statistcsExecutor = statistcsExecutor;
        this.path = path;
        FILE_MAX_SIZE = fileMaxSize;
        this.jTreePanel = jTreePanel;
        this.transferLog = transferLog;
        this.index = index;
    }

    @Override
    public void run() {
        try {
            progressRate.put(index, taskStatistcs);
            //上传
            if (handleType == 2) {
                Integer socketNum = (int) Math.ceil(structure.getSize() / (double) FILE_MAX_SIZE);
                List<Callable<Integer>> callables = new ArrayList<>();
                for (int i = 0; i < socketNum; i++) {
                    Socket socket = new Socket(serverAddress, serverPort);
                    Long start = (long) i * (long) FILE_MAX_SIZE;
                    String firstStr = getFirstString(structure, 0, socketNum, i, start, 0L);
                    sendMsg(socket, firstStr);
                    if (i == socketNum - 1) {
                        callables.add(new UploadFileSubtaskRunnable(path, start, (int) (structure.getSize() - (long) i * (long) FILE_MAX_SIZE), socket, taskStatistcs, i));
                    } else {
                        callables.add(new UploadFileSubtaskRunnable(path, start, FILE_MAX_SIZE, socket, taskStatistcs, i));
                    }
                }
                Future statistcsFuture = statistcsExecutor.submit(new TransferStatisticsRunnable(transferLog, index, socketNum, structure.getSize(), taskStatistcs));
                List<Future<Integer>> futures = executor.invokeAll(callables);
                Integer result = 0;
                try {
                    for (int i = 0; i < futures.size(); i++) {
                        result = result + futures.get(i).get();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                try {
                    statistcsFuture.cancel(true);
                    while (!statistcsFuture.isCancelled()) {
                    }
                    transferLog.setHasDownloadSize(TransferListPanel.getSize(structure.getSize().toString()))
                            .setTransferSpeed("0.00B/s");
                    TransferListPanel.updateData(transferLog, index);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (result.equals(socketNum)) {
                    Thread.sleep(1000);
                    jTreePanel.refreshUI();
                }
                taskFinish();
                return;
            }
            if (handleType == 1) {
                File tempFile = new File(FileConfig.TEMP_FILE_PATH);
                if (!tempFile.exists()) {
                    tempFile.mkdir();
                }
                Integer socketNum = (int) Math.ceil(structure.getSize() / (double) FILE_MAX_SIZE);
                List<Callable<Boolean>> callables = new ArrayList<>();
                for (int i = 0; i < socketNum; i++) {
                    Socket socket = new Socket(serverAddress, serverPort);
                    Long start = (long) i * (long) FILE_MAX_SIZE;
                    File file = new File(FileConfig.TEMP_FILE_PATH + "/" + structure.getUuid() + "-" + i);
                    Long fileExistSize = 0L;
                    if (file.exists()) {
                        fileExistSize = file.length();
                    }
                    Long end = start + FILE_MAX_SIZE;
                    start += fileExistSize;
                    if (end > structure.getSize()) {
                        end = structure.getSize();
                    }
                    String firstStr = getFirstString(structure, 1, socketNum, i, start, end);
                    sendMsg(socket, firstStr);
                    callables.add(new DownloadFileSubtaskRunnable(structure.getUuid(), socket, taskStatistcs, i));
                }
                Future statistcsFuture = statistcsExecutor.submit(new TransferStatisticsRunnable(transferLog, index, socketNum, structure.getSize(), taskStatistcs));
                List<Future<Boolean>> futures = executor.invokeAll(callables);

                List<Boolean> results = new ArrayList<>();
                try {
                    for (int i = 0; i < futures.size(); i++) {
                        results.add(futures.get(i).get());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                try {
                    statistcsFuture.cancel(true);
                    while (!statistcsFuture.isDone()) {
                    }
                    transferLog.setHasDownloadSize(TransferListPanel.getSize(structure.getSize().toString()))
                            .setTimeLeft("一些时间")
                            .setTransferSpeed("0.00B/s");
                    TransferListPanel.updateData(transferLog, index);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Integer resultSum = 0;
                for (Boolean result : results) {
                    if (result) {
                        resultSum++;
                    }
                }
                if (resultSum == results.size()) {
                    mergeFiles(resultSum, structure.getUuid(), path);
                } else {
                    return;
                }
                taskFinish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void mergeFiles(Integer fileNums, String uuid, String path) {
        String fileName = structure.getName();
        File existDir = new File(path);
        if (!existDir.exists()) {
            existDir.mkdirs();
        }
        File saveFile = new File(path + "/" + fileName);
        List<File> transferFiles = new ArrayList<>();
        for (int i = 0; i < fileNums; i++) {
            transferFiles.add(new File(FileConfig.TEMP_FILE_PATH + "/" + uuid + "-" + i));
        }
        try {
            FileChannel resultFileChanel = new FileOutputStream(saveFile, true).getChannel();
            for (int i = 0; i < fileNums; i++) {
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

    private void taskFinish() {
        transferLog.setStatus("已完成")
                .setHasDownloadSize(TransferListPanel.getSize(structure.getSize().toString()))
                .setTimeLeft("0")
                .setTransferSpeed("0")
                .setLastConnectTime(String.valueOf(System.currentTimeMillis()));
        TransferListPanel.saveLogFile(transferLog);
        TransferListPanel.updateData(transferLog, index);
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
    public static String getFirstString(Structure structure, Integer type, Integer num, Integer index, Long start, Long end) {
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

    public static void sendMsg(Socket socket, String msg) throws IOException {
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.print(msg);
        pw.flush();
    }
}
