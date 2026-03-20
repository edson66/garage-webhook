# Estapar Garage Webhook API

Aplicação backend para gerenciamento de estacionamento em tempo real, com cálculo de receita, controle de ocupação e processamento de eventos via webhook.

---

##  Tecnologias

- Java 21
- Spring Boot 3.x
- MySQL 8
- Flyway
- Spring RestClient
- Docker & Docker Compose

---

## Funcionalidades

### Sincronização automática

Ao iniciar, a aplicação:

- Consome `GET /garage` do simulador
- Limpa dados transacionais (sessões)
- Realiza **upsert** de setores e vagas

---

### Webhook

**POST /webhook**

Recebe eventos:

- `ENTRY`
- `PARKED`
- `EXIT`

Processa regras como controle de ocupação e cálculo de permanência de forma resiliente.

---

### Receita

**GET /revenue**

Calcula a receita por setor e data, utilizando query otimizada com `COALESCE`.

---

## Documentação

Swagger disponível em:

 http://localhost:3003/swagger-ui.html

---

## Testes

O projeto possui:

- Testes unitários para regras de negócio
- Testes de integração com banco real utilizando Testcontainers

Isso garante maior confiabilidade e fidelidade ao ambiente de produção.

---

##  Como executar

Basta subir o ambiente completo:

```bash
docker-compose up -d
```

Isso irá iniciar:

- MySQL
- Simulador
- API (porta 3003)

A aplicação já sobe automaticamente e começa a processar os eventos do simulador.

---

### Parando a simulação para testes manuais

Caso queira interromper o envio automático de eventos para testar o endpoint de receita (`GET /revenue`) ou validar o banco de dados com calma, você pode parar apenas o container do simulador:

```bash
docker stop garage-sim
```

---


## Endpoints

### Webhook

**POST /webhook**

---

###  Receita

**GET /revenue**

#### Requisição:

```json
{
  "date": "2025-01-01",
  "sector": "A"
}
```

#### Resposta:

```json
{
  "amount": 40.50,
  "currency": "BRL",
  "timestamp": "2025-01-01T00:00:00Z"
}
```
