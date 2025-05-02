package dev.rafael.cadastro.sounds;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
      return musicById.map(musicMapper::mapToDTO).orElse(null);
      //map(musicMapper::mapToDTO) aplica a conversão Music -> MusicDTO, se Music existir.
    };

    public MusicDTO uploadMusicFile(MusicDTO musicDTO){
        Music music = new MusicMapper().mapToEntity(musicDTO);
        music = musicRepository.save(music);
        return musicMapper.mapToDTO(music);
    }

    public void deleteMusic(UUID id){
        musicRepository.deleteById(id);
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
}
