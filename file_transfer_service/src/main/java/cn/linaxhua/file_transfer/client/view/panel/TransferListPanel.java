package cn.linaxhua.file_transfer.client.view.panel;

import cn.linaxhua.file_transfer.client.view.panel.table.CustomTableModel;
import cn.linaxhua.file_transfer.client.vo.TransferLog;
import cn.linaxhua.file_transfer.common.config.FileConfig;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class TransferListPanel extends JPanel {

    private static final CustomTableModel model = new CustomTableModel();
    private static final JTable table = new JTable(model);
    private static final JScrollPane scroolPane = new JScrollPane(table);
    private static List<TransferLog> transferLogList = new ArrayList<>();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final Vector data = new Vector();

    private static Integer UID = null;

    private static Boolean hasInit = false;

    private static Lock lock = new ReentrantLock();


    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    public TransferListPanel() {
        initJTable();
        setLayout(new BorderLayout());
        add(scroolPane, BorderLayout.CENTER);
    }

    public static void initJTable() {
        model.setDataVector(data, TransferLog.COLUMNS);
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, r);
        table.setShowGrid(true);
    }

    public static void initTableList() {
        File logFileDir = new File(FileConfig.LOG_FILE_PATH);
        if (!logFileDir.exists()) {
            logFileDir.mkdir();
        }
        File logFile = new File(FileConfig.LOG_FILE_PATH + "/" + FileConfig.LOG_FILE_NAME + "-" + UID);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String temp;
        List<String> dataList = new ArrayList<>();
        try {
            while ((temp = bufferedReader.readLine()) != null) {
                dataList.add(temp);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String oneData : dataList) {
            String[] datas = oneData.split(" ");
            transferLogList.add(new TransferLog(datas[0], datas[1], datas[2], datas[3], datas[4], datas[5], datas[6], datas[7]));
        }
    }

    private static TransferLog formateTransferLog(TransferLog transferLog) {
        String fileSize = getSize(transferLog.getFileSize());
        String lastConnectTime = "";
        if (!transferLog.getLastConnectTime().equals("") && !transferLog.getLastConnectTime().equals("null")) {
            lastConnectTime = dateFormat.format(new Date(Long.parseLong(transferLog.getLastConnectTime())));
        }
        TransferLog returnLog = new TransferLog();
        returnLog.setFileName(transferLog.getFileName())
                .setFileSize(fileSize)
                .setType(transferLog.getType())
                .setStatus(transferLog.getStatus())
                .setHasDownloadSize(transferLog.getHasDownloadSize())
                .setTimeLeft(transferLog.getTimeLeft())
                .setTransferSpeed(transferLog.getTransferSpeed())
                .setLastConnectTime(lastConnectTime);
        return returnLog;
    }

    public static String getSize(String size) {
        BigDecimal sizeDecimal = new BigDecimal(size);
        int unitNum = 0;
        BigDecimal threshold = new BigDecimal(1024);
        while (sizeDecimal.compareTo(threshold) != -1) {
            sizeDecimal = sizeDecimal.divide(threshold);
            unitNum++;
        }
        return sizeDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + TransferLog.UNITS[unitNum];
    }

    public static void initTableData() {
        data.clear();
        for (TransferLog log : transferLogList) {
            data.add(formateTransferLog(log).getVector());
        }
        table.updateUI();
    }

    public synchronized static Integer addData(TransferLog transferLog) {
        Integer index;
        lock.lock();
        try {
            index = transferLogList.size();
            transferLogList.add(transferLog);
            data.add(formateTransferLog(transferLog).getVector());
            table.updateUI();
        } finally {
            lock.unlock();
        }
        return index;
    }

    /**
     * 修改TransferLogList中的第index条数据，同时刷新table
     *
     * @param transferLog 修改后数据
     * @param index       索引
     */
    public synchronized static void updateData(TransferLog transferLog, Integer index) {
        transferLogList.set(index, transferLog);
        data.set(index, formateTransferLog(transferLog).getVector());
        table.updateUI();
    }

    public static void saveLogFile(TransferLog transferLog) {
        File logFileDir = new File(FileConfig.LOG_FILE_PATH);
        if (!logFileDir.exists()) {
            logFileDir.mkdir();
        }
        File logFile = new File(FileConfig.LOG_FILE_PATH + "/" + FileConfig.LOG_FILE_NAME + "-" + UID);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        PrintWriter writer;
        try {
            writer = new PrintWriter(new FileOutputStream(logFile, true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        writer.write(transferLog.toString());
        writer.flush();
        writer.close();
    }


    public void showWindow(Integer uid) {
        UID = uid;
        if (!hasInit) {
            initTableList();
            hasInit = true;
        }
        initTableData();
        this.setVisible(true);
    }

    public void hideWindow() {
        this.setVisible(false);
    }
}
