package cn.linaxhua.file_transfer.server.thread_manager;

import cn.linaxhua.file_transfer.common.config.FileConfig;
import cn.linaxhua.file_transfer.common.entity.Structure;
import cn.linaxhua.file_transfer.server.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.*;

@Component
public class SocketManager {
    @Autowired
    private BusinessService businessService;
    private static ExecutorService socketExecutor = null;
    private static Long FILE_MAX_SIZE;
    private static Integer SOCKET_THREAD_SIZE;
    private static Integer TRANSFER_PORT;

    @Value("${socket-port.file-max-size}")
    public void setFileMaxSize(Long fileMaxSize) {
        FILE_MAX_SIZE = fileMaxSize;
    }

    @Value("${socket-port.socket-thread-size}")
    public void setSocketThreadSize(Integer socketThreadSize) {
        SOCKET_THREAD_SIZE = socketThreadSize;
    }

    @Value("${socket-port.transfer-port}")
    private void setTransferPort(Integer transferPort) {
        TRANSFER_PORT = transferPort;
    }


    public static Long getFileMaxSize() {
        return FILE_MAX_SIZE;
    }

    public static ExecutorService getExcutorService() {
        return socketExecutor;
    }

    public static Integer getTransferPort() {
        return TRANSFER_PORT;
    }


    @PostConstruct
    private void init() {
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(1000);
        socketExecutor = new ThreadPoolExecutor(4, SOCKET_THREAD_SIZE, 0, TimeUnit.SECONDS, blockingQueue);
    }


    public String getUploadFileUUID(Structure structure) {
        String uuid = UUID.randomUUID().toString().replace("-","");
        structure.setUuid(uuid)
                .setType("uploading");
        try {
            businessService.addStructure(structure);
            return uuid;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Boolean deleteFile(Structure structure) {
        File file = new File(FileConfig.EXIST_FILE_PATH + "/" + structure.getUuid());
        if (!file.exists()) {
            return true;
        }
        return file.delete();
    }
}
