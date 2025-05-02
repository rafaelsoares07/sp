package dev.rafael.cadastro.sounds;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/musics")
public class MusicController {

    @Autowired
    private MusicServices musicServices;

    @GetMapping
    public ResponseEntity<List<MusicDTO>> findAllMusics() {
        return ResponseEntity.status(200).body(musicServices.findAllMusics());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findMusicById(@PathVariable UUID id) {

        MusicDTO musicDTO = musicServices.findMusicById(id);
        if(musicDTO != null){
            musicDTO.setFileMusicPath(System.getenv("API_TESTE")+"sounds/"+musicDTO.getFileMusicPath());
            musicDTO.setFilePhotoPath(System.getenv("API_TESTE")+"photos/"+musicDTO.getFilePhotoPath());
            return ResponseEntity.status(HttpStatus.OK).body(musicDTO);
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SEM NUSICA COM ESSE ID");
        }

    }

    @PostMapping
    public ResponseEntity<?> uploadMusicFile(@RequestParam("fileMusic") MultipartFile fileMusic, @RequestParam("filePhoto") MultipartFile filePhoto, @RequestParam("name") String name, @RequestParam("album") String album, @RequestParam("artist") String artist) {

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


            MusicDTO created = musicServices.uploadMusicFile(musicDTO);
            return ResponseEntity.ok(created);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro ao salvar o arquivo.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMusic(@PathVariable UUID id){
        if(musicServices.findMusicById(id) != null){
            musicServices.deleteMusic(id);
            return ResponseEntity.ok("Musica deletada com sucesso");
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Musica não encontrada");
        }
    };

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMusic(@PathVariable UUID id, @RequestParam("file") MultipartFile file, @RequestParam("name") String name, @RequestParam("album") String album, @RequestParam("artist") String artist){
        MusicDTO musicUpdated = MusicDTO.builder()
                .name(name)
                .artist(artist)
                .album(album)
                .build();

        MusicDTO musicUpdate= musicServices.updateMusic(id,musicUpdated);

        if(musicUpdate != null){
            return ResponseEntity.status(HttpStatus.OK).body(musicUpdate);
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("nao tem musica com esse id");
        }
    }

}




//    @GetMapping("/files")
//    public List<String> listAvailableAudioFiles() throws IOException {
//
//        List<String> sounds = new ArrayList<String>();
//
//        //Classe do Spring que busca recursos (arquivos) no classpath
//        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//        Resource[] resources = resolver.getResources("classpath:sounds/*.mp3");
//
//        for (Resource resource : resources) {
//            String nome = resource.getFilename();
//            if (nome != null && nome.endsWith(".mp3")) {
//                sounds.add(nome);
//            }
//        }
//        return sounds;
//    }
//    @GetMapping("/getAlssl")
//    public ResponseEntity<Resource> getSounds() {
//        try {
//            //ClassPathResource -> Acessa arquivos dentro de resources (texto, images,video,html)
//            ClassPathResource audioFile = new ClassPathResource("sounds/rock-mateusfazenorock-podesereasy.mp3");
//
//            if (!audioFile.exists()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            //contentType -> Informa ao navegador ou outro cliente o formato dos dados que estão sendo enviados
//            return ResponseEntity.ok()
//                    .contentType(MediaType.valueOf("audio/mpeg"))
//                    .body(audioFile);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
