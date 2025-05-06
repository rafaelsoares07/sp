package dev.rafael.cadastro.musics;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_music")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
//Lombok não gera construtores parciais automaticamente — você precisa definir manualmente ou usar o @Builder.
public class Music {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String artist;

    @Column(nullable = false)
    private String album;

    @Column(nullable = false)
    private String filePhotoPath;

    @Column(nullable = false)
    private String fileMusicPath;

}
