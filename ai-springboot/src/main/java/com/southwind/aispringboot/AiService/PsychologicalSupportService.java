package com.southwind.aispringboot.AiService;

import com.southwind.aispringboot.DTO.command.ConsultationSessionCreateDTO;
import com.southwind.aispringboot.DTO.response.ConsultationMessageResponseDTO;
import com.southwind.aispringboot.entity.ConsultationSession;
import com.southwind.aispringboot.service.ConsultationMessageService;
import com.southwind.aispringboot.service.ConsultationSessionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
public class PsychologicalSupportService {

    @Autowired
    @Qualifier("open-ai")
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private ConsultationSessionService consultationSessionService;

    @Autowired
    private ConsultationMessageService consultationMessageService;

    public StructOutPut.StreamChatSession startSession(Long userId, ConsultationSessionCreateDTO createDTO) {
        ConsultationSession consultationSession = consultationSessionService.createSession(userId, createDTO);
        consultationMessageService.saveUserMessage(consultationSession.getId(), createDTO.getInitialMessage(), null);
        String sessionId = "session_" + consultationSession.getId();
        return new StructOutPut.StreamChatSession(
                sessionId,
                userId,
                createDTO.getInitialMessage(),
                System.currentTimeMillis(),
                System.currentTimeMillis() + 86400000L,
                1,
                "ACTIVE"
        );
    }

    public Flux<String> streamPsychologicalChat(String sessionId, String userMessage) {
        return Flux.create(sink -> {
            Long dbsessionId = extractSessionId(sessionId);
            if (dbsessionId == null) {
                sink.error(new RuntimeException("会话ID格式错误"));
                return;
            }

            boolean isInitialMessage = false;
            Integer messageCount = consultationMessageService.getMessageCountBySessionId(dbsessionId);
            if (messageCount == 1) {
                ConsultationMessageResponseDTO lastMessage = consultationMessageService.getLastMessageBySessionId(dbsessionId);
                if (lastMessage != null && lastMessage.getSenderType() == 1 && userMessage.equals(lastMessage.getContent())) {
                    isInitialMessage = true;
                }
            }

            if (!isInitialMessage) {
                consultationMessageService.saveUserMessage(dbsessionId, userMessage, null);
            }

            String conversationId = "conversation_" + sessionId;

            List<Message> userMessages = new ArrayList<>();
            userMessages.add(new UserMessage(userMessage));
            chatMemory.add(conversationId, userMessages);

            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(PromptManage.PSYCHOLOGICAL_SUPPORT_SYSTEM_PROMPT)
            ));

            StringBuilder fullResponse = new StringBuilder();

            chatClient.prompt(prompt)
                    .user(userMessage)
                    .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .stream()
                    .content()
                    .doOnNext(fragment -> {
                        fullResponse.append(fragment);
                        sink.next(fragment);
                    })
                    .doOnComplete(() -> {
                        String completeRes = fullResponse.toString();
                        consultationMessageService.saveAiMessage(dbsessionId, completeRes, "open-ai");
                        List<Message> aiMessages = new ArrayList<>();
                        aiMessages.add(new AssistantMessage(completeRes));
                        chatMemory.add(conversationId, aiMessages);
                        sink.complete();
                    })
                    .doOnError(sink::error)
                    .subscribe();
        });
    }

    public Long extractSessionId(String sessionId) {
        if (sessionId != null && sessionId.startsWith("session_")) {
            String idStr = sessionId.substring("session_".length());
            return Long.parseLong(idStr);
        }
        return null;
    }
}
