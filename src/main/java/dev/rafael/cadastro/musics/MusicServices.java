package dev.rafael.cadastro.musics;


import dev.rafael.cadastro.exceptions.GenericException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    public List<MusicDTO> findAllMusics() {
        List<Music> musics = musicRepository.findAll();
        String basePathSounds = System.getenv("API_TESTE") + "sounds/";
        String basePathPhotos = System.getenv("API_TESTE") + "photos/";

        return musics.stream()
                .map(musicMapper::mapToDTO)
                .peek(musicDTO -> musicDTO.setFileMusicPath(basePathSounds + musicDTO.getFileMusicPath()))
                .peek(musicDTO -> musicDTO.setFilePhotoPath(basePathPhotos + musicDTO.getFilePhotoPath()))
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
        String UPLOAD_DIR_MUSICS = "sounds";
        String UPLOAD_DIR_PHOTOS="photos";

        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new GenericException("Não existe música com esse id", HttpStatus.NOT_FOUND));

        String projectDir = System.getProperty("user.dir");
        File musicFile = new File(projectDir + "/" + UPLOAD_DIR_MUSICS + "/" + music.getFileMusicPath());
        File photoFile = new File(projectDir + "/" + UPLOAD_DIR_PHOTOS + "/" + music.getFilePhotoPath());

        File trashDir = new File(projectDir + "/.trash");
        trashDir.mkdirs();

        // Arquivos temporários
        File tempMusic = new File(trashDir, music.getFileMusicPath());
        File tempPhoto = new File(trashDir, music.getFilePhotoPath());

        try {
            // Move para a lixeira
            if (musicFile.exists()) Files.move(musicFile.toPath(), tempMusic.toPath(), StandardCopyOption.REPLACE_EXISTING);
            if (photoFile.exists()) Files.move(photoFile.toPath(), tempPhoto.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Deleta do banco (pode lançar exception, por isso dentro do try)
            musicRepository.deleteById(id);

            // Deleta permanentemente os arquivos da lixeira
            tempMusic.delete();
            tempPhoto.delete();

            return "Música deletada com sucesso";

        } catch (Exception e) {
            // Em caso de erro, tenta restaurar os arquivos
            try {
                if (tempMusic.exists()) Files.move(tempMusic.toPath(), musicFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                if (tempPhoto.exists()) Files.move(tempPhoto.toPath(), photoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new GenericException("Erro ao restaurar arquivos após falha: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            throw new GenericException("Erro ao deletar música: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

            String originalMusicName = fileMusic.getOriginalFilename().trim().replaceAll("\\s+", "_");
            String originalPhotoName = filePhoto.getOriginalFilename().trim().replaceAll("\\s+", "_");

            // Salva o arquivo de música
            Path musicPath = Paths.get(projectDir, UPLOAD_DIR_MUSICS, originalMusicName);
            Files.createDirectories(musicPath.getParent());
            fileMusic.transferTo(musicPath.toFile());

            // Cria o diretório de upload se ele não existir
            File photoDir = new File(projectDir, UPLOAD_DIR_PHOTOS);
            if (!photoDir.exists()) {
                photoDir.mkdirs();
            }

            // Salva o arquivo de foto
            Path photoPath = Paths.get(projectDir, UPLOAD_DIR_PHOTOS, originalPhotoName);
            Files.createDirectories(photoPath.getParent());
            filePhoto.transferTo(photoPath.toFile());

            // Cria a música (supondo que Music tenha um construtor com name, artist, album)
            MusicDTO musicDTO = new MusicDTO();
            musicDTO.setName(name);
            musicDTO.setAlbum(album);
            musicDTO.setArtist(artist);
            musicDTO.setFilePhotoPath(photoPath.toFile().getName());
            musicDTO.setFileMusicPath(musicPath.toFile().getName());

            Optional<Music> musicExist = musicRepository.findByName(name);

            if(musicExist.isPresent()){
                throw new GenericException("Já existe uma música com esse nome", HttpStatus.CONFLICT);
            }

            MusicDTO created = this.uploadMusicFile(musicDTO);
            return created;

        } catch (IOException e) {
            throw new GenericException("err",HttpStatus.BAD_REQUEST);
        }
    }
}
