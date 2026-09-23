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
- Spring Boot 3.2.3 (Spring Web, Spring Security)
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

A lista completa de endpoints, o funcionamento do JWT, o RBAC, a explicação sobre OAuth 2.0 e a análise de segurança estão em **[INSTRUCOES-CORRECAO.md](./INSTRUCOES-CORRECAO.md)**.
