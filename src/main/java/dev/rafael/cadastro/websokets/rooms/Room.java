package dev.rafael.cadastro.websokets.rooms;

import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
    private final String id;
    private final String code;
    private final String owner;
    private final Map<WebSocketSession, Member> membros = new ConcurrentHashMap<>();

    // Novos parâmetros para a sala
    private int songTime; // Tempo por música
    private String genre; // Gênero musical
    private int rounds; // Número de rodadas
    private String gameMode; // Modo de jogo

    public Room(String id, String code, String owner) {
        this.id = id;
        this.code = code;
        this.owner = owner;
        this.songTime = 30;  // valor padrão
        this.genre = "Pop";  // valor padrão
        this.rounds = 5;     // valor padrão
        this.gameMode = "Clássico"; // valor padrão
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getOwner() {
        return owner;
    }

    public int getSongTime() {
        return songTime;
    }

    public void setSongTime(int songTime) {
        this.songTime = songTime;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public void adicionarMembro(WebSocketSession session, String name) {
        membros.put(session, new Member(session, name));
    }

    public void removerMembro(WebSocketSession session) {
        membros.remove(session);
    }

    public Map<WebSocketSession, Member> getMembros() {
        return membros;
    }

    // Método para enviar o estado da sala para o cliente
    public String getRoomState() {
        return String.format("{\"roomId\":\"%s\",\"code\":\"%s\",\"songTime\":%d,\"genre\":\"%s\",\"rounds\":%d,\"gameMode\":\"%s\"}",
                id, code, songTime, genre, rounds, gameMode);
    }

    // Classe interna representando o membro
    public static class Member {
        private final WebSocketSession sessionSocket;
        private final String name;

        public Member(WebSocketSession sessionSocket, String name) {
            this.sessionSocket = sessionSocket;
            this.name = name;
        }

        public WebSocketSession getSessionSocket() {
            return sessionSocket;
        }

        public String getName() {
            return name;
        }
    }
}
