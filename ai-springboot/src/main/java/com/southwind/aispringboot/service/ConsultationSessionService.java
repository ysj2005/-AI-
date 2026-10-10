package com.southwind.aispringboot.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.southwind.aispringboot.DTO.command.ConsultationSessionCreateDTO;
import com.southwind.aispringboot.entity.ConsultationSession;
import com.southwind.aispringboot.entity.User;
import com.southwind.aispringboot.mapper.ConsultationSessionMapper;
import com.southwind.aispringboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConsultationSessionService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;

    public ConsultationSession createSession(Long userId, ConsultationSessionCreateDTO createDTO) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            ConsultationSession session = ConsultationSession.builder()
                    .userId(userId)
                    .sessionTitle(createDTO.getSessionTitle())
                    .startedAt(LocalDateTime.now())
                    .build();

            if (StrUtil.isBlank(createDTO.getSessionTitle())) {
                session.setSessionTitle("小JAI助手-" + DateUtil.format(LocalDateTime.now(), "MM-dd HH:mm:ss"));
            }

            consultationSessionMapper.insert(session);
            return session;
        }
        return null;
    }
}

