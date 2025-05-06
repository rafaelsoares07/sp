package dev.rafael.cadastro.sounds;
import dev.rafael.cadastro.config.ResponseSucessAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/musics")
public class MusicController {

    @Autowired
    private MusicServices musicServices;

    @GetMapping
    public ResponseEntity<ResponseSucessAPI<List<MusicDTO>>> findAllMusics() {
        return new ResponseSucessAPI<>(musicServices.findAllMusics(),"Listagem de músicas bem sucedida",HttpStatus.OK).toResponseEntity();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseSucessAPI<MusicDTO>> findMusicById(@PathVariable UUID id) {
        return new ResponseSucessAPI<>(musicServices.findMusicById(id),"Música encontrada com sucesso",HttpStatus.OK).toResponseEntity();
    }

    @PostMapping
    public ResponseEntity<ResponseSucessAPI<MusicDTO>> uploadMusicFile(@RequestParam("fileMusic") MultipartFile fileMusic, @RequestParam("filePhoto") MultipartFile filePhoto, @RequestParam("name") String name, @RequestParam("album") String album, @RequestParam("artist") String artist) {
        return new ResponseSucessAPI<MusicDTO>(musicServices.createMusic(name,album,artist,fileMusic,filePhoto),"Musica criada com sucesso",HttpStatus.CREATED).toResponseEntity();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseSucessAPI<String>> deleteMusic(@PathVariable UUID id){
        return new ResponseSucessAPI<>(musicServices.deleteMusic(id),"Música deletada com sucesso",HttpStatus.OK).toResponseEntity();
    };
}
