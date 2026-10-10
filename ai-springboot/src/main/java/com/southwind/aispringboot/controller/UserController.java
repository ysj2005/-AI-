package com.southwind.aispringboot.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.southwind.aispringboot.DTO.command.UserLoginCommandDTO;
import com.southwind.aispringboot.DTO.command.UserRegisterCommandDTO;
import com.southwind.aispringboot.DTO.response.UserLoginResponseDTO;
import com.southwind.aispringboot.common.Result;
import com.southwind.aispringboot.service.UserService;
import com.southwind.aispringboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginCommandDTO commandDTO) {
        //调用服务层登陆方法
        UserLoginResponseDTO result = userService.login(commandDTO);
        return Result.ok(result);
    }
//用户注册接口
    @PostMapping("/add")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> register(@Valid @RequestBody UserRegisterCommandDTO commandDTO) {
        UserLoginResponseDTO.UserDetailResponseDTO result = userService.register(commandDTO);
        return Result.ok(result);
    }
    @GetMapping("/current")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> getCurrentUser() {
        String token = JwtTokenUtil.getCurrentToken();
        if (token == null) {
            return Result.error("401", "未登录或token已过期");
        }
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();
        UserLoginResponseDTO.UserDetailResponseDTO result = userService.getUserById(userId);
        return Result.ok(result);
    }
}
