package cn.linaxhua.file_transfer.client.view.panel;

import cn.linaxhua.file_transfer.client.service.ApiService;
import cn.linaxhua.file_transfer.client.thread_manager.SocketManager;
import cn.linaxhua.file_transfer.common.entity.Structure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;


@Component
public class JTreePanel extends JPanel {
    @Autowired
    private ApiService apiService;
    @Autowired
    private SocketManager socketManager;

    private static ApiService apiServiceProxy;

    @PostConstruct
    private void init() {
        apiServiceProxy = apiService;
    }

    private List<Structure> structureList = null;
    private Integer uid = null;
    private String userName = null;
    private Set<String> dirPathSet = new HashSet<>();


    private DefaultMutableTreeNode root = null;
    private DefaultTreeModel model = null;
    private JTree tree = null;
    private JPanel treePanel = new JPanel();
    private JPanel buttonPanel = new JPanel();
    private JButton downloadButton = new JButton("下载");
    private JButton uploadButton = new JButton("上传");
    private JButton addDirButton = new JButton("新建文件夹");
    private JButton deleteButton = new JButton("删除");
    private JButton freshButton = new JButton("刷新目录");

    public JTreePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        root = new DefaultMutableTreeNode("root");
        model = new DefaultTreeModel(root);
        tree = new JTree(model);
        tree.setEditable(true);
        JScrollPane scrollPane = new JScrollPane(tree);
        treePanel.setLayout(new BorderLayout());
        treePanel.add(scrollPane, BorderLayout.CENTER);
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(downloadButton);
        buttonPanel.add(uploadButton);
        buttonPanel.add(addDirButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(freshButton);
        add(treePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        addListener();
    }

    private void createTree(List<Structure> structureList) {
        dirPathSet.clear();
        dirPathSet.add("/");
        for (Structure structure : structureList) {
            String[] paths = structure.getPath().substring(1).split("/");
            DefaultMutableTreeNode currentNode = root;
            StringBuilder dirPathsb = new StringBuilder("");
            for (String path : paths) {
                if (path.length() == 0) {
                    continue;
                }
                Boolean hasFind = false;
                Enumeration e = currentNode.children();
                while (e.hasMoreElements()) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
                    if (node.getUserObject().toString().equals(path)) {
                        hasFind = true;
                        currentNode = node;
                        break;
                    }
                }
                dirPathsb.append("/");
                dirPathsb.append(path);
                if (!hasFind) {
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(path);
                    currentNode.add(node);
                    currentNode = node;
                    dirPathSet.add(dirPathsb.toString());
                }
            }
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(structure.getName());
            switch (structure.getType()) {
                case "dir":
                    node = new DefaultMutableTreeNode(structure.getName());
                    dirPathsb.append("/");
                    dirPathsb.append(structure.getName());
                    dirPathSet.add(dirPathsb.toString());
                    break;
                case "uploading":
                    node = new DefaultMutableTreeNode(structure.getName() + "[未完成上传]");
                default:
                    break;
            }
            currentNode.add(node);
        }
        TreePath path = new TreePath(root.getPath());
        tree.expandPath(path);
    }

    private void addListener() {
        downloadButton.addActionListener(event -> {
            Map<String, String> map = getPathOfFile();
            if (map == null) {
                return;
            }
            String filePath = map.get("path");
            String name = map.get("name");
            Structure downloadStrcture;
            try {
                downloadStrcture = apiService.getStructureByPath(filePath, name, uid);
                if (downloadStrcture == null) {
                    JOptionPane.showMessageDialog(this, "下载服务异常", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "下载服务异常", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (downloadStrcture.getSize() < 0) {
                JOptionPane.showMessageDialog(this, "不应下载文件夹", "错误", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setCurrentDirectory(new File("."));
            jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int chooseResult = jFileChooser.showSaveDialog(this);
            if (chooseResult != JFileChooser.APPROVE_OPTION) {
                return;
            }
            String savePath = jFileChooser.getSelectedFile().getPath();
            File downloadFile = new File(savePath + "/" + downloadStrcture.getName());
            if (downloadFile.exists()) {
                JOptionPane.showMessageDialog(this, "下载目录已有此文件", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                socketManager.downloadFile(downloadStrcture, savePath, this);
            } catch (Exception exception) {
                exception.printStackTrace();
                JOptionPane.showMessageDialog(this, "下载服务异常", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        uploadButton.addActionListener(e -> {
            String dirPath = getPathOfDir();
            if (dirPath == null) {
                return;
            }
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setCurrentDirectory(new File("."));
            jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int chooseResult = jFileChooser.showOpenDialog(this);
            if (chooseResult != JFileChooser.APPROVE_OPTION) {
                return;
            }
            String filePath = jFileChooser.getSelectedFile().getPath();
            Structure structure = new Structure();
            File file = new File(filePath);
            structure
                    .setUid(uid)
                    .setPath(dirPath)
                    .setName(file.getName())
                    .setSize(file.length())
                    .setUpdate_time(new Date())
                    .setType("file");
            try {
                Structure s = apiService.getStructureByPath(dirPath, file.getName(), uid);
                if (s != null) {
                    if ("file".equals(s.getType())) {
                        JOptionPane.showMessageDialog(this, "该目录下已有同名文件", "错误", JOptionPane.WARNING_MESSAGE);
                    } else {
                        String uuid = s.getUuid();
                        String id = s.getId().toString();
                        structure.setId(Integer.parseInt(id));
                        structure.setUuid(uuid);
                        socketManager.uploadFile(structure, filePath, this);
                    }
                    return;
                } else {
                    String[] result = apiService.uploadFile(structure);
                    String uuid = result[0];
                    String id = result[1];
                    structure.setId(Integer.parseInt(id));
                    structure.setUuid(uuid);
                    socketManager.uploadFile(structure, filePath, this);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                JOptionPane.showMessageDialog(this, "上传服务异常", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        addDirButton.addActionListener(event -> {
            String dirPath = getPathOfDir();
            if (dirPath == null) {
                return;
            }
            String dirName = JOptionPane.showInputDialog("请输入新文件夹名称");
            if (dirName == null) {
                return;
            }
            try {
                Structure structure = new Structure();
                structure.setUid(uid)
                        .setPath(dirPath)
                        .setUuid(UUID.randomUUID().toString().replaceAll("-", ""))
                        .setName(dirName)
                        .setSize(-1L)
                        .setUpdate_time(new Date())
                        .setType("dir");
                apiService.addStructure(structure);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "新建文件夹失败", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            refreshUI();
        });
        deleteButton.addActionListener(event -> {
            Map<String, String> map = getDeletePath();
            if (map == null) {
                return;
            }
            String path = map.get("path");
            String name = map.get("name");
            if (name.contains("[未完成上传]")) {
                name = name.substring(0, name.lastIndexOf('['));
            }
            Structure structure = null;
            try {
                structure = apiService.getStructureByPath(path, name, uid);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "删除失败", "失败", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                if (!apiService.deleteStructure(structure.getId())) {
                    JOptionPane.showMessageDialog(this, "删除失败", "失败", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "删除失败", "失败", JOptionPane.ERROR_MESSAGE);
                return;
            }
            refreshUI();
        });

        freshButton.addActionListener(event -> {
            try {
                refreshUI();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "刷新目录失败", "失败", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public synchronized void refreshUI() {
        this.structureList = apiService.listAllStructure();
        root.removeAllChildren();
        createTree(structureList);
        tree.updateUI();
    }

    public void showWindow(Integer uid, String userName) {
        this.uid = uid;
        this.userName = userName;
        root.setUserObject(this.userName);
        refreshUI();
        this.setVisible(true);
    }

    public void hideWindow() {
        this.setVisible(false);
    }

    private String getPathOfDir() {
        DefaultMutableTreeNode selectNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (selectNode == null) {
            JOptionPane.showMessageDialog(this, "请选择目录", "无目录选择", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        TreeNode[] nodes = selectNode.getPath();
        StringBuilder sb = new StringBuilder("");
        for (int i = 1; i < nodes.length; i++) {
            sb.append("/");
            sb.append(nodes[i].toString());
        }
        if (sb.length() == 0) {
            sb.append("/");
        }
        String path = sb.toString();
        if (selectNode.isLeaf()) {
            if (!dirPathSet.contains(path)) {
                JOptionPane.showMessageDialog(this, "请选择文件夹而不是文件", "目录选择错误", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }
        return path;
    }

    private Map<String, String> getPathOfFile() {
        DefaultMutableTreeNode selectNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (selectNode == null) {
            JOptionPane.showMessageDialog(this, "请先选择文件", "无文件选择", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (!selectNode.isLeaf()) {
            JOptionPane.showMessageDialog(this, "请选择文件而不是文件夹", "无文件选择", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        TreeNode[] nodes = selectNode.getPath();
        StringBuilder sb = new StringBuilder("");
        for (int i = 1; i < nodes.length; i++) {
            sb.append("/");
            sb.append(nodes[i].toString());
        }
        String choosePath = sb.toString();
        int splitIndex = choosePath.lastIndexOf('/');
        String filePath = choosePath.substring(0, splitIndex);
        if (filePath.length() == 0) {
            filePath = "/";
        }
        String name = choosePath.substring(splitIndex + 1);
        Map<String, String> map = new HashMap<>();
        map.put("path", filePath);
        map.put("name", name);
        return map;
    }

    private Map<String, String> getDeletePath() {
        DefaultMutableTreeNode selectNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (selectNode == null) {
            JOptionPane.showMessageDialog(this, "请先选择目录或文件", "无选择", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (!selectNode.isLeaf()) {
            JOptionPane.showMessageDialog(this, "请从子文件逐个删除", "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        TreeNode[] nodes = selectNode.getPath();
        if (nodes.length == 1) {
            JOptionPane.showMessageDialog(this, "无法删除根目录", "错误", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        StringBuilder sb = new StringBuilder("");
        for (int i = 1; i < nodes.length - 1; i++) {
            sb.append("/");
            sb.append(nodes[i].toString());
        }
        if (sb.length() == 0) {
            sb.append("/");
        }
        String filePath = sb.toString();
        String name = nodes[nodes.length - 1].toString();
        Map<String, String> map = new HashMap<>();
        map.put("path", filePath);
        map.put("name", name);
        return map;
    }

    public static void updateFileType(Integer id, String type) {
        Structure structure = new Structure()
                .setId(id)
                .setType(type);
        apiServiceProxy.updateStructure(structure);
    }
}
