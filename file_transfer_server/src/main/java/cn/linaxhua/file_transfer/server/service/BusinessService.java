package cn.linaxhua.file_transfer.server.service;

import cn.linaxhua.file_transfer.common.entity.Structure;

import java.util.List;

public interface BusinessService {
    public Structure getStructureByPath(String path, String name, Integer uid);

    public Structure getStructure(Integer id);

    public List<Structure> listAllStructure(Integer uid);

    public Boolean addStructure(Structure structure);

    public Boolean updateStructure(Structure structure);

    public Boolean deleteStructure(Integer id);
}
