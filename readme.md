# Gestão de Usuários — API REST Segura

Projeto da disciplina Sistemas Web Seguros. Uma API REST em Spring Boot para cadastro, consulta, atualização e exclusão de usuários, com login via JWT e permissões diferentes por perfil de acesso. Inclui uma interface web simples para testar tudo sem precisar de Postman.

## Tecnologias

- Java 17 + Spring Boot 3.2.3 (Web, Security, Validation)
- JJWT para geração/validação do token
- BCrypt para hash de senha
- HTML/CSS/JS puro no front-end
- Dados em memória (sem banco), com 3 usuários já cadastrados para teste

## Estrutura


config/     -> SecurityConfig, JwtTokenProvider, JwtAuthenticationFilter
controller/ -> AuthController (login), UsuarioController (CRUD)
dto/        -> objetos de entrada/saída (a senha nunca volta na resposta)
model/      -> Usuario, Perfil
repository/ -> armazenamento em memória
resources/static/index.html -> front-end da demonstração

## Instalação e execução

Pré-requisito: JDK 17+ e Maven.

```bash
mvn clean install
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`, já com o front-end na raiz.

## Como testar

Usuários pré-cadastrados:

| Perfil | E-mail | Senha |
|---|---|---|
| ADMINISTRADOR | admin@sistema.com | Admin123! |
| OPERADOR | operador@sistema.com | Operador123! |
| CLIENTE | cliente@sistema.com | Cliente123! |

Pelo front-end: logue com qualquer um deles e use as telas de listagem/cadastro/edição/exclusão — a resposta HTTP de cada ação aparece na tela.

Pelo Postman: faça `POST /api/auth/login` com email/senha, pegue o `token` da resposta e mande nas próximas chamadas no header `Authorization: Bearer <token>`. Testando com perfis diferentes dá pra ver na prática os 200/201/204 (permitido) e os 403 (bloqueado.

## Endpoints

| Método | Rota | O que faz | Resposta |
|---|---|---|---|
| POST | /api/auth/login | Autentica e devolve o JWT | 200 / 401 |
| GET | /api/usuarios | Lista todos os usuários | 200 |
| GET | /api/usuarios/{id} | Consulta um usuário | 200 / 403 / 404 |
| POST | /api/usuarios | Cadastra usuário | 201 / 400 |
| PUT | /api/usuarios/{id} | Atualiza usuário | 200 / 403 / 404 |
| DELETE | /api/usuarios/{id} | Remove usuário | 204 / 404 |

## Autenticação com JWT

O login (POST /api/auth/login recebe email e senha, compara com o hash BCrypt salvo e, se bater, gera um token assinado (HS256) contendo id, nome, email, perfil, data de emissão e expiração. A chave de assinatura fica fora do código, em `jwt.secret` / variável de ambiente `JWT_SECRET`.

O token expira em **1 hora**. Como o sistema lida com troca de perfil de acesso, um tempo curto limita o estrago se o token vazar, sem forçar o usuário a logar de novo o tempo todo. O ponto fraco dessa abordagem é que, quando expira, o usuário precisa logar de novo do zero — o ideal seria complementar com um **refresh token** (um token de vida mais longa, usado só para pedir um novo access token sem repetir usuário/senha), que não foi implementado aqui por não ser exigido no escopo do projeto.

Cada requisição protegida passa por um filtro (`JwtAuthenticationFilter`) que lê o header `Authorization: Bearer <token>`, valida assinatura e expiração, e popula o contexto de segurança — não há sessão guardada no servidor, é stateless.

## Controle de acesso (RBAC)

| Ação | ADMINISTRADOR | OPERADOR | CLIENTE |
|---|---|---|---|
| Listar todos | sim | sim | não |
| Consultar usuário | qualquer | qualquer | só o próprio |
| Criar usuário | sim | não | não |
| Editar usuário | qualquer (inclui perfil) | qualquer (exceto perfil) | só o próprio (exceto perfil) |
| Excluir usuário | sim | não | não |


## OAuth 2.0 e OpenID Connect (não implementado)

Hoje o login é feito direto contra a nossa base. Se uma aplicação parceira precisasse acessar a API em nome de um usuário, o caminho recomendado seria delegar a autenticação a um provedor de identidade (ex: Keycloak, Google) via **OpenID Connect**, que é OAuth 2.0 com uma camada de identidade em cima.
Na prática isso mudaria a arquitetura assim: o usuário faz login no provedor (não na nossa API), a aplicação parceira recebe um `authorization code` e troca por um `access_token` (e um `id_token`, que é o que o OpenID Connect adiciona — prova quem é o usuário). Esse token vai no header `Authorization: Bearer` em cada chamada, igual ao JWT atual. A API passaria a validar o token contra o `issuer-uri` do provedor (que confere assinatura, expiração e claims) em vez de emitir o próprio token.

As vantagens sobre o modelo atual: a aplicação parceira nunca vê a senha do usuário; dá pra dar acesso limitado por escopo em vez de acesso total; e o token pode ser revogado no provedor sem precisar trocar a senha do usuário. A troca de token expirado seria feita com um refresh token, sem pedir login de novo.

## Riscos de segurança e mitigação

- **Token JWT roubado em trânsito** — mitigado com expiração curta (1h); em produção precisaria rodar atrás de HTTPS.
- **Senha em texto puro** — evitado usando hash BCrypt em vez de salvar a senha direto.
- **Acesso indevido a endpoints** — mitigado pelo RBAC (SecurityConfig + checagem no controller).
- **Chave do JWT exposta no código** — corrigido movendo para variável de ambiente (`JWT_SECRET`).
- **Dados de entrada inválidos** (email mal formatado, campo vazio) — mitigado com Bean Validation (`@Valid`, `@NotBlank`, `@Email`) retornando 400.
