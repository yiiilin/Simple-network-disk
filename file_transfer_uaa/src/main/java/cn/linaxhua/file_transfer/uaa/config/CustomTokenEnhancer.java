package cn.linaxhua.file_transfer.uaa.config;

import cn.linaxhua.file_transfer.uaa.dao.RBACDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomTokenEnhancer implements TokenEnhancer {

    @Autowired
    private RBACDao rbacDao;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        Object principal = authentication.getPrincipal();
        User user = null;
        if (principal instanceof User) {
            user = (User) principal;
        }
        cn.linaxhua.file_transfer.common.entity.User userInfo = rbacDao.getUserByUid(user.getUsername());
        final Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("uid", userInfo.getUid());
        additionalInfo.put("name", userInfo.getName());
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }
}
