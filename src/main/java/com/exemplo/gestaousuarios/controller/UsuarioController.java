package com.exemplo.gestaousuarios.controller;

import com.exemplo.gestaousuarios.dto.UsuarioDto;
import com.exemplo.gestaousuarios.model.Usuario;
import com.exemplo.gestaousuarios.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
    public UsuarioController(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder) { this.usuarioRepository=usuarioRepository; this.passwordEncoder=passwordEncoder; }
    @GetMapping public ResponseEntity<List<UsuarioDto>> listarTodos() { return ResponseEntity.ok(usuarioRepository.findAll().stream().map(UsuarioDto::new).collect(Collectors.toList())); }
    @GetMapping("/{id}") public ResponseEntity<?> buscarPorId(@PathVariable String id, Authentication auth) {
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE")) && !auth.getName().equals(id)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("mensagem", "Acesso negado. Você só pode visualizar seus próprios dados."));
        Optional<Usuario> usuario=usuarioRepository.findById(id); if (usuario.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensagem", "Usuário não encontrado.")); return ResponseEntity.ok(new UsuarioDto(usuario.get()));
    }
    @PostMapping public ResponseEntity<?> criar(@Valid @RequestBody Usuario novoUsuario) {
        if (usuarioRepository.findByEmail(novoUsuario.getEmail()).isPresent()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensagem", "E-mail já cadastrado."));
        novoUsuario.setSenha(passwordEncoder.encode(novoUsuario.getSenha())); return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioDto(usuarioRepository.save(novoUsuario)));
    }
    @PutMapping("/{id}") public ResponseEntity<?> atualizar(@PathVariable String id, @RequestBody Usuario dados, Authentication auth) {
        boolean cliente=auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE")); boolean admin=auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        if (cliente && !auth.getName().equals(id)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("mensagem", "Acesso negado. Você só pode atualizar seus próprios dados."));
        Optional<Usuario> opt=usuarioRepository.findById(id); if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensagem", "Usuário não encontrado."));
        Usuario usuario=opt.get(); if (dados.getNome()!=null) usuario.setNome(dados.getNome()); if (dados.getEmail()!=null) usuario.setEmail(dados.getEmail()); if (dados.getSenha()!=null && !dados.getSenha().isBlank()) usuario.setSenha(passwordEncoder.encode(dados.getSenha())); if (admin && dados.getPerfil()!=null) usuario.setPerfil(dados.getPerfil());
        return ResponseEntity.ok(new UsuarioDto(usuarioRepository.save(usuario)));
    }
    @DeleteMapping("/{id}") public ResponseEntity<?> excluir(@PathVariable String id) { if (usuarioRepository.findById(id).isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensagem", "Usuário não encontrado.")); usuarioRepository.deleteById(id); return ResponseEntity.noContent().build(); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> tratarValidacao(MethodArgumentNotValidException ex) { String mensagem=ex.getBindingResult().getFieldErrors().stream().map(e -> e.getDefaultMessage()).findFirst().orElse("Dados inválidos."); return ResponseEntity.badRequest().body(Map.of("mensagem", mensagem)); }
}
