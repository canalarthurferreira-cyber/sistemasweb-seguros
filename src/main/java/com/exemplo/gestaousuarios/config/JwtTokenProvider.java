package com.exemplo.gestaousuarios.config;

import com.exemplo.gestaousuarios.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final Key key = Keys.hmacShaKeyFor("ChaveSecretaSuperSeguraParaGarantirAIntegridadeDoJWT123456!".getBytes());
    private final long validezEmMilisegundos = 3600000; // 1 hora

    public String gerarToken(Usuario usuario) {
        Date agora = new Date();
        Date validade = new Date(agora.getTime() + validezEmMilisegundos);

        return Jwts.builder()
                .setSubject(usuario.getId())
                .claim("nome", usuario.getNome())
                .claim("email", usuario.getEmail())
                .claim("perfil", usuario.getPerfil().name())
                .setIssuedAt(agora)
                .setExpiration(validade)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
