# CoteFacil Assessment - Sistema de Pedidos com Gateway e JWT

Este projeto consiste em duas APIs REST que se comunicam: uma API Gateway responsável por autenticação JWT e roteamento, e uma API de Pedidos que realiza operações CRUD.

## Tecnologias

- Java 17
- Spring Boot 3.1.5
- Spring Security 6
- JWT (jjwt 0.11.5)
- Spring Data JPA / Hibernate
- H2 Database (em memória)
- OpenAPI / Swagger
- Docker / Docker Compose
- JUnit 5, Mockito, JaCoCo

## Estrutura

- `api-gateway`: porta 8080, responsável por `/auth/login` e proxy para `/api/orders/**`
- `api-orders`: porta 8081, CRUD de pedidos e itens, protegido por JWT

## Como executar

### Pré-requisitos
- Java 17
- Maven
- Docker (opcional)

### Localmente (sem Docker)

1. Clone o repositório
2. Em um terminal, execute a API de Pedidos:
