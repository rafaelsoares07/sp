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
    private static final AtomicInteger roomIdGenerator = new AtomicInteger(1); // Começa em 1
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();


    // Construtor para injeção de dependência
    public MeuHandler(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;

        // Registrar ações e seus respectivos handlers
        actionHandlers.put("create_local_room", this::handleCreateLocalRoom);
        actionHandlers.put("join_room", this::handleJoinRoom);
        actionHandlers.put("ping", this::handlePing);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Mensagem recebida: " + payload);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(payload);

        if (jsonNode.has("action")) {
            String action = jsonNode.get("action").asText();
            System.out.println("Ação recebida: " + action);

            BiConsumer<WebSocketSession, JsonNode> handler = actionHandlers.get(action);

            if (handler != null) {
                handler.accept(session, jsonNode);
            } else {
                session.sendMessage(new TextMessage("Ação desconhecida: " + action));
            }
        } else {
            session.sendMessage(new TextMessage("JSON inválido: campo 'action' não encontrado."));
        }
    }

    private String convertListToJson(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            e.printStackTrace();
            return "Erro ao converter a lista de músicas para JSON";
        }
    }

    private void handleCreateLocalRoom(WebSocketSession session, JsonNode data) {
        try {
            showRooms();

            if(usuarioJaEstaEmAlgumaSala(session)){
                session.sendMessage(new TextMessage("usuário já está em uma sala"));
                return;
            }

            String roomId = String.valueOf(roomIdGenerator.getAndIncrement());
            String code = "ds33"; // Aqui pode ser gerado de forma dinâmica, caso necessário

            Room novaSala = new Room(roomId, code);

            // Vamos pegar o nome do usuário do JSON de entrada
            String name = data.has("name") ? data.get("name").asText() : "Unknown User";
            novaSala.adicionarMembro(session, name);

            rooms.put(roomId, novaSala);

            ObjectMapper mapper = new ObjectMapper();
            String resposta = mapper.createObjectNode()
                    .put("action", "room_created")
                    .put("roomId", roomId)
                    .put("code", code)
                    .toString();

            System.out.println(resposta);
            session.sendMessage(new TextMessage(resposta));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleJoinRoom(WebSocketSession session, JsonNode data) {
        String roomId = data.has("roomId") ? data.get("roomId").asText() : "unknown";
        String name = data.has("name") ? data.get("name").asText() : "Unknown User";
        System.out.println("Entrando na sala: " + roomId);
        Room room = rooms.get(roomId);
        if (room == null) {
            System.out.println("Sala não existe!");
            return;
        }
        room.adicionarMembro(session, name);
        showRooms();
    }



    private void handlePing(WebSocketSession session, JsonNode data) {
        try {
            session.sendMessage(new TextMessage("pong"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean usuarioJaEstaEmAlgumaSala(WebSocketSession session) {
        for (Map.Entry<String, Room> entry : rooms.entrySet()) {
            Room room = entry.getValue();
            for (Room.Member member : room.getMembros().values()) {
                if (member.getSessionSocket().equals(session.getId())) {
                    System.out.println("Usuário já está na sala: " + entry.getKey() +
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
