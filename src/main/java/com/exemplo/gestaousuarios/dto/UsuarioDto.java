package com.exemplo.gestaousuarios.dto;

import com.exemplo.gestaousuarios.model.Perfil;
import com.exemplo.gestaousuarios.model.Usuario;

public class UsuarioDto {
    private String id;
    private String nome;
    private String email;
    private Perfil perfil;

    public UsuarioDto(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.perfil = usuario.getPerfil();
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public Perfil getPerfil() { return perfil; }
}
