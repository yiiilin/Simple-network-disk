package cn.linaxhua.file_transfer.server.service.impl;

import cn.linaxhua.file_transfer.common.entity.Structure;
import cn.linaxhua.file_transfer.server.dao.BusinessDao;
import cn.linaxhua.file_transfer.server.service.BusinessService;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessServiceImpl implements BusinessService {
    @Autowired
    private BusinessDao businessDao;

    @Override
    public Structure getStructureByPath(String path,String name,Integer uid) {
        return businessDao.getStructureByPath(path,name,uid);
    }

    @Override
    public Structure getStructure(Integer id) {
        return businessDao.getStructure(id);
    }

    @Override
    public List<Structure> listAllStructure(Integer uid) {
        return businessDao.listAllStructure(uid);
    }

    @Override
    public Boolean addStructure(Structure structure) {
        return businessDao.addStructure(structure);
    }

    @Override
    public Boolean updateStructure(Structure structure) {
        return businessDao.updateStructure(structure);
    }

    @Override
    public Boolean deleteStructure(Integer id) {
        return businessDao.deleteStructure(id);
    }
}
