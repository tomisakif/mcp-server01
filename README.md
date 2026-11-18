<!--
Markdown Cheatsheet
https://github.com/adam-p/markdown-here/wiki/markdown-cheatsheet
-->

## 🚀 Sistema de Gerenciamento de Documentos e Relatórios via MCP 🚀

Este projeto implementa um sistema de gerenciamento de documentos e relatórios utilizando o Protocolo de Contexto do Modelo ([MCP](https://modelcontextprotocol.io/)) e o [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk).

A aplicação permite criar, consultar, listar, atualizar, excluir e buscar documentos por meio de ferramentas disponibilizadas por um servidor MCP desenvolvido em Java.

O projeto utiliza a comunicação STDIO entre o cliente e o servidor MCP, além de persistir os documentos em um arquivo JSON.

A solução contempla seis ferramentas MCP:

- `criar_documento`: cria um novo documento.
- `obter_documento`: consulta um documento pelo seu identificador.
- `listar_documentos`: apresenta os documentos cadastrados.
- `atualizar_documento`: atualiza os dados de um documento.
- `excluir_documento`: exclui um documento pelo seu identificador.
- `buscar_documentos`: pesquisa documentos pelo título.

O servidor pode ser utilizado por meio do cliente Java desenvolvido no projeto, do MCP Inspector e do Claude Desktop.

### 🛠️ Ferramentas MCP disponibilizadas

O servidor MCP disponibiliza seis ferramentas para o gerenciamento de documentos:

| Ferramenta | Descrição |
|---|---|
| `criar_documento` | Cria um documento informando título e descrição. O status inicial é definido automaticamente como `PENDENTE`. |
| `obter_documento` | Recupera um documento por meio de seu identificador (ID). |
| `listar_documentos` | Retorna todos os documentos cadastrados. |
| `atualizar_documento` | Atualiza o título, a descrição e o status de um documento existente. |
| `excluir_documento` | Remove um documento por meio de seu identificador (ID). |
| `buscar_documentos` | Pesquisa documentos pelo título, sem diferenciar letras maiúsculas e minúsculas. |

### 📄 Estrutura dos documentos

Cada documento possui os seguintes atributos:

- **ID:** identificador numérico do documento.
- **Título:** nome ou título do documento.
- **Descrição:** informações descritivas do documento.
- **Status:** situação atual, podendo ser `PENDENTE`, `EM_ANDAMENTO` ou `FINALIZADO`.
- **Data de criação:** data e horário em que o documento foi criado.
- **Data de atualização:** data e horário da última atualização do documento.

### 💾 Persistência dos dados

Os documentos são armazenados em formato JSON no arquivo `data/documentos.json`, localizado no diretório de trabalho configurado para o servidor.

O servidor permite definir outro caminho para o arquivo por meio da propriedade Java `documentos.arquivo`. Caso essa propriedade não seja informada, será utilizado o caminho padrão `data/documentos.json`.

### 📌 Executar a aplicação 📌
Este é um projeto único para duas aplicações: servidor e cliente. O servidor é executado a partir da classe `br.edu.ifpr.mcp_server.main.McpServer01SyncStdio`. Já o cliente é executado a partir da classe `br.edu.ifpr.mcp_server.main.McpClient01SyncStdio`.

Inicialmente, é preciso gerar a aplicação servidor e, em seguida, executar o cliente, tanto no Eclipse IDE quanto em linha de comando (CLI).

Além disto, o servidor MCP Java pode ser integrado a assistentes de IA compatíveis com o protocolo MCP. Neste projeto, a integração foi implementada e testada com o Claude Desktop.

Também é possível verificar e testar o servidor MCP Java com o MCP Inspector. O MCP Inspector é uma ferramenta do ecossistema Model Context Protocol para testar e depurar servidores MCP. Ela disponibiliza interface web, CLI e TUI. Mais adiante, este documento mostra os passos necessários para testar o servidor MCP Java com o MCP Inspector rodando como um contêiner Docker no Docker Desktop, juntamente com os arquivos Dockerfile e compose.yaml, disponíveis na raiz do projeto.

#### ⚙️ Eclipse IDE ⚙️
1. Clicar com o botão direito do mouse no nome do projeto;
2. Selecionar **Run As** > **Maven build...**
3. Na janela **Edit configuration and launch**:
    * Na aba **Main** digitar **clean package -Pserver -DskipTests** no campo **Goals**
    * Na aba **JRE** digitar **--enable-native-access=ALL-UNNAMED** no campo **VM arguments**
4. Clicar no botão **Run**

A configuração de execução Maven pode ser criada na primeira execução e reutilizada nas seguintes. Sempre que houver alterações no código ou necessidade de gerar novamente o artefato, execute a configuração correspondente. Para reutilizá-la, clique na seta ao lado do botão Run e selecione a configuração desejada.

Se o processo de construção da aplicação servidor ocorreu sem erros, ele produzirá o diretório (pasta) **server** na raiz do projeto. Este diretório contém a aplicação executável do servidor e o sub-diretório **libs** contendo as bibliotecas de código necessárias ao servidor.

**A aplicação servidor é executada pela aplicação cliente e não precisa de intervenção do programador.**

Para gerar a aplicação cliente, deve-se executar os quatro passos mostrados anteriormente, com a seguinte diferença: na aba **Main**, digitar **clean package -Pclient -DskipTests** no campo **Goals**. A configuração pode ser reutilizada sempre que for necessário gerar novamente a aplicação cliente, especialmente após alterações no código ou quando o objetivo Maven `clean` remover o diretório `target`.

Se o processo de construção da aplicação cliente ocorreu sem erros, ele produzirá o diretório **dist-client** como sub-diretório de **target**. O diretório **dist-client** contém a aplicação cliente e o sub-diretório **libs** com as bibliotecas necessárias à sua execução.

#### ⚙️ Linha de Comando (CLI) ⚙️
Abrir uma janela do terminal (por exemplo, o Prompt de Comando do Windows), mudar para o diretório raiz do projeto e executar os comandos para gerar as aplicações:

```
# gerar a aplicação servidor
mvn clean package -Pserver -DskipTests
```
```
# gerar a aplicação cliente
mvn clean package -Pclient -DskipTests
```

Independentemente do modo de execução do processo de construção (*build*), se realizado no Eclipse IDE ou em CLI, uma vez que ambas as aplicações tenham sido criadas, o programador deve abrir uma janela do terminal e mudar para o diretório **target/dist-client**, que fica dentro do diretório raiz do projeto. Estando neste diretório digitar **java -jar mcp_server01_client.jar**, pressionar a tecla ENTER <img width="20" height="20" src="https://img.icons8.com/color/48/enter-key.png" alt="enter-key"/> e aguardar a execução do cliente que, por sua vez, executará o servidor e apresentará ao usuário um menu para interagir com a(s) ferramenta(s) disponibilizadas pelo servidor.

---

**OBSERVAÇÃO IMPORTANTE**

O objetivo Maven `clean` remove o diretório `target` e todo o seu conteúdo.

Como a aplicação cliente é gerada no diretório `target/dist-client`, sempre que uma nova construção utilizar o objetivo `clean`, o cliente gerado anteriormente será removido e deverá ser construído novamente.

Assim, caso seja necessário gerar novamente o servidor após a construção do cliente, será preciso gerar também o cliente antes de utilizá-lo.

---

### 🧪 Executar os Testes Automatizados 🧪

O projeto possui testes automatizados unitários e de integração, localizados no diretório `src/test/java`.

**Testes unitários**

Os testes unitários verificam individualmente o comportamento das classes responsáveis pela persistência dos documentos e pelas regras de negócio do sistema.

Entre as classes de teste estão:

- `JsonDocumentoRepositoryTest`: verifica as operações de leitura, gravação, atualização e exclusão de documentos no arquivo JSON.
- `DocumentoServiceTest`: verifica as operações e regras de negócio relacionadas ao gerenciamento de documentos.

**Testes de integração**

A classe `McpServer01IT` contém testes de integração que verificam a comunicação com o servidor MCP e a execução das ferramentas disponibilizadas.

**Executar os testes**

Para executar todos os testes automatizados, utilizar o seguinte comando na raiz do projeto:

```bash
mvn clean verify -Pserver
```

No Eclipse IDE, selecionar **Run As > Maven build...** e informar o mesmo comando no campo **Goals**.

O comando executa as fases de compilação, testes unitários, empacotamento e testes de integração, conforme a configuração Maven do projeto.

**Resultados dos testes**

Na última execução completa, foram obtidos os seguintes resultados:

- Testes unitários: 22 aprovados.
- Testes de integração: 15 aprovados.
- Total: 37 testes aprovados, sem falhas ou erros.

Resultado da execução: `BUILD SUCCESS`.

---

#### ⚙️ MCP Inspector no Docker Desktop ⚙️

O MCP Inspector é uma ferramenta que permite testar e depurar servidores MCP por meio de uma interface gráfica.

Neste projeto, o MCP Inspector é executado em um contêiner Docker, utilizando o Docker Desktop.

**Pré-requisitos**

- Docker Desktop instalado e em execução.
- Aplicação servidor MCP previamente gerada.

**Iniciar o MCP Inspector**

1. Abrir uma janela do terminal, como o Prompt de Comando do Windows.
2. Mudar para o diretório raiz do projeto.
3. Executar o comando:

```bash
docker compose up -d --build
```

4. Aguardar a criação da imagem Docker, caso necessário, e a inicialização do contêiner.
5. Abrir o navegador e acessar:

`http://127.0.0.1:6274`

**Utilização**

Após a inicialização, o MCP Inspector permite estabelecer conexão com o servidor MCP Java e executar as seis ferramentas disponibilizadas pelo projeto.

**Persistência dos documentos**

Os documentos são armazenados no arquivo `data/documentos.json`, localizado no diretório raiz do projeto.

O arquivo é disponibilizado ao contêiner por meio de um volume configurado no arquivo `compose.yaml`, permitindo preservar os dados mesmo após a parada ou recriação do contêiner.

**Encerrar o MCP Inspector**

Para encerrar o serviço e remover o contêiner e a rede criados pelo Docker Compose, executar o seguinte comando no diretório raiz do projeto:

```bash
docker compose down
```

Para iniciar novamente o serviço, basta executar o comando `docker compose up -d --build`.

---

### 🤖 Instalar o servidor MCP local no [Claude Desktop](https://claude.com/)

O servidor MCP deste projeto pode ser integrado ao Claude Desktop, permitindo utilizar as ferramentas de gerenciamento de documentos diretamente no assistente.

**1. Localizar o arquivo de configuração**

No Claude Desktop:

1. Acessar **Settings**.
2. Selecionar **Developer**.
3. Clicar em **Edit Config**.
4. Abrir o arquivo `claude_desktop_config.json`.

**2. Configurar o servidor MCP**

Adicionar a configuração do servidor dentro da propriedade `mcpServers`:

```json
{
  "mcpServers": {
    "mcp-server01": {
      "command": "java",
      "args": [
        "-Ddocumentos.arquivo=C:\\caminho\\para\\mcp-server01\\data\\documentos.json",
        "-jar",
        "C:\\caminho\\para\\mcp-server01\\server\\mcp_server01_server.jar"
],
      "env": {}
    }
  }
}
```

**Observação:** Substitua `C:\\caminho\\para\\mcp-server01` pelo caminho absoluto do diretório do projeto em seu computador. No Windows, utilize duas barras invertidas (`\\`) nos caminhos do arquivo JSON.

**Observações importantes:**

- O argumento `-Ddocumentos.arquivo` define o caminho absoluto do arquivo JSON utilizado para armazenar os documentos.
- A propriedade `-Ddocumentos.arquivo` deve ser informada antes do argumento `-jar`.
- O arquivo JAR do servidor deve ser previamente gerado por meio do Maven.
- O Java (JDK ou JRE) deve estar instalado e disponível no sistema.

**3. Reiniciar o Claude Desktop**

Após salvar o arquivo de configuração, encerrar completamente o Claude Desktop e iniciá-lo novamente.

**4. Verificar a conexão**

Acessar **Settings > Developer** e verificar se o servidor `mcp-server01` aparece com o status **Em execução**.

Com a conexão estabelecida, as ferramentas do servidor MCP estarão disponíveis para utilização no Claude Desktop.

---

#### 📘 REFERÊNCIAS E MATERIAIS COMPLEMENTARES 📘

**Documentação oficial**

- [Model Context Protocol (MCP) – Documentação Oficial](https://modelcontextprotocol.io/)
- [MCP Java SDK – Repositório oficial no GitHub](https://github.com/modelcontextprotocol/java-sdk)

**Materiais complementares**

- [Building a JDBC-based MCP Server: A Developer Guide – CData](https://www.cdata.com/kb/articles/jdbc-mcp-server-dev-guide.rst)
- [Learn MCP Java Tutorial – Microsoft](https://github.com/microsoft/lets-learn-mcp-java/tree/main)
- [Tutorial Java MCP – GitHub](https://github.com/eugenp/tutorials/tree/master/java-mcp)
- [CData JDBC MCP Server – GitHub](https://github.com/CDataSoftware/cdata-jdbc-mcp-server)
