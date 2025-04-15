package dev.rafael.cadastro.Missoes;

import dev.rafael.cadastro.Ninjas.NinjaModel;
import jakarta.persistence.*;

@Entity
@Table(name = "tb_missoes")
public class MissoesModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private String name;

    private String dificuldade;

    private NinjaModel ninja;

    public MissoesModel(String name, String dificuldade) {
        this.name = name;
        this.dificuldade = dificuldade;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDificuldade() {
        return dificuldade;
    }

    public void setDificuldade(String dificuldade) {
        this.dificuldade = dificuldade;
    }
}
