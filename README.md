# ChatMQ

Projeto de Middleware Orientado a Mensagens simulando um sistema de troca de mensagens com controle de estado online e offline via Java RMI.

## Pré-requisitos

* Java 21 ou superior
* Maven
* ActiveMQ

## Compilação

Na raiz do projeto, execute o comando abaixo para compilar e gerar os executáveis:

```bash
mvn clean install

```

## Execução

**Importante:** O servidor ActiveMQ deve estar rodando

**Servidor:**

```bash
java -jar target/server.jar

```

**Cliente:**

```bash
java -jar target/client.jar

```