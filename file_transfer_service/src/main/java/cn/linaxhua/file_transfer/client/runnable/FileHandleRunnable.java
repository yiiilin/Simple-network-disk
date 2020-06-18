package cn.linaxhua.file_transfer.client.runnable;

import cn.linaxhua.file_transfer.client.view.panel.JTreePanel;
import cn.linaxhua.file_transfer.client.view.panel.TransferListPanel;
import cn.linaxhua.file_transfer.client.vo.TransferLog;
import cn.linaxhua.file_transfer.common.config.FileConfig;
import cn.linaxhua.file_transfer.common.entity.Structure;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FileHandleRunnable implements Runnable {
    private Structure structure = null;
    private List<Integer> ports = null;
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

    /**
     * 第一个Integer表示记录编号，即任务编号
     * 第二个Map是分片下载进度统计，其中Integer是第几个下载任务，Long是下载的字节数
     */
    private static ConcurrentHashMap<Integer, Map<Integer, Long>> progressRate = new ConcurrentHashMap<>();


    public FileHandleRunnable(Structure structure, List<Integer> ports, Integer handleType, ThreadPoolExecutor executor, String path, Integer fileMaxSize, JTreePanel jTreePanel, ThreadPoolExecutor statistcsExecutor, TransferLog transferLog, int index) throws IOException {
        this.structure = structure;
        this.ports = ports;
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
                Integer portNum = ports.size();
                List<Callable<Integer>> callables = new ArrayList<>();
                for (int i = 0; i < portNum; i++) {
                    if (i == portNum - 1) {
                        callables.add(new UploadFileSubtaskRunnable(path, (long) i * (long) FILE_MAX_SIZE, (int) (structure.getSize() - (long) i * (long) FILE_MAX_SIZE), ports.get(i), taskStatistcs, i));
                    } else {
                        callables.add(new UploadFileSubtaskRunnable(path, (long) i * (long) FILE_MAX_SIZE, FILE_MAX_SIZE, ports.get(i), taskStatistcs, i));
                    }
                }
                Future statistcsFuture = statistcsExecutor.submit(new TransferStatisticsRunnable(transferLog, index, portNum, structure.getSize(), taskStatistcs));
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
                if (result.equals(portNum)) {
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
                Integer portNum = ports.size();
                List<Callable<Boolean>> callables = new ArrayList<>();
                for (int i = 0; i < portNum; i++) {
                    callables.add(new DownloadFileSubtaskRunnable(structure.getUuid(), i + 1, ports.get(i), taskStatistcs, i));
                }
                Future statistcsFuture = statistcsExecutor.submit(new TransferStatisticsRunnable(transferLog, index, portNum, structure.getSize(), taskStatistcs));
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
                    while (!statistcsFuture.isCancelled()) {
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
        for (int i = 1; i <= fileNums; i++) {
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
}
