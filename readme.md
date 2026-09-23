# Gestão de Usuários — API REST Segura

API REST para cadastro, consulta, atualização e exclusão de usuários, com autenticação via **JWT** e controle de acesso por perfis (**RBAC**), acompanhada de uma interface web simples para demonstração.

Projeto desenvolvido para a disciplina de APIs REST, Web Services, Autenticação, Autorização e Segurança de Aplicações Web.

## Objetivo do projeto

Disponibilizar uma API REST segura para gerenciamento de usuários, que possa ser integrada por aplicações parceiras, demonstrando na prática:

- Operações CRUD de usuários;
- Autenticação por token JWT;
- Autorização baseada em três perfis de acesso (Administrador, Operador, Cliente);
- Boas práticas de segurança (hash de senha, controle de acesso, stateless auth).

## Tecnologias utilizadas

**Back-end**
- Java 17
- Spring Boot 3.2.3 (Spring Web, Spring Security, Bean Validation)
- JJWT 0.11.5 (`io.jsonwebtoken`) — geração e validação de tokens JWT
- BCrypt — hash de senhas
- Maven

**Front-end**
- HTML, CSS e JavaScript puro (servido como recurso estático pelo próprio Spring Boot)

**Persistência**
- Repositório em memória (`ConcurrentHashMap`), com três usuários pré-cadastrados para fins de demonstração. Não é necessário banco de dados para rodar o projeto localmente.

## Estrutura do projeto

```
src/main/java/com/exemplo/gestaousuarios/
├── GestaoUsuariosApplication.java     # classe principal
├── config/
│   ├── SecurityConfig.java            # regras de autorização por endpoint/role
│   ├── JwtTokenProvider.java          # geração e validação do JWT
│   └── JwtAuthenticationFilter.java   # filtro que lê o token em cada requisição
├── controller/
│   ├── AuthController.java            # POST /api/auth/login
│   └── UsuarioController.java         # CRUD de usuários
├── dto/                                # objetos de entrada/saída (nunca expõem senha)
├── model/                              # Usuario, Perfil (enum)
└── repository/
    └── UsuarioRepository.java         # armazenamento em memória
src/main/resources/
├── application.properties
└── static/index.html                  # front-end de demonstração
```

## Como instalar

**Pré-requisitos:** JDK 17+ e Maven instalados.

```bash
# extrair/clonar o projeto
cd sistemasweb-seguros-main

# baixar dependências e compilar
mvn clean install
```

## Como executar

```bash
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`. O front-end de demonstração é servido automaticamente na raiz (`http://localhost:8080/`).

## Como testar

### Opção 1 — pelo front-end (recomendado)

1. Acesse `http://localhost:8080`.
2. Use um dos usuários pré-cadastrados abaixo para logar.
3. A tela exibe a resposta HTTP bruta de cada operação (status + JSON), útil para acompanhar o funcionamento da API.

| Perfil | E-mail | Senha |
|---|---|---|
| ADMINISTRADOR | admin@sistema.com | Admin123! |
| OPERADOR | operador@sistema.com | Operador123! |
| CLIENTE | cliente@sistema.com | Cliente123! |

### Opção 2 — via Postman/Insomnia

1. `POST /api/auth/login` com `{ "email": "...", "senha": "..." }` → copie o `token` da resposta.
2. Nas demais requisições, envie o header `Authorization: Bearer <token>`.
3. Teste os endpoints com usuários de perfis diferentes para observar as respostas `200/201/204` (permitido) e `403` (negado).

---

## Documentação da API

### 1. Endpoints

| Método | Endpoint | Finalidade | Resposta esperada |
|---|---|---|---|
| POST | `/api/auth/login` | Autenticar usuário e gerar token JWT | `200 OK` / `401 Unauthorized` |
| GET | `/api/usuarios` | Listar todos os usuários | `200 OK` |
| GET | `/api/usuarios/{id}` | Consultar um usuário específico | `200 OK` / `403 Forbidden` / `404 Not Found` |
| POST | `/api/usuarios` | Cadastrar novo usuário | `201 Created` / `400 Bad Request` |
| PUT | `/api/usuarios/{id}` | Atualizar dados de um usuário | `200 OK` / `403 Forbidden` / `404 Not Found` |
| DELETE | `/api/usuarios/{id}` | Excluir um usuário | `204 No Content` / `404 Not Found` |

Implementação: `AuthController.java` e `UsuarioController.java`.

### 2. Segurança com JWT

**Processo de login:** o usuário envia `email` e `senha` em JSON para `POST /api/auth/login`. A senha informada é comparada com o hash BCrypt armazenado (`passwordEncoder.matches(...)`); credenciais inválidas retornam `401`.

**Geração do token:** após validar as credenciais, `JwtTokenProvider.gerarToken()` cria o JWT assinado com o algoritmo `HS256`. A chave de assinatura vem da propriedade `jwt.secret` (variável de ambiente `JWT_SECRET`, com valor padrão apenas para uso local).

**Informações armazenadas no token (claims):**
- `sub` (subject): ID do usuário
- `nome`
- `email`
- `perfil`: ADMINISTRADOR, OPERADOR ou CLIENTE
- `iat` (issued at): data de emissão
- `exp` (expiration): data de expiração

**Política de expiração:** **1 hora**.

*Justificativa:* o sistema gerencia dados sensíveis de usuários (incluindo troca de perfis de acesso), então um tempo curto reduz a janela de uso indevido caso o token seja roubado. Ao mesmo tempo, 1 hora evita que o usuário precise reautenticar a todo instante durante uma sessão normal de uso/demonstração — um equilíbrio entre segurança e usabilidade. Em produção, o ideal seria complementar com *refresh tokens* para renovar a sessão sem exigir novo login.

Implementação: `JwtTokenProvider.java`, `JwtAuthenticationFilter.java`.

### 3. Controle de acesso (RBAC)

Perfis implementados: `ADMINISTRADOR`, `OPERADOR`, `CLIENTE` (enum `Perfil`). As regras são aplicadas em duas camadas: no filtro de segurança (`SecurityConfig`) e, quando necessário, reforçadas dentro do controller (regra "cliente só vê/edita a si mesmo").

| Endpoint | ADMINISTRADOR | OPERADOR | CLIENTE |
|---|---|---|---|
| `GET /api/usuarios` (listar todos) | ✅ | ✅ | ❌ |
| `GET /api/usuarios/{id}` | ✅ (qualquer) | ✅ (qualquer) | ✅ apenas o próprio ID |
| `POST /api/usuarios` (criar) | ✅ | ❌ | ❌ |
| `PUT /api/usuarios/{id}` | ✅ (qualquer, inclui trocar perfil) | ✅ (qualquer, exceto perfil) | ✅ apenas o próprio ID (exceto perfil) |
| `DELETE /api/usuarios/{id}` | ✅ | ❌ | ❌ |

Isso atende diretamente ao pedido: Administrador com acesso total; Operador consultando e atualizando; Cliente restrito aos próprios dados.

Implementação: `SecurityConfig.java` (regras por método HTTP/role) + `UsuarioController.java` (checagens de propriedade do recurso).

### 4. OAuth 2.0 (explicação conceitual, não implementado)

Como o enunciado pede apenas a explicação, sem exigir implementação, o funcionamento seria:

1. **Concessão de acesso:** a aplicação parceira registraria um *client* junto ao servidor de autorização. Para um cenário com usuário final, seria usado o fluxo **Authorization Code**: o usuário é redirecionado para uma tela de login/consentimento do nosso sistema, autoriza o acesso e o servidor de autorização devolve um `authorization code`, trocado em seguida por um `access_token` (e opcionalmente um `refresh_token`). Para integrações servidor-a-servidor sem usuário envolvido, o fluxo **Client Credentials** seria mais adequado.
2. **Utilização de tokens:** a aplicação parceira envia o `access_token` recebido no header `Authorization: Bearer <token>` em cada chamada aos endpoints protegidos — o mesmo mecanismo já usado hoje com o JWT emitido no login.
3. **Benefícios em relação a compartilhar usuário/senha:**
   - A aplicação parceira nunca tem acesso à senha do usuário (apenas a um token com permissões limitadas);
   - É possível delegar permissões específicas (*scopes*), por exemplo "somente leitura", sem dar acesso total à conta;
   - Tokens podem ser revogados ou expirar independentemente da senha, sem exigir que o usuário a troque;
   - Reduz a superfície de ataque, já que a senha nunca trafega para fora do sistema original.

### 5. Análise de Segurança

| Risco | Medida de mitigação | Status |
|---|---|---|
| Roubo/interceptação do token JWT em trânsito | Uso de HTTPS em produção + expiração curta do token (1h) | Expiração implementada; HTTPS a configurar no deploy |
| Senha armazenada em texto puro | Hash com BCrypt (`BCryptPasswordEncoder`) | ✅ Implementado |
| Acesso indevido a endpoints (escalonamento de privilégio) | RBAC por endpoint via `SecurityConfig` + checagem de propriedade do recurso no controller | ✅ Implementado |
| Chave secreta do JWT exposta no código-fonte | Chave movida para `application.properties`/variável de ambiente `JWT_SECRET` | ✅ Implementado |
| Ausência de validação de entrada (dados malformados/e-mail inválido) | Bean Validation (`@Valid`, `@NotBlank`, `@Email`) nos campos de entrada, com tratamento de erro retornando `400` | ✅ Implementado |

### Checklist de avaliação

| Critério do enunciado | Situação |
|---|---|
| Correta aplicação dos princípios REST | ✅ Verbos HTTP e códigos de status corretos |
| Endpoints solicitados (mín. 4) | ✅ 6 endpoints documentados acima |
| Autenticação com JWT | ✅ Login gera token; filtro valida em cada requisição |
| Controle de acesso por perfis (RBAC) | ✅ Três perfis com regras diferenciadas |
| Boas práticas de segurança | ✅ BCrypt, RBAC, chave JWT externalizada e validação de entrada implementados |
| Clareza da documentação | ✅ Este README |
| Funcionamento geral da solução | ✅ Front-end demonstra login, listagem, cadastro, edição e exclusão |
| Capacidade de justificar decisões | ✅ Justificativas registradas nas seções de JWT, OAuth 2.0 e Análise de Segurança |
