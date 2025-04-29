package dev.rafael.cadastro.sounds;

import org.springframework.core.io.Resource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    @GetMapping("/get")
    public ResponseEntity<Resource> getSound() {
        try {
            //ClassPathResource -> Acessa arquivos dentro de resources (texto, images,video,html)
            ClassPathResource audioFile = new ClassPathResource("sounds/rock-mateusfazenorock-podesereasy.mp3");

            if (!audioFile.exists()) {
                return ResponseEntity.notFound().build();
            }

            //contentType -> Informa ao navegador ou outro cliente o formato dos dados que estão sendo enviados
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("audio/mpeg"))
                    .body(audioFile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/list")
    public List<String> getSoundsList() throws IOException {

        List<String> sounds = new ArrayList<String>();

        //Classe do Spring que busca recursos (arquivos) no classpath
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:sounds/*.mp3");

        for (Resource resource : resources) {
            String nome = resource.getFilename();
            if (nome != null && nome.endsWith(".mp3")) {
                sounds.add(nome);
            }
        }
        return sounds;
    }


    @PostMapping("/add")
    public ResponseEntity<String> addNewSound(@RequestParam("file") MultipartFile file) {

        String UPLOAD_DIR = "sounds";

        try {
            // Cria o diretório de upload se ele não existir
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();  // Cria o diretório se ele não existir
            }


            String projectDir = System.getProperty("user.dir");
            Path path = Paths.get(projectDir, "sounds", file.getOriginalFilename());
            Files.createDirectories(path.getParent());
            file.transferTo(path.toFile());


            return ResponseEntity.ok("Arquivo salvo com sucesso: ");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro ao salvar o arquivo.");
        }

    }

}

