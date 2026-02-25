# CoteFacil Assessment - Sistema de Pedidos com Gateway e JWT

Este projeto consiste em duas APIs REST desenvolvidas em Java com Spring Boot que se comunicam entre si, implementando autenticação JWT e operações CRUD de pedidos.

- **API Gateway (porta 8080)**: Responsável por autenticação (`/auth/login`) e roteamento de requisições para a API de Pedidos.
- **API de Pedidos (porta 8081)**: Gerencia pedidos e itens de pedido, com endpoints protegidos por JWT.

## Tecnologias Utilizadas

- Java 17
- Spring Boot 3.1.5
- Spring Security 6
- JWT (jjwt 0.11.5)
- Spring Data JPA / Hibernate
- H2 Database (em memória)
- OpenAPI / Swagger
- Docker / Docker Compose
- JUnit 5, Mockito, JaCoCo

## Pré-requisitos

- Java 17 ou superior
- Maven 3.8+
- Docker (opcional, para execução com containers)
- Git

## Como Executar

### 1. Clone o repositório

```bash
git clone <seu-repositorio>
cd cotefacil-assessment


2. Executar localmente (sem Docker)

2.1. Inicie a API de Pedidos

```bash
cd api-orders
mvn spring-boot:run

A API estará disponível em http://localhost:8081.

2.2. Inicie a API Gateway

Em outro terminal:

```bash
cd api-gateway
mvn spring-boot:run

A API Gateway estará disponível em http://localhost:8080.

3. Executar com Docker Compose
Na raiz do projeto, execute:

```bash
docker-compose up --build

As APIs serão iniciadas nos mesmos endereços: Gateway em http://
localhost:8080 e Pedidos em http://localhost:8081.

Credenciais de Teste

Usuário: usuario
Senha: senha123

Documentação Interativa (Swagger)

API Gateway: http://localhost:8080/swagger-ui.html

API de Pedidos: http://localhost:8081/swagger-ui.html

H2 Console (Banco de Dados em Memória)
API Gateway: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:gatewaydb | User: sa | Password: (vazio)

API de Pedidos: http://localhost:8081/h2-console
JDBC URL: jdbc:h2:mem:ordersdb | User: sa | Password: (vazio)

Endpoints e Exemplos de Requisições

1. Autenticação (API Gateway)

POST /auth/login
Gera um token JWT válido por 1 hora.

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "usuario", "password": "senha123"}'

Resposta:

json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}


2. Operações de Pedidos (via Gateway)

Todas as requisições abaixo devem incluir o token no header Authorization: Bearer <token>.


2.1. Criar um pedido

POST /api/orders

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "João Silva",
    "customerEmail": "joao@email.com",
    "items": [
      {
        "productName": "Notebook",
        "quantity": 1,
        "unitPrice": 3500.00
      }
    ]
  }'


2.2. Listar todos os pedidos (com paginação)

GET /api/orders?page=0&size=10

```bash
curl -X GET "http://localhost:8080/api/orders?page=0&size=10" \
  -H "Authorization: Bearer SEU_TOKEN"

2.3. Buscar pedido por ID

GET /api/orders/{id}

```bash
curl -X GET http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer SEU_TOKEN"


2.4. Atualizar um pedido

PUT /api/orders/{id}

bash
curl -X PUT http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "João Silva Atualizado",
    "customerEmail": "joao.novo@email.com",
    "items": [
      {
        "productName": "Mouse",
        "quantity": 2,
        "unitPrice": 50.00
      }
    ]
  }'


2.5. Deletar um pedido (apenas se status = PENDING)

DELETE /api/orders/{id}

```bash
curl -X DELETE http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer SEU_TOKEN"



3. Operações de Itens de Pedido


3.1. Listar itens de um pedido


GET /api/orders/{id}/items

```bash
curl -X GET http://localhost:8080/api/orders/1/items \
  -H "Authorization: Bearer SEU_TOKEN"


3.2. Adicionar item a um pedido

POST /api/orders/{id}/items

```bash
curl -X POST http://localhost:8080/api/orders/1/items \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "Teclado",
    "quantity": 1,
    "unitPrice": 150.00
  }'
Testando a Comunicação entre APIs
A API Gateway atua como proxy: todas as requisições para /api/orders/** são encaminhadas para a API de Pedidos, incluindo o token JWT validado. A API de Pedidos também valida o token antes de processar a requisição.

Executando Testes
Em cada módulo, execute:

bash
mvn test
A cobertura de testes (JaCoCo) pode ser verificada nos relatórios gerados em target/site/jacoco/index.html.

Estrutura do Projeto
text
cotefacil-assessment/
├── api-gateway/          # Código da API Gateway
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── api-orders/           # Código da API de Pedidos
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── docker-compose.yml
├── .github/workflows/ci.yml
└── README.md
Decisões Arquiteturais
Separação em duas APIs: Isola responsabilidades (autenticação/roteamento vs. negócio), permitindo escalabilidade independente.

Proxy via RestTemplate: Solução simples e eficaz para roteamento; em cenários mais complexos, poderia ser substituído por Spring Cloud Gateway.

Validação dupla do token: A API de Pedidos também valida o token recebido, garantindo segurança adicional.

H2 em memória: Facilita execução e testes; em produção, recomenda-se PostgreSQL ou outro banco relacional.

Tratamento global de exceções: Retorna respostas padronizadas com códigos HTTP adequados.

Documentação com Swagger: Interface interativa para explorar e testar os endpoints.

Docker e Docker Compose: Permitem execução consistente em qualquer ambiente.

CI/CD
O projeto inclui um workflow do GitHub Actions (.github/workflows/ci.yml) que executa build e testes em cada push para a branch main.

Observações
O token JWT expira em 1 hora. Para obter um novo, faça login novamente.

A chave secreta JWT está configurada nos arquivos application.yml de ambas as APIs. Em produção, externalize essa chave via variáveis de ambiente.

O CORS está configurado para permitir qualquer origem (*). Ajuste conforme necessidade em produção.

Os dados são armazenados em memória (H2) e serão perdidos ao parar as aplicações. Para persistência, configure um banco permanente.
