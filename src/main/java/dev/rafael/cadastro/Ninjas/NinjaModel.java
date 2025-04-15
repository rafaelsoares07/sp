package dev.rafael.cadastro.Ninjas;

import dev.rafael.cadastro.Missoes.MissoesModel;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "tb_cadastro")
public class NinjaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    private String nome;
    private String email;
    private int idade;

    @ManyToOne
    private MissoesModel missoes;

    public NinjaModel(String nome, String email, int idade) {
        this.nome = nome;
        this.email = email;
        this.idade = idade;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

}









//classes podem virar entidades no banco de dados...
//para tranforma em entity é importante colocar o id como identificador unico de cada registro
//nao precisa colocar o id no contrutor o proprio java ja faz essa implementacao par a gente, nao precisamos passar o id
//baixar dependencia para trabalhar com banco de dados que é o spring jpa e vamos ter acesso a anotacion entity
//como o java vai colocar o id para a gente, precisamos infromar como queromos que ele coloque e devemos colocar a anotation @id no campo de id