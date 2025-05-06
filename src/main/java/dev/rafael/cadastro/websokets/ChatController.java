package dev.rafael.cadastro.websokets;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/message") // Cliente envia para: /websocket/mensagem
    @SendTo("/topic/respostas")  // Broadcast para todos que ouvem: /topic/respostas
    public String receberMensagem(@Payload String mensagem) {
        System.out.println("Mensagem recebida: " + mensagem);
        return "Servidor recebeu: " + mensagem;
    }
}

