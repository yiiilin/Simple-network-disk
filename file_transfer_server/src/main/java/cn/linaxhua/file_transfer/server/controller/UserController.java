package cn.linaxhua.file_transfer.server.controller;

import cn.linaxhua.file_transfer.common.entity.User;
import cn.linaxhua.file_transfer.common.response.ResponseResultJson;
import cn.linaxhua.file_transfer.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "updatePassword", method = RequestMethod.PATCH)
    public ResponseResultJson<Boolean> updatePassword(@RequestBody Map<String,String> passwordMap) {
        String oldPassword=passwordMap.get("oldPassword");
        String newPassword=passwordMap.get("newPassword");
        Integer uid = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        try {
            User user = userService.getUser(uid);
            if (!encoder.matches(oldPassword, user.getPassword())) {
                return ResponseResultJson.failed("旧密码不正确", Boolean.FALSE);
            }
            String encodePwd = encoder.encode(newPassword);
            userService.updatePassword(uid, encodePwd);
            return ResponseResultJson.success("修改密码成功", Boolean.TRUE);
        } catch (Exception e) {
            return ResponseResultJson.unknownError("未知错误", Boolean.FALSE);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "updateName", method = RequestMethod.PATCH)
    public ResponseResultJson<Boolean> updateName(@RequestParam String name) {
        Integer uid = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        try {
            userService.updateName(uid, name);
            return ResponseResultJson.success("修改名称成功", Boolean.TRUE);
        } catch (Exception e) {
            return ResponseResultJson.unknownError("未知错误", Boolean.FALSE);
        }
    }


    @PreAuthorize("permitAll()")
    @RequestMapping(value = "register", method = RequestMethod.POST)
    public ResponseResultJson<Integer> register(@RequestBody User user) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodePwd = encoder.encode(user.getPassword());
        user.setPassword(encodePwd);
        try {
            userService.insertUser(user);
            return ResponseResultJson.success("注册成功", user.getUid());
        } catch (Exception e) {
            return ResponseResultJson.unknownError("未知错误", null);
        }
    }

}
