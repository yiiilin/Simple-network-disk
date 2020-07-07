package cn.linaxhua.file_transfer.client.runnable;

import cn.linaxhua.file_transfer.client.view.panel.TransferListPanel;
import cn.linaxhua.file_transfer.client.vo.TransferLog;
import lombok.SneakyThrows;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransferStatisticsRunnable implements Runnable {
    private TransferLog transferLog;
    private Integer logIndex;
    private Integer taskAmount;
    private Long fileSize;
    private Map<Integer, Long> progressRate;
    private Deque<Long> statistcsDeque = new ArrayDeque<>(2);
    private Deque<Long> timeDeque = new ArrayDeque<>(2);
    private static String[] timeUnits = {"s", "m", "h"};


    /**
     * 任务进度统计线程，每个任务一个
     *
     * @param transferLog  任务记录
     * @param index        第几个任务
     * @param taskAmount   分片总量
     * @param fileSize     文件总大小
     * @param progressRate 统计结果
     */
    public TransferStatisticsRunnable(TransferLog transferLog, Integer index, Integer taskAmount, Long fileSize, Map<Integer, Long> progressRate) {
        this.transferLog = transferLog;
        this.logIndex = index;
        this.taskAmount = taskAmount;
        this.fileSize = fileSize;
        this.progressRate = progressRate;
    }

    @SneakyThrows
    @Override
    public void run() {
        long sumSize, transferBytes, tempTag;
        statistcsDeque.addFirst(0L);
        statistcsDeque.addFirst(0L);
        timeDeque.addFirst(System.currentTimeMillis());
        timeDeque.addFirst(System.currentTimeMillis());
        Long temp;
        while (!Thread.currentThread().isInterrupted()) {
            sumSize = 0;
            for (int i = 0; i < taskAmount; i++) {
                temp = progressRate.get(i);
                if (temp != null) {
                    sumSize = sumSize + temp;
                }
            }
            if (sumSize >= fileSize) {
                break;
            }
            tempTag = statistcsDeque.removeLast();
            statistcsDeque.addFirst(sumSize);
            timeDeque.removeLast();
            timeDeque.addFirst(System.currentTimeMillis());
            transferBytes = sumSize - tempTag;
            formatTransferSpeed(transferBytes, sumSize);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 格式化TransferLog
     *
     * @param transferBytes 传输字节数
     * @param processedSize 已经传输的字节数
     */
    private void formatTransferSpeed(Long transferBytes, Long processedSize) {
        try {
            long spendTime = timeDeque.getFirst() - timeDeque.getLast();
            String hasDownloadSize = TransferListPanel.getSize(processedSize.toString());
            String timeLeft = getTimeLeft(transferBytes, processedSize, spendTime);
            String speed = getSpeed(transferBytes, spendTime);
            transferLog.setHasDownloadSize(hasDownloadSize)
                    .setTimeLeft(timeLeft)
                    .setTransferSpeed(speed);
            TransferListPanel.updateData(transferLog, logIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getTimeLeft(Long transferBytes, Long processedSize, long spendTime) {
        if (transferBytes <= 0) {
            return "无限期";
        }
        long lastSize = fileSize - processedSize;
        if (lastSize / transferBytes > 86400) {
            return "大于一天";
        }
        long lastTime = lastSize * 1000 / transferBytes / spendTime;
        Long sec, min, hou;
        sec = lastTime % 60;
        lastTime /= 60;
        min = lastTime % 60;
        lastTime /= 24;
        hou = lastTime;
        StringBuilder stringBuilder = new StringBuilder();
        boolean hasTop = false;
        if (hou != 0) {
            stringBuilder.append(hou + timeUnits[2]);
            hasTop = true;
        }
        if (min != 0) {
            stringBuilder.append(min + timeUnits[1]);
            hasTop = true;
        } else {
            if (hasTop) {
                stringBuilder.append(min + timeUnits[1]);
            }
        }
        if (sec != 0) {
            stringBuilder.append(sec + timeUnits[0]);
        } else {
            if (hasTop) {
                stringBuilder.append(min + timeUnits[0]);
            }
        }
        return stringBuilder.toString();
    }

    private String getSpeed(Long transferBytes, long spendTime) {
        if (spendTime == 0) {
            return "0.00KB/s";
        }
        BigDecimal sizeDecimal = new BigDecimal(transferBytes * 1000 / spendTime);
        int unitNum = 0;
        BigDecimal threshold = new BigDecimal(1024);
        while (sizeDecimal.compareTo(threshold) != -1) {
            sizeDecimal = sizeDecimal.divide(threshold);
            unitNum++;
        }
        return sizeDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + TransferLog.UNITS[unitNum] + "/s";
    }
}
