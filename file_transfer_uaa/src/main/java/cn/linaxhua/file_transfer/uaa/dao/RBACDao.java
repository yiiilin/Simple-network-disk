package cn.linaxhua.file_transfer.uaa.dao;


import cn.linaxhua.file_transfer.common.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RBACDao {
    public User getUserByUid(String uid);
}
