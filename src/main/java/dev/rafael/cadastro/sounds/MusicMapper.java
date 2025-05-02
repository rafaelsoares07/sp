package dev.rafael.cadastro.sounds;

import org.springframework.stereotype.Component;

@Component
public class MusicMapper {

    //transformar dto em entity
    public Music mapToEntity(MusicDTO musicDTO){
        Music music = new Music();
        music.setId(musicDTO.getId());
        music.setName(musicDTO.getName());
        music.setAlbum(musicDTO.getAlbum());
        music.setArtist(musicDTO.getArtist());
        music.setFileMusicPath(musicDTO.getFileMusicPath());
        music.setFilePhotoPath(musicDTO.getFilePhotoPath());

        return music;
    }

    //tranformar entity em DTO
    public MusicDTO mapToDTO(Music music){
        MusicDTO musicDTO = new MusicDTO();
        musicDTO.setId(music.getId());
        musicDTO.setName(music.getName());
        musicDTO.setAlbum(music.getAlbum());
        musicDTO.setArtist(music.getArtist());
        musicDTO.setFileMusicPath(music.getFileMusicPath());
        musicDTO.setFilePhotoPath(music.getFilePhotoPath());

        return musicDTO;
    }

}
