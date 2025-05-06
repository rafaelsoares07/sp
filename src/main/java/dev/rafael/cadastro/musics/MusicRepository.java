package dev.rafael.cadastro.musics;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MusicRepository extends JpaRepository<Music, UUID> {

    Optional<Music> findByName(String name);
    //herda metodos prontos, como save, delete, dindall, counte
    // consultas costomizadas "findBy[Nome do Campo]" para identificar qual campo você está tentando consultar na sua entidade.
}
