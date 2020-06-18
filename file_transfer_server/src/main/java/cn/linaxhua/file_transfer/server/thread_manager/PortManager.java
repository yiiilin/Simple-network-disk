package cn.linaxhua.file_transfer.server.thread_manager;

import cn.linaxhua.file_transfer.common.util.NetUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


@Component
public class PortManager {
    private static final ConcurrentSkipListSet<Integer> IDLE_PORT_SET = new ConcurrentSkipListSet<Integer>();
    private static final ConcurrentSkipListSet<Integer> OCCUPY_PORT_SET = new ConcurrentSkipListSet<Integer>();
    private static ThreadPoolExecutor socketExecutor = null;
    private static Integer SOCKET_PORT_SIZE;
    private static Integer SOCKET_PORT_START;
    private static Integer SOCKET_PORT_END;


    @Value("${socket-port.socket-thread-size}")
    private void setSocketPortSize(Integer socketPortSize) {
        SOCKET_PORT_SIZE = socketPortSize;
    }

    @Value("${socket-port.start-port}")
    private void setSocketPortStart(Integer socketPortStart) {
        SOCKET_PORT_START = socketPortStart;
    }

    @Value("${socket-port.end-port}")
    private void setSocketPortEnd(Integer socketPortEnd) {
        SOCKET_PORT_END = socketPortEnd;
    }

    @PostConstruct
    public void init() {
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(100);
        socketExecutor = new ThreadPoolExecutor(4, SOCKET_PORT_SIZE, 0, TimeUnit.SECONDS, blockingQueue);
        for (int i = SOCKET_PORT_START; i < SOCKET_PORT_END; i++) {
            IDLE_PORT_SET.add(i);
        }
    }

    public ExecutorService getSocketExecutor() {
        return socketExecutor;
    }

    private Integer getIdlePortSize() {
        return IDLE_PORT_SET.size();
    }

    /**
     * 需占用 num 个端口
     *
     * @param num num 个端口
     * @return 占用端口
     */
    public synchronized List<Integer> occupyPort(int num) {
        if (getIdlePortSize() < num) {
            return null;
        }
        List<Integer> occupyPorts = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            int port = IDLE_PORT_SET.pollFirst();
            if(!NetUtil.isPortAvailable(port)){

            }
            OCCUPY_PORT_SET.add(port);
            occupyPorts.add(port);
        }
        return occupyPorts;
    }


    /**
     * 释放端口
     *
     * @param port 是否
     * @return 是否成功释放端口
     */
    public synchronized static Boolean releasePort(int port) {
        try {
            OCCUPY_PORT_SET.remove(port);
            IDLE_PORT_SET.add(port);
            return Boolean.TRUE;
        } catch (Exception e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
    }
}
