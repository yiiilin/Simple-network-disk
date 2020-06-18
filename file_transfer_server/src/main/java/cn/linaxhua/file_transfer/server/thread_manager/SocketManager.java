package cn.linaxhua.file_transfer.server.thread_manager;

import cn.linaxhua.file_transfer.common.config.FileConfig;
import cn.linaxhua.file_transfer.common.entity.Structure;
import cn.linaxhua.file_transfer.server.dao.BusinessDao;
import cn.linaxhua.file_transfer.server.runnable.FileHandleRunnable;
import cn.linaxhua.file_transfer.server.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

@Component
public class SocketManager {
    @Autowired
    private PortManager portManager;
    @Autowired
    private BusinessService businessService;
    private static ExecutorService fileExecutor = null;

    private static Long FILE_MAX_SIZE;


    private static Integer FILE_HANDLE_THREAD_SIZE;


    @Value("${socket-port.file-max-size}")
    public void setFileMaxSize(Long fileMaxSize) {
        FILE_MAX_SIZE = fileMaxSize;
    }

    public static Long getFileMaxSize() {
        return FILE_MAX_SIZE;
    }

    @Value("${socket-port.file-handle-thread-size}")
    public void setFileHandleThreadSize(Integer fileHandleThreadSize) {
        FILE_HANDLE_THREAD_SIZE = fileHandleThreadSize;
    }


    @PostConstruct
    private void init() {
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(1000);
        fileExecutor = new ThreadPoolExecutor(4, FILE_HANDLE_THREAD_SIZE, 0, TimeUnit.SECONDS, blockingQueue);
    }


    public List<Integer> getDownloadFilePorts(Structure structure) throws IOException {
        long fileSize = structure.getSize();
        int num = (int) (fileSize / FILE_MAX_SIZE);
        if (fileSize % FILE_MAX_SIZE > 0) {
            num++;
        }
        List<Integer> ports = portManager.occupyPort(num);
        fileExecutor.submit(new FileHandleRunnable(structure, ports, 1, portManager.getSocketExecutor(), businessService));
        return ports;
    }

    public List<Integer> getUploadFilePorts(Structure structure) throws IOException {
        long fileSize = structure.getSize();
        int num = (int) (fileSize / FILE_MAX_SIZE);
        if (fileSize % FILE_MAX_SIZE > 0) {
            num++;
        }
        List<Integer> ports = portManager.occupyPort(num);
        fileExecutor.submit(new FileHandleRunnable(structure, ports, 2, portManager.getSocketExecutor(), businessService));
        return ports;
    }

    public Boolean deleteFile(Structure structure){
        File file=new File(FileConfig.EXIST_FILE_PATH+"/"+structure.getUuid());
        if(!file.exists()){
            return true;
        }
        return file.delete();
    }
}
