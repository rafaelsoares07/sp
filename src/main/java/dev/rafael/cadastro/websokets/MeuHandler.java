package dev.rafael.cadastro.websokets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rafael.cadastro.musics.MusicRepository;
import dev.rafael.cadastro.websokets.rooms.Room;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class MeuHandler extends TextWebSocketHandler {

    private final MusicRepository musicRepository;
    private final Map<String, BiConsumer<WebSocketSession, JsonNode>> actionHandlers = new HashMap<>();
    private static final AtomicInteger roomIdGenerator = new AtomicInteger(1);
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public MeuHandler(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
        actionHandlers.put("create_local_room", this::handleCreateLocalRoom);
        actionHandlers.put("join_room", this::handleJoinRoom);
        actionHandlers.put("ping", this::handlePing);
        actionHandlers.put("notify", this::handleSendMessage);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(payload);

        System.out.println("Recebido: " + payload);

        if (jsonNode.has("action")) {
            String action = jsonNode.get("action").asText();
            BiConsumer<WebSocketSession, JsonNode> handler = actionHandlers.get(action);
            if (handler != null) {
                handler.accept(session, jsonNode);
            } else {
                sendJsonMessage(session, "error", action, null, "Ação desconhecida: " + action);
            }
        } else {
            sendJsonMessage(session, "error", "unknown", null, "JSON inválido: campo 'action' não encontrado.");
        }
    }

    private void handleCreateLocalRoom(WebSocketSession session, JsonNode data) {
        try {
            if (usuarioJaEstaEmAlgumaSala(session)) {
                sendJsonMessage(session, "error", "create_local_room", null, "Usuário já está em uma sala");
                return;
            }

            String roomId = String.valueOf(roomIdGenerator.getAndIncrement());
            String code = CodeGenerator.generateCode();
            Room novaSala = new Room(roomId, code);

            String name = data.has("name") ? data.get("name").asText() : "Unknown User";
            novaSala.adicionarMembro(session, name);
            rooms.put(code, novaSala);

            Map<String, Object> salaJson = salaParaJson(novaSala);
            sendJsonMessage(session, "success", "room_created", salaJson, "Sala criada com sucesso");

        } catch (Exception e) {
            e.printStackTrace();
            sendJsonMessage(session, "error", "create_local_room", null, "Erro interno ao criar a sala");
        }
    }

    private void handleJoinRoom(WebSocketSession session, JsonNode data) {
        String roomId = data.has("roomId") ? data.get("roomId").asText() : null;
        String name = data.has("name") ? data.get("name").asText() : "Unknown User";

        if (roomId == null) {
            sendJsonMessage(session, "error", "join_room", null, "roomId é obrigatório");
            return;
        }

        Room room = rooms.get(roomId);
        if (room == null) {
            sendJsonMessage(session, "error", "join_room", null, "Sala não encontrada");
            return;
        }

        room.adicionarMembro(session, name);

        Map<String, Object> salaJsonAtualizada = salaParaJson(room);

        for (Room.Member membro : room.getMembros().values()) {
            WebSocketSession s = membro.getSessionSocket();
            if (s != null && s.isOpen()) {
                sendJsonMessage(s, "info", "join_room", salaJsonAtualizada, name + " entrou na sala");
            }
        }



    }

    private void sendMessageRoom(Room room, String nomeUsuario) {
        Map<String, String> messageData = new HashMap<>();
        messageData.put("user", nomeUsuario);
        messageData.put("message", nomeUsuario + " entrou na sala");

        for (Room.Member member : room.getMembros().values()) {
            WebSocketSession session = member.getSessionSocket();
            if (session != null && session.isOpen()) {
                sendJsonMessage(session, "info", "user_joined", messageData, nomeUsuario + " entrou na sala");
            } else {
                System.out.println("Sessão fechada ou nula para o membro: " + member.getName());
            }
        }
    }

    private void handleSendMessage(WebSocketSession session, JsonNode data) {
        String message = data.has("message") ? data.get("message").asText() : null;
        String senderName = data.has("name") ? data.get("name").asText() : "Unknown User";

        if (message == null || message.trim().isEmpty()) {
            sendJsonMessage(session, "error", "notify", null, "Mensagem não pode ser vazia.");
            return;
        }

        Room salaDoUsuario = encontrarSalaPorSessao(session);
        if (salaDoUsuario == null) {
            sendJsonMessage(session, "error", "notify", null, "Você não está em nenhuma sala.");
            return;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("user", senderName);
        payload.put("message", message);

        for (Room.Member membro : salaDoUsuario.getMembros().values()) {
            WebSocketSession s = membro.getSessionSocket();
            if (s != null && s.isOpen()) {
                sendJsonMessage(s, "message", "notify", payload, message);
            }
        }
    }

    private void handlePing(WebSocketSession session, JsonNode data) {
        sendJsonMessage(session, "success", "ping", null, "pong");
        showRooms();
    }

    private void sendJsonMessage(WebSocketSession session, String type, String action, Map<String, ?> data, String message) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            var node = mapper.createObjectNode();
            node.put("type", type);
            node.put("action", action);
            if (data != null) node.set("data", mapper.valueToTree(data));
            if (message != null) node.put("message", message);
            session.sendMessage(new TextMessage(node.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean usuarioJaEstaEmAlgumaSala(WebSocketSession session) {
        for (Room room : rooms.values()) {
            for (Room.Member member : room.getMembros().values()) {
                if (member.getSessionSocket().equals(session)) {
                    System.out.println("Usuário já está na sala: " + room.getId());
                    return true;
                }
            }
        }
        return false;
    }

    private Room encontrarSalaPorSessao(WebSocketSession session) {
        for (Room room : rooms.values()) {
            for (Room.Member member : room.getMembros().values()) {
                if (member.getSessionSocket().equals(session)) {
                    return room;
                }
            }
        }
        return null;
    }

    private void showRooms() {
        System.out.println("===== Salas cadastradas =====");
        rooms.forEach((id, room) -> {
            System.out.println("Sala ID: " + id + ", Código: " + room.getCode());
            room.getMembros().forEach((session, member) -> {
                System.out.println(" - Session Socket: " + member.getSessionSocket() + ", Nome: " + member.getName());
            });
        });
    }

    private Map<String, Object> salaParaJson(Room room) {
        Map<String, Object> salaJson = new HashMap<>();
        salaJson.put("roomId", room.getId());
        salaJson.put("code", room.getCode());

        Map<String, Object> membrosJson = new HashMap<>();
        for (Room.Member membro : room.getMembros().values()) {
            Map<String, Object> membroJson = new HashMap<>();
            membroJson.put("name", membro.getName());
            membroJson.put("sessionId", membro.getSessionSocket().getId());
            membrosJson.put(membro.getSessionSocket().getId(), membroJson);
        }

        salaJson.put("members", membrosJson);
        return salaJson;
    }
}
