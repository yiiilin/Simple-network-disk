package cn.linaxhua.file_transfer.server.service;

import cn.linaxhua.file_transfer.common.entity.User;

public interface UserService {
    public Boolean updatePassword(Integer uid, String password);

    public Boolean updateName(Integer uid, String name);

    public Boolean insertUser(User user);

    public User getUser(Integer uid);

}
