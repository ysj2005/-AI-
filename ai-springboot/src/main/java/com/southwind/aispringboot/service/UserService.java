package com.southwind.aispringboot.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.southwind.aispringboot.DTO.command.UserLoginCommandDTO;
import com.southwind.aispringboot.DTO.command.UserRegisterCommandDTO;
import com.southwind.aispringboot.DTO.response.UserLoginResponseDTO;
import com.southwind.aispringboot.entity.User;
import com.southwind.aispringboot.enumClass.UserType;
import com.southwind.aispringboot.exception.BusinessException;
import com.southwind.aispringboot.mapper.UserMapper;
import com.southwind.aispringboot.service.convert.UserConvert;
import com.southwind.aispringboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class UserService {
    @Resource
    private UserMapper userMapper;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public UserLoginResponseDTO login(UserLoginCommandDTO commandDTO){
        //构建查询条件
        LambdaQueryWrapper<User> queryWrapper =  new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername,commandDTO.getUsername())
                .or()
                .eq(User::getEmail,commandDTO.getUsername());
        //调用MP API查询
        User user =  userMapper.selectOne(queryWrapper);
        System.out.println( user);
        //判断用户·是否存在
        if (user == null){
            throw new BusinessException("用户不存在");
        }
        //验证密码
        String inputPassword = commandDTO.getPassword();
        if(!passwordEncoder.matches(inputPassword,user.getPassword())){
            throw new BusinessException("密码错误");
        }
        //检查用户的状态
        if (!user.isActive()){
          throw new BusinessException("用户被禁用,请联系管理员");
        }
        //生成JWT token
        String token = JwtTokenUtil.generateToken(user.getId(),user.getUsername(),user.getUserType());
        System.out.println(token);
       UserLoginResponseDTO.UserDetailResponseDTO userInfo = UserConvert.entityToDetailResponse(user);
        return UserConvert.entityToResponse(token, userInfo);
    }
    public UserLoginResponseDTO.UserDetailResponseDTO register(UserRegisterCommandDTO commandDTO){
        System.out.println(JSONUtil.parseObj(commandDTO));
        //验证密码是否一致
        if(!commandDTO.getPassword().equals(commandDTO.getConfirmPassword())){
            throw new BusinessException("密码不一致");
        }
        //检查用户名是否存在
        LambdaQueryWrapper<User> usernameQuery = new LambdaQueryWrapper<>();
        usernameQuery.eq(User::getUsername,commandDTO.getUsername());
        if (userMapper.selectCount(usernameQuery)>0){
            throw new BusinessException("用户名已存在");
        }
        //检查邮箱是否存在
        LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
        emailQuery.eq(User::getEmail,commandDTO.getEmail());
        if (userMapper.selectCount(emailQuery)>0){
            throw new BusinessException("邮箱已存在");
        }
        //用户类型
        if (!UserType.isValidCode(commandDTO.getUserType())){
            throw new BusinessException("无效的用户类型");
        }
        //创建用户
        String password = commandDTO.getPassword().trim();
        String encodedPassword = passwordEncoder.encode(password);
        User user = UserConvert.registerCommandToEntity(commandDTO,encodedPassword);
        //插入数据库
        userMapper.insert(user);

        return UserConvert.entityToDetailResponse(user);
    }

    public UserLoginResponseDTO.UserDetailResponseDTO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        System.out.println("User from DB: " + user);
        return UserConvert.entityToDetailResponse(user);
    }
}
