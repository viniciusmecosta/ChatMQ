# ChatMQ

Projeto de Middleware Orientado a Mensagens simulando um sistema de troca de mensagens com controle de estado online e offline via Java RMI.

## Pré-requisitos

* Java 21 ou superior
* Maven

## Como compilar

No terminal, acesse a pasta raiz do projeto e execute um dos comandos abaixo para baixar as dependências e gerar os arquivos executáveis separados na pasta `target`:

mvn clean package

Ou:

mvn clean install

## Como executar

Após a compilação, os executáveis individuais estarão disponíveis na pasta `target`. Eles podem ser executados diretamente utilizando o comando `java -jar`.

### Iniciar o Servidor

Abra um terminal na raiz do projeto e execute:

java -jar target/server.jar

### Iniciar o Cliente

Abra um novo terminal na raiz do projeto e execute:

java -jar target/client.jar