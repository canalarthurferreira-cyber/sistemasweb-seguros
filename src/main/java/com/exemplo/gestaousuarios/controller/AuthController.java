package com.exemplo.gestaousuarios.controller;

import com.exemplo.gestaousuarios.config.JwtTokenProvider;
import com.exemplo.gestaousuarios.dto.LoginDto;
import com.exemplo.gestaousuarios.dto.TokenDto;
import com.exemplo.gestaousuarios.model.Usuario;
import com.exemplo.gestaousuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(loginDto.getEmail());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensagem", "Credenciais inválidas."));
        }

        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(loginDto.getSenha(), usuario.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensagem", "Credenciais inválidas."));
        }

        String token = tokenProvider.gerarToken(usuario);
        TokenDto tokenDto = new TokenDto(token, usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil().name());

        return ResponseEntity.ok(tokenDto);
    }
}
