package cn.linaxhua.file_transfer.client.thread_manager;

import cn.linaxhua.file_transfer.client.runnable.FileHandleRunnable;
import cn.linaxhua.file_transfer.client.view.panel.JTreePanel;
import cn.linaxhua.file_transfer.client.view.panel.TransferListPanel;
import cn.linaxhua.file_transfer.client.vo.TransferLog;
import cn.linaxhua.file_transfer.common.config.FileConfig;
import cn.linaxhua.file_transfer.common.entity.Structure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;

@Component
public class SocketManager {
    private static ThreadPoolExecutor socketExecutor = null;
    private static ThreadPoolExecutor fileExecutor = null;
    private static ThreadPoolExecutor statistcsExecutor = null;

    private static Integer FILE_HANDLE_THREAD_SIZE;
    private static Integer SOCKET_PORT_SIZE;
    private static Integer FILE_MAX_SIZE;


    @Value("${socket-port.file-handle-thread-size}")
    public void setFileHandleThreadSize(Integer fileHandleThreadSize) {
        FILE_HANDLE_THREAD_SIZE = fileHandleThreadSize;
    }

    @Value("${socket-port.socket-thread-size}")
    private void setSocketPortSize(Integer socketPortSize) {
        SOCKET_PORT_SIZE = socketPortSize;
    }

    @Value("${socket-port.file-max-size}")
    public void setFileMaxSize(Integer fileMaxSize) {
        FILE_MAX_SIZE = fileMaxSize;
    }


    @PostConstruct
    private void init() {
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(30);
        fileExecutor = new ThreadPoolExecutor(1, FILE_HANDLE_THREAD_SIZE, 0, TimeUnit.SECONDS, blockingQueue);
        BlockingQueue<Runnable> fileBlockingQueue = new ArrayBlockingQueue<Runnable>(300);
        socketExecutor = new ThreadPoolExecutor(3, SOCKET_PORT_SIZE, 0, TimeUnit.SECONDS, fileBlockingQueue);
        BlockingQueue<Runnable> statistcsQueue = new ArrayBlockingQueue<Runnable>(30);
        statistcsExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, statistcsQueue);
    }

    public void downloadFile(List<Integer> ports, Structure structure, String savePath, JTreePanel jTreePanel) throws IOException {
        TransferLog transferLog = new TransferLog();
        transferLog.setFileName(structure.getName())
                .setFileSize(structure.getSize().toString())
                .setStatus("进行中")
                .setHasDownloadSize("0B")
                .setTimeLeft("无限期")
                .setTransferSpeed("0.00B/s")
                .setLastConnectTime("null")
                .setType("下载");
        int index = TransferListPanel.addData(transferLog);
        fileExecutor.submit(new FileHandleRunnable(structure, ports, 1, socketExecutor, savePath, FILE_MAX_SIZE, jTreePanel, statistcsExecutor, transferLog, index));
    }

    public void uploadFile(List<Integer> ports, Structure structure, String path, JTreePanel jTreePanel) throws IOException {
        TransferLog transferLog = new TransferLog();
        transferLog.setFileName(structure.getName())
                .setFileSize(structure.getSize().toString())
                .setStatus("进行中")
                .setHasDownloadSize("0B")
                .setTimeLeft("无限期")
                .setTransferSpeed("0.00B/s")
                .setLastConnectTime("null")
                .setType("上传");
        int index = TransferListPanel.addData(transferLog);
        fileExecutor.submit(new FileHandleRunnable(structure, ports, 2, socketExecutor, path, FILE_MAX_SIZE, jTreePanel, statistcsExecutor, transferLog, index));
    }

}
