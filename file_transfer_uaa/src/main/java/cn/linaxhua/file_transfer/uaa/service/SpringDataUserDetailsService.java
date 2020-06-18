package cn.linaxhua.file_transfer.uaa.service;


import cn.linaxhua.file_transfer.common.entity.User;
import cn.linaxhua.file_transfer.uaa.dao.RBACDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 封装用户信息时需要
 */
@Service
public class SpringDataUserDetailsService implements UserDetailsService {
    @Autowired
    private RBACDao rbacDao;

    @Override
    public UserDetails loadUserByUsername(String uid) throws UsernameNotFoundException {
        try {
            User user = rbacDao.getUserByUid(uid);
            UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getUid().toString())
                    .password("{bcrypt}"+user.getPassword())
                    .authorities("common")
                    .build();
            return userDetails;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
