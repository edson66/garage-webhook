# Garage Webhook API

Aplicação backend para gerenciamento de estacionamento em tempo real, com cálculo de receita, controle de ocupação e processamento de eventos via webhook.

---

##  Tecnologias

- Java 21
- Spring Boot 3.5.11
- MySQL 8
- Flyway
- Spring RestClient
- Docker & Docker Compose

---

## Decisões de Arquitetura

O projeto foi construído focando em código limpo, manutenção e separação de responsabilidades:
- **Arquitetura em Camadas:** Divisão clara entre `controller`, `service`, `repository`, `model` e `dto`.
- **Isolamento de Infraestrutura:** Detalhes técnicos (configurações, WebClients e tratamento de exceções) foram isolados em um pacote `infra`, mantendo o domínio da aplicação puro.
- **Precisão Financeira:** Uso exclusivo de `BigDecimal` para o cálculo de tarifas e descontos, evitando erros de arredondamento de ponto flutuante.
- **Global Exception Handler:** Centralização do tratamento de erros (`@RestControllerAdvice`) para retornar respostas JSON padronizadas e amigáveis em caso de falhas de validação ou regras de negócio.
---

## Funcionalidades

### Sincronização automática

Ao iniciar, a aplicação:

- Consome `GET /garage` de um ou request manuais simulador
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

Para rodar a suíte de testes localmente:

```bash
./mvnw test
```

---

##  Como executar

Basta subir o ambiente completo:

```bash
docker-compose up -d
```

Isso irá iniciar:

- MySQL
- API (porta 3003)

A aplicação sobe automaticamente e começa a processar os eventos.

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
