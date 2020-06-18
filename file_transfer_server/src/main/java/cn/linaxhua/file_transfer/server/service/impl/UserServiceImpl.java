package cn.linaxhua.file_transfer.server.service.impl;

import cn.linaxhua.file_transfer.common.entity.User;
import cn.linaxhua.file_transfer.server.dao.UserDao;
import cn.linaxhua.file_transfer.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Override
    public Boolean updatePassword(Integer uid, String password) {
        return userDao.updatePassword(uid, password);
    }

    @Override
    public Boolean updateName(Integer uid, String name) {
        return userDao.updateName(uid, name);
    }

    @Override
    public Boolean insertUser(User user) {
        return userDao.insertUser(user);
    }

    @Override
    public User getUser(Integer uid) {
        return userDao.getUser(uid);
    }
}
