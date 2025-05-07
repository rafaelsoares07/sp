package dev.rafael.cadastro.websokets.rooms;

import org.springframework.web.socket.WebSocketSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
    private final String id;
    private final String code;
    private final Map<WebSocketSession, Member> membros = new ConcurrentHashMap<>();

    public Room(String id, String code) {
        this.id = id;
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void adicionarMembro(WebSocketSession session, String name) {
        membros.put(session, new Member(session.getId(), name));
    }

    public void removerMembro(WebSocketSession session) {
        membros.remove(session);
    }

    public Map<WebSocketSession, Member> getMembros() {
        return membros;
    }

    // Classe interna representando o membro
    public static class Member {
        private final String sessionSocket;
        private final String name;

        public Member(String sessionSocket, String name) {
            this.sessionSocket = sessionSocket;
            this.name = name;
        }

        public String getSessionSocket() {
            return sessionSocket;
        }

        public String getName() {
            return name;
        }
    }
}
