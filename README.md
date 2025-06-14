# EatEase - Backend

Este projeto foi desenvolvido no âmbito da cadeira de **Projeto 2** e constitui o backend da aplicação EatEase, um sistema de gestão para restaurantes.

## Frontend

O frontend desta aplicação está disponível em: [https://github.com/Maruqes/EatEaseFrontend](https://github.com/Maruqes/EatEaseFrontend)

## Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.4.3**
- **Spring Data JPA** - Para persistência de dados
- **Spring Security** - Para autenticação e autorização
- **PostgreSQL** - Base de dados
- **Thymeleaf** - Template engine para views
- **Hibernate Validator** - Validação de dados
- **Jackson** - Serialização/deserialização JSON
- **SpringDoc OpenAPI** - Documentação da API (Swagger)
- **ZXing** - Geração de códigos QR
- **BCrypt** - Encriptação de passwords
- **Maven** - Gestão de dependências

## Funcionalidades

O sistema EatEase oferece as seguintes funcionalidades:

- **Gestão de Funcionários** - Registo e autenticação de funcionários
- **Gestão de Menus** - Criação e gestão de menus por categorias
- **Gestão de Itens** - Cadastro de pratos e bebidas
- **Gestão de Ingredientes** - Controlo de stock de ingredientes
- **Gestão de Pedidos** - Criação e acompanhamento de pedidos
- **Gestão de Mesas** - Controlo do estado das mesas
- **Sistema de Estados** - Acompanhamento do estado dos pedidos (Pendente, Em Preparação, Pronto, Servido, Cancelado)

## Estrutura do Projeto

```
src/main/java/com/eatease/eatease/
├── controller/     # Controladores REST
├── dto/           # Data Transfer Objects
├── model/         # Entidades JPA
├── repository/    # Repositórios JPA
├── service/       # Lógica de negócio
└── config/        # Configurações
```

## Configuração

1. **Base de Dados**: Configure a ligação PostgreSQL no ficheiro `application.properties`
2. **Dependências**: Execute `mvn clean install` para instalar as dependências
3. **Execução**: Execute `mvn spring-boot:run` para iniciar a aplicação

## API Documentation

A documentação da API está disponível através do Swagger UI em:
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/api-docs`


Projeto desenvolvido no âmbito da cadeira de Projeto 2.
