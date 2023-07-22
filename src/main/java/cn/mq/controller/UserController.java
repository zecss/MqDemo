package cn.mq.controller;

import cn.mq.domain.User;
import cn.mq.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: TODO
 * @Param
 * @return
 */

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("reg")
    public String reg(@RequestBody User user) {
        return userService.reg(user);
    }
}
