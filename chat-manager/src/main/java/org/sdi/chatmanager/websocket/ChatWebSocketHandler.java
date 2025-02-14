package org.sdi.chatmanager.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.utils.CopyOnWriteMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final CopyOnWriteMap<Long, WebSocketSession> sessions;

    public ChatWebSocketHandler() {
        sessions = new CopyOnWriteMap<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("New connection: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        IncomingMessage incomingMessage = objectMapper.readValue(message.getPayload(), IncomingMessage.class);

        switch (incomingMessage.getType()) {
            case SUBSCRIBE -> handleSubscription(session, incomingMessage);
            case TYPING -> handleTyping(incomingMessage);
            case SEEN -> handleSeen(incomingMessage);
            default -> System.out.println("Unhandled message type: " + incomingMessage.getType());
        }
    }

    private void handleSubscription(WebSocketSession session, IncomingMessage message) {
        Long userId = message.getSenderId();
        sessions.put(userId, session);
        System.out.println("User subscribed: " + userId);
    }

    private void handleTyping(IncomingMessage message) {
        Long senderId = message.getSenderId();
        Long recipientId = message.getRecipientId();
        boolean isTyping = (boolean) message.getContent();

        sendMessage(recipientId, MessageType.TYPING, Map.of("senderId", senderId, "isTyping", isTyping));
    }

    private void handleSeen(IncomingMessage message) {
        Long senderId = message.getSenderId();
        Long recipientId = message.getRecipientId();

        sendMessage(recipientId, MessageType.SEEN, Map.of("senderId", senderId));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.forEach((userId, userSession) -> {
            if (userSession.getId().equals(session.getId())) {
                sessions.remove(userId);
            }
        });
        System.out.println("Connection closed: " + session.getId());
    }

    public void sendMessage(Long userId, MessageType messageType, Object message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Message messageToSend = new Message(messageType, message);
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageToSend)));
            } catch (Exception e) {
                System.out.println("Error sending message to user: " + userId);
            }
        }
    }

    private static class IncomingMessage {

        private MessageType type;
        private Long senderId;
        private Long recipientId;
        private Object content;

        public MessageType getType() {
            return type;
        }

        public void setType(MessageType type) {
            this.type = type;
        }

        public Long getSenderId() {
            return senderId;
        }

        public void setSenderId(Long senderId) {
            this.senderId = senderId;
        }

        public Long getRecipientId() {
            return recipientId;
        }

        public void setRecipientId(Long recipientId) {
            this.recipientId = recipientId;
        }

        public Object getContent() {
            return content;
        }

        public void setContent(Object content) {
            this.content = content;
        }
    }

    private record Message(MessageType type, Object message) {
    }

    public enum MessageType {

        DIRECT_MESSAGE_CREATE,
        DIRECT_MESSAGE_DELETE,
        DIRECT_MESSAGE_PATCH,
        GROUP_MESSAGE_CREATE,
        GROUP_MESSAGE_DELETE,
        GROUP_MESSAGE_PATCH,
        SUBSCRIBE,
        TYPING,
        SEEN
    }
}
