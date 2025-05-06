package dev.rafael.cadastro.sounds;


import dev.rafael.cadastro.exceptions.GenericException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service

public class MusicServices {

    //injeção de dependências, anotation substitue o construtot
    @Autowired
    private MusicRepository musicRepository;
    @Autowired
    private MusicMapper musicMapper;

//    public MusicServices(MusicRepository musicRepository) {
//        this.musicRepository = musicRepository;
//    }

    public List<MusicDTO> findAllMusics(){
        List<Music> musics= musicRepository.findAll();

        return musics.stream()
                .map(musicMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    public MusicDTO findMusicById(UUID id){
        Optional<Music> musicById = musicRepository.findById(id);
        MusicDTO musicDTO = musicById.map(musicMapper::mapToDTO).orElse(null);

        if (musicDTO == null) {
            throw new GenericException("Música não encontrada", HttpStatus.NOT_FOUND);
        }

        musicDTO.setFileMusicPath(System.getenv("API_TESTE") + "sounds/" + musicDTO.getFileMusicPath());
        musicDTO.setFilePhotoPath(System.getenv("API_TESTE") + "photos/" + musicDTO.getFilePhotoPath());

        return musicDTO;
    };

    public MusicDTO uploadMusicFile(MusicDTO musicDTO){
        Music music = new MusicMapper().mapToEntity(musicDTO);
        music = musicRepository.save(music);
        return musicMapper.mapToDTO(music);
    }

    public String deleteMusic(UUID id){
        musicRepository.findById(id).orElseThrow(() -> new GenericException("Não existe música com esse id", HttpStatus.NOT_FOUND));
        musicRepository.deleteById(id);
        return null;
    }

    public MusicDTO updateMusic(UUID id, MusicDTO musicUpdateDTO){
        Optional<Music> musicExist = musicRepository.findById(id);
        if(musicExist.isPresent()){
            Music music = musicMapper.mapToEntity(musicUpdateDTO);
            music.setId(id);
            Music musicSave = musicRepository.save(music);
            return musicMapper.mapToDTO(musicSave);
        }
        return null;
    }

    public  MusicDTO createMusic(String name, String album, String artist, MultipartFile fileMusic, MultipartFile filePhoto) {
        String UPLOAD_DIR_MUSICS = "sounds";
        String UPLOAD_DIR_PHOTOS="photos";

        try {
            String projectDir = System.getProperty("user.dir");

            File musicDir = new File(projectDir, UPLOAD_DIR_MUSICS);
            if (!musicDir.exists()) {
                musicDir.mkdirs();
            }

            // Salva o arquivo de música
            Path musicPath = Paths.get(projectDir, UPLOAD_DIR_MUSICS, fileMusic.getOriginalFilename());
            Files.createDirectories(musicPath.getParent());
            fileMusic.transferTo(musicPath.toFile());

            // Cria o diretório de upload se ele não existir
            File photoDir = new File(projectDir, UPLOAD_DIR_PHOTOS);
            if (!photoDir.exists()) {
                photoDir.mkdirs();
            }

            // Salva o arquivo de foto
            Path photoPath = Paths.get(projectDir, UPLOAD_DIR_PHOTOS, filePhoto.getOriginalFilename());
            Files.createDirectories(photoPath.getParent());
            filePhoto.transferTo(photoPath.toFile());

            // Cria a música (supondo que Music tenha um construtor com name, artist, album)
            MusicDTO musicDTO = new MusicDTO();
            musicDTO.setName(name);
            musicDTO.setAlbum(album);
            musicDTO.setArtist(artist);
            musicDTO.setFilePhotoPath(photoPath.toFile().getName());
            musicDTO.setFileMusicPath(musicPath.toFile().getName());

            MusicDTO created = this.uploadMusicFile(musicDTO);
            return created;

        } catch (IOException e) {
            throw new GenericException("err",HttpStatus.BAD_REQUEST);
        }
    }
}
