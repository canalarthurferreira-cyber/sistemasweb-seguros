package com.exemplo.gestaousuarios.controller;

import com.exemplo.gestaousuarios.dto.UsuarioDto;
import com.exemplo.gestaousuarios.model.Perfil;
import com.exemplo.gestaousuarios.model.Usuario;
import com.exemplo.gestaousuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Listar todos (Administrador e Operador)
    @GetMapping
    public ResponseEntity<List<UsuarioDto>> listarTodos() {
        List<UsuarioDto> lista = usuarioRepository.findAll().stream()
                .map(UsuarioDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    // 2. Consultar por ID (Regra RBAC para Cliente)
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable String id, Authentication auth) {
        String usuarioLogadoId = auth.getName();
        boolean isCliente = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

        if (isCliente && !usuarioLogadoId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensagem", "Acesso negado. Você só pode visualizar seus próprios dados."));
        }

        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensagem", "Usuário não encontrado."));
        }

        return ResponseEntity.ok(new UsuarioDto(usuario.get()));
    }

    // 3. Cadastrar usuário (Apenas Administrador)
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Usuario novoUsuario) {
        if (usuarioRepository.findByEmail(novoUsuario.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensagem", "E-mail já cadastrado."));
        }

        novoUsuario.setSenha(passwordEncoder.encode(novoUsuario.getSenha()));
        Usuario salvo = usuarioRepository.save(novoUsuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioDto(salvo));
    }

    // 4. Atualizar usuário
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable String id, @RequestBody Usuario dados, Authentication auth) {
        String usuarioLogadoId = auth.getName();
        boolean isCliente = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if (isCliente && !usuarioLogadoId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensagem", "Acesso negado. Você só pode atualizar seus próprios dados."));
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensagem", "Usuário não encontrado."));
        }

        Usuario usuario = usuarioOpt.get();

        if (dados.getNome() != null) usuario.setNome(dados.getNome());
        if (dados.getEmail() != null) usuario.setEmail(dados.getEmail());
        if (dados.getSenha() != null && !dados.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dados.getSenha()));
        }
        if (isAdmin && dados.getPerfil() != null) {
            usuario.setPerfil(dados.getPerfil());
        }

        usuarioRepository.save(usuario);
        return ResponseEntity.ok(new UsuarioDto(usuario));
    }

    // 5. Excluir usuário (Apenas Administrador)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable String id) {
        if (usuarioRepository.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensagem", "Usuário não encontrado."));
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
