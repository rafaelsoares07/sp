package dev.rafael.cadastro.sounds;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MusicRepository extends JpaRepository<Music, UUID> {
    //herda metodos prontos, como save, delete, dindall, counte
    // consultas costomizadas "findBy[Nome do Campo]" para identificar qual campo você está tentando consultar na sua entidade.
}
