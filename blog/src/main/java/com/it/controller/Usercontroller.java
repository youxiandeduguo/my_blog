package com.it.controller;

import com.it.pojo.Result;
import com.it.pojo.User;
import com.it.service.UserService;
import com.it.utils.JwtUtil;
import com.it.utils.Md5Util;
import com.it.utils.ThreadLocalUtil;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Validated
public class Usercontroller {
    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/register")
    public Result register(@Pattern(regexp="^\\S{5,16}") String username,@Pattern(regexp="^\\S{5,16}") String password){
        User u=userService.findByUserName(username);
        if(u==null){
            userService.register(username, password);
            return Result.success();
        }else{
            return Result.error("用户已被占用");
        }
    }

    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp="^\\S{5,16}") String username,@Pattern(regexp="^\\S{5,16}") String password){
        User loginUser=userService.findByUserName(username);
        if(loginUser==null){
            return Result.error("用户名错误");
        }
        if(Md5Util.getMD5String(password).equals(loginUser.getPassword())){
            Map<String,Object> claims=new HashMap<>();
            claims.put("id",loginUser.getId());
            claims.put("username",loginUser.getUsername());
            String token = JwtUtil.genToken(claims);
            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            ops.set(token,token,1, TimeUnit.HOURS);
            return Result.success(token);
        }
        return Result.error("密码错误");
    }

    @GetMapping("/userInfo")
    public Result<User> userInfo(/*@RequestHeader(name ="Authorization")String token*/){
        /*
        Map<String,Object> map=JwtUtil.parseToken(token);
        String username=(String) map.get("username");*/
        Map<String,Object> map= ThreadLocalUtil.get();
        String username=(String)map.get("username");

        User user=userService.findByUserName(username);
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result update(@RequestBody @Validated User user){
        userService.update(user);
        return Result.success();
    }

    @PatchMapping("updateAvatar")
    public Result updateAvatar(@RequestParam @URL String avatarUrl){
        userService.updateAvatar(avatarUrl);
        return Result.success();
    }
    @PatchMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String,String> params,@RequestHeader("Authorization")String token){
        //首先对密码规范进行判断
        String oldPwd=params.get("old_pwd");
        String newPwd=params.get("new_pwd");
        String rePwd=params.get("re_pwd");
        if(!StringUtils.hasLength(oldPwd)||!StringUtils.hasLength(newPwd)||!StringUtils.hasLength(rePwd)){
            return Result.error("密码不全");
        }

        Map<String , Object> map= ThreadLocalUtil.get();
        String username=(String)map.get("username");
        User loginUser=userService.findByUserName(username);
        if(!loginUser.getPassword().equals(Md5Util.getMD5String(oldPwd))){
            return Result.error("原密码错误");
        }

        if(!rePwd.equals(newPwd)){
            return Result.error("新密码不一致");
        }

        //验证完毕，进行更改操作
        userService.updatePwd(newPwd);

        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.getOperations().delete(token);
        return Result.success();

    }


}
