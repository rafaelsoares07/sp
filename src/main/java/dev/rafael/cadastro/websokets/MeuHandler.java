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
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        showRooms();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(payload);

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
            showRooms();

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

            Map<String, String> responseData = new HashMap<>();
            responseData.put("roomId", roomId);
            responseData.put("code", code);

            sendJsonMessage(session, "success", "room_created", responseData, "Sala criada com sucesso");
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
        showRooms();

        Map<String, String> responseData = new HashMap<>();
        responseData.put("roomId", roomId);
        responseData.put("message", "Entrou na sala com sucesso");

        sendJsonMessage(session, "success", "join_room", responseData, "Usuário adicionado à sala");
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
            if (data != null) {
                node.set("data", mapper.valueToTree(data));
            }
            if (message != null) {
                node.put("message", message);
            }
            session.sendMessage(new TextMessage(node.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean usuarioJaEstaEmAlgumaSala(WebSocketSession session) {
        for (Room room : rooms.values()) {
            for (Room.Member member : room.getMembros().values()) {
                if (member.getSessionSocket().equals(session.getId())) {
                    System.out.println("Usuário já está na sala: " + room.getId() +
                            " - Nome: " + member.getName() +
                            " - Session Socket: " + member.getSessionSocket());
                    return true;
                }
            }
        }
        return false;
    }

    private void showRooms() {
        System.out.println("===== Salas cadastradas =====");
        rooms.forEach((id, room) -> {
            System.out.println("Sala ID: " + id + ", Código: " + room.getCode());
            System.out.println("Membros:");
            room.getMembros().forEach((session, member) -> {
                System.out.println(" - Session Socket: " + member.getSessionSocket() + ", Nome: " + member.getName());
            });
        });
    }
}
