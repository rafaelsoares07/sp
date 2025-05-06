package dev.rafael.cadastro.musics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MusicDTO {
    private UUID id;
    private String name;
    private String artist;
    private String album;
    private String filePhotoPath;
    private String fileMusicPath;
}
