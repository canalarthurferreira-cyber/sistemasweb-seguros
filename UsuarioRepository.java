package com.exemplo.gestaousuarios.repository;

import com.exemplo.gestaousuarios.model.Perfil;
import com.exemplo.gestaousuarios.model.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UsuarioRepository {
    private final Map<String, Usuario> usuarios = new ConcurrentHashMap<>();

    public UsuarioRepository() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Usuários iniciais para demonstração
        Usuario admin = new Usuario("1", "Administrador do Sistema", "admin@sistema.com", encoder.encode("Admin123!"), Perfil.ADMINISTRADOR);
        Usuario operador = new Usuario("2", "Operador de Suporte", "operador@sistema.com", encoder.encode("Operador123!"), Perfil.OPERADOR);
        Usuario cliente = new Usuario("3", "Cliente Exemplo", "cliente@sistema.com", encoder.encode("Cliente123!"), Perfil.CLIENTE);

        usuarios.put(admin.getId(), admin);
        usuarios.put(operador.getId(), operador);
        usuarios.put(cliente.getId(), cliente);
    }

    public List<Usuario> findAll() {
        return new ArrayList<>(usuarios.values());
    }

    public Optional<Usuario> findById(String id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    public Optional<Usuario> findByEmail(String email) {
        return usuarios.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public Usuario save(Usuario usuario) {
        if (usuario.getId() == null || usuario.getId().isEmpty()) {
            usuario.setId(UUID.randomUUID().toString());
        }
        usuarios.put(usuario.getId(), usuario);
        return usuario;
    }

    public void deleteById(String id) {
        usuarios.remove(id);
    }
}
