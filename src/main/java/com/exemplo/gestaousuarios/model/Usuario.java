package com.exemplo.gestaousuarios.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class Usuario {
    private String id;
    @NotBlank(message = "O nome é obrigatório")
    private String nome;
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail deve ter um formato válido")
    private String email;
    @NotBlank(message = "A senha é obrigatória")
    private String senha;
    private Perfil perfil;

    public Usuario() {}
    public Usuario(String id, String nome, String email, String senha, Perfil perfil) { this.id=id; this.nome=nome; this.email=email; this.senha=senha; this.perfil=perfil; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
}
