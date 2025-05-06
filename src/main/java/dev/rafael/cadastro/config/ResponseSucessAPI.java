package dev.rafael.cadastro.config;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
public class ResponseSucessAPI<T> {
    private T data;
    private String message;
    private int status;

    public ResponseSucessAPI(T data, String message, HttpStatus status) {
        this.data = data;
        this.message = message;
        this.status = status.value();
    }

    public ResponseEntity<ResponseSucessAPI<T>> toResponseEntity() {
        return ResponseEntity.status(this.status).body(this);
    }
}


//
//
//O que acontece passo a passo?
//        musicServices.createMusic(...) é executado imediatamente.
//
//Isso acontece antes mesmo de o construtor ResponseSucessAPI ser chamado.
//
//O valor retornado por createMusic(...) (que deve ser um MusicDTO) é passado como argumento para o construtor de ResponseSucessAPI