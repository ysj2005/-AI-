package com.southwind.aispringboot.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginCommandDTO {
    @NotBlank(message = "用户名或邮箱不能为空")
    @Size(min = 3, max = 100, message = "用户名或邮箱长度必须在3到100个字符之间")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在6到50个字符之间")
    private String password;
}
