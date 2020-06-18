package cn.linaxhua.file_transfer.server.dao;

import cn.linaxhua.file_transfer.common.entity.Structure;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BusinessDao {
    public List<Structure> listAllStructure(Integer uid);

    public Boolean addStructure(Structure structure);

    public Boolean updateStructure(Structure structure);

    public Boolean deleteStructure(Integer id);

    Structure getStructureByPath(String path, String name, Integer uid);

    Structure getStructure(Integer id);
}
