package cn.linaxhua.file_transfer.server.dao;

import cn.linaxhua.file_transfer.common.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao {

    public Boolean updatePassword(Integer uid, String password);

    public Boolean updateName(Integer uid, String name);

    public Boolean insertUser(User user);

    public User getUser(Integer uid);
}
