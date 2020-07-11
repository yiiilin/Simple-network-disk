package cn.linaxhua.file_transfer.server.controller;

import cn.linaxhua.file_transfer.common.entity.Structure;
import cn.linaxhua.file_transfer.common.response.ResponseResultJson;
import cn.linaxhua.file_transfer.server.service.BusinessService;
import cn.linaxhua.file_transfer.server.thread_manager.SocketManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("busi")
public class BusinessController {
    @Autowired
    private BusinessService businessService;
    @Autowired
    private SocketManager socketManager;

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "listAllStructure", method = RequestMethod.GET)
    public ResponseResultJson<List<Structure>> listAllStructure() {
        try {
            Integer uid = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
            List<Structure> structures = businessService.listAllStructure(uid);
            return ResponseResultJson.success("文件结构获取成功", structures);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResultJson.unknownError("未知错误", null);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "addStructure", method = RequestMethod.POST)
    public ResponseResultJson<Boolean> addStructure(@RequestBody Structure structure) {
        try {
            businessService.addStructure(structure);
            return ResponseResultJson.success("文件结构添加成功", Boolean.TRUE);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResultJson.unknownError("未知错误", Boolean.FALSE);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "updateStructure", method = RequestMethod.PUT)
    public ResponseResultJson<Boolean> updateStructure(@RequestBody Structure structure) {
        try {
            businessService.updateStructure(structure);
            return ResponseResultJson.success("文件结构修改成功", Boolean.TRUE);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResultJson.unknownError("未知错误", Boolean.FALSE);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "deleteStructure/{id}", method = RequestMethod.DELETE)
    public ResponseResultJson<Boolean> deleteStructure(@PathVariable("id") String id) {
        try {
            id = id.replace("[", "");
            id = id.replace("]", "");
            Integer deleteFileid = Integer.parseInt(id);
            Structure structure = businessService.getStructure(deleteFileid);
            if (socketManager.deleteFile(structure)) {
                businessService.deleteStructure(deleteFileid);
                return ResponseResultJson.success("文件结构删除成功", Boolean.TRUE);
            } else {
                return ResponseResultJson.unknownError("未知错误", Boolean.FALSE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResultJson.unknownError("未知错误", Boolean.FALSE);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "getStructureByPath", method = RequestMethod.POST)
    public ResponseResultJson<Structure> getStructureByPath(@RequestBody Map<String, String> map) {
        try {
            String path = map.get("path");
            String name = map.get("name");
            Integer uid = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
            Structure detailStructure = businessService.getStructureByPath(path, name, uid);
            return ResponseResultJson.success("获取详细文件结构成功", detailStructure);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResultJson.unknownError("未知错误", null);
        }
    }

    @RequestMapping(value = "uploadFile", method = RequestMethod.POST)
    public ResponseResultJson<String> uploadFile(@RequestBody Structure structure) {
        try {
            String uuid = socketManager.getUploadFileUUID(structure);
            if (uuid != null) {
                return ResponseResultJson.success("获取上传文件UUID及id成功", uuid+"-"+structure.getId());
            } else {
                return ResponseResultJson.unknownError("未知错误", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResultJson.unknownError("未知错误", null);
        }
    }
}
