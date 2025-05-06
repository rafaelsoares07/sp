package dev.rafael.cadastro.websokets;

import dev.rafael.cadastro.musics.MusicRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

public class MeuHandler extends TextWebSocketHandler {

    private final MusicRepository musicRepository;

    // Construtor para injeção de dependência
    public MeuHandler(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Mensagem recebida: " + payload);

        // Serializa a lista de músicas para uma string JSON
        String musicJson = convertListToJson(musicRepository.findAll());

        // Envia a lista de músicas como uma string JSON
        session.sendMessage(new TextMessage(musicJson));
    }

    // Método para converter uma lista de músicas em JSON
    private String convertListToJson(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            e.printStackTrace();
            return "Erro ao converter a lista de músicas para JSON";
        }
    }
}
