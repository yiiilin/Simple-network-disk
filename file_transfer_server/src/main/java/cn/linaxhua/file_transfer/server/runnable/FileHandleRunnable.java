package cn.linaxhua.file_transfer.server.runnable;

import cn.linaxhua.file_transfer.common.entity.Structure;
import cn.linaxhua.file_transfer.common.config.FileConfig;
import cn.linaxhua.file_transfer.common.util.SpringContextUtil;
import cn.linaxhua.file_transfer.server.service.BusinessService;
import cn.linaxhua.file_transfer.server.thread_manager.SocketManager;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FileHandleRunnable implements Runnable {
    private Structure structure = null;
    private List<Integer> ports = null;
    //操作类型   1为下载   2为上传
    private Integer handleType = null;
    private ExecutorService executor;
    private BusinessService businessService;


    public FileHandleRunnable(Structure structure, List<Integer> ports, Integer handleType, ExecutorService executor, BusinessService businessService) throws IOException {
        this.structure = structure;
        this.ports = ports;
        this.handleType = handleType;
        this.executor = executor;
        this.businessService = businessService;
    }

    @Override
    public void run() {
        //上传
        if (handleType == 2) {
            File tempFile = new File(FileConfig.TEMP_FILE_PATH);
            if (!tempFile.exists()) {
                tempFile.mkdir();
            }
            Integer portNum = ports.size();
            List<Callable<Boolean>> callables = new ArrayList<>();
            for (int i = 0; i < portNum; i++) {
                callables.add(new UploadFileSubtaskCallable(structure.getUuid(), i + 1, ports.get(i)));
            }
            List<Future<Boolean>> futures = null;
            try {
                futures = executor.invokeAll(callables);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            List<Boolean> results = new ArrayList<>();
            try {
                for (int i = 0; i < futures.size(); i++) {
                    results.add(futures.get(i).get());
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            Integer resultSum = 0;
            for (Boolean result : results) {
                if (result) {
                    resultSum++;
                }
            }
            if (resultSum == results.size()) {
                mergeFiles(resultSum);
            } else {
                return;
            }
            try {
                businessService.addStructure(structure);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (handleType == 1) {
            Integer portNum = ports.size();
            for (int i = 0; i < portNum; i++) {
                if (i == portNum - 1) {
                    executor.submit(new DownloadFileSubtaskRunnable(structure.getUuid(), (long) i * SocketManager.getFileMaxSize(), structure.getSize() - (long) i * SocketManager.getFileMaxSize(), ports.get(i)));
                } else {
                    executor.submit(new DownloadFileSubtaskRunnable(structure.getUuid(), (long) i * SocketManager.getFileMaxSize(), SocketManager.getFileMaxSize(), ports.get(i)));
                }
            }
        }
    }

    private void mergeFiles(Integer fileNums) {
        String fileName = structure.getUuid();
        File existDir = new File(FileConfig.EXIST_FILE_PATH);
        if (!existDir.exists()) {
            existDir.mkdir();
        }
        File firstFile = new File(FileConfig.EXIST_FILE_PATH + "/" + fileName);
        List<File> transferFiles = new ArrayList<>();
        for (int i = 1; i <= fileNums; i++) {
            transferFiles.add(new File(FileConfig.TEMP_FILE_PATH + "/" + fileName + "-" + i));
        }
        try {
            FileChannel resultFileChanel = new FileOutputStream(firstFile, true).getChannel();
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
}
