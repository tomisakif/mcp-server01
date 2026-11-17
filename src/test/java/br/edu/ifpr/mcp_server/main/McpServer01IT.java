package br.edu.ifpr.mcp_server.main;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpError;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

import io.modelcontextprotocol.spec.McpSchema.TextContent;

class McpServer01IT {
   private static final Logger LOG =
                                   LoggerFactory.getLogger(McpServer01IT.class);
   // nome e caminho para o servidor MCP
   private static final String SERVER_PATH = "server/mcp_server01_server.jar";
   
   private static final Path ARQUIVO_TESTE = Path.of(
	        "target", "test-data", "documentos-integracao.json"
	);

   private static McpSyncClient client;

   @BeforeAll
   static void setUpBeforeClass() throws Exception {
      // antes que a classe de testes seja carregada, este método providencia
      // alguma inicialização ou alocação de recursos, neste caso, o servidor
      // MCP é executado e o cliente se conecta a ele
      File serverJar = new File(SERVER_PATH);
      assertTrue(serverJar.isFile(),"JAR do servidor não encontrado: " +
                                    serverJar.getAbsolutePath());

      String           jarPath = serverJar.getAbsolutePath();
      Files.deleteIfExists(ARQUIVO_TESTE);
    	ServerParameters params = ServerParameters.builder("java")
    	        .args(
    	        		"-Ddocumentos.arquivo=" + ARQUIVO_TESTE.toAbsolutePath(),
    	                "-jar",
    	                jarPath
    	        )
    	        .build();
      JacksonMcpJsonMapper jsonMapper = new JacksonMcpJsonMapper(
    			                                              new JsonMapper());
      StdioClientTransport transport  = new StdioClientTransport(params,
    			                                                 jsonMapper);

      transport.setStdErrorHandler(line -> LOG.info("[SERVER STDERR] " + line));

      client = McpClient.sync(transport).build();
      client.initialize();
   }

   @BeforeEach
   void limparDadosAntesDeCadaTeste() throws IOException {
       Files.deleteIfExists(ARQUIVO_TESTE);
   }
   
   @AfterAll
   static void tearDownAfterClass() throws Exception {
      // ao terminar todos os testes, todos os métodos nesta classe foram
      // executados, o cliente se desconecta do servidor
      if (client != null)
         client.closeGracefully();
   }

   // ==========================================================================
   // Os métodos de teste à seguir simulam a aplicação cliente:
   // - há ferramentas (tools) disponíveis???
   // - a ferramenta 'obter_documento' existe???
   // - a ferramenta 'obter_documento' executou com sucesso???
   // ==========================================================================

   @Test
   void deveDisponibilizarFerramentas() {
       ListToolsResult result = client.listTools();

       assertNotNull(result);
       assertNotNull(result.tools());
       assertFalse(result.tools().isEmpty());

       assertTrue(result.tools().stream()
               .anyMatch(tool -> "obter_documento".equals(tool.name())));

       assertTrue(result.tools().stream()
               .anyMatch(tool -> "listar_documentos".equals(tool.name())));

       assertTrue(result.tools().stream()
               .anyMatch(tool -> "criar_documento".equals(tool.name())));

       assertTrue(result.tools().stream()
               .anyMatch(tool -> "atualizar_documento".equals(tool.name())));

       assertTrue(result.tools().stream()
               .anyMatch(tool -> "excluir_documento".equals(tool.name())));

       assertTrue(result.tools().stream()
               .anyMatch(tool -> "buscar_documentos".equals(tool.name())));
   }

   @Test
   void deveDisponibilizarObterDocumento() {
      ListToolsResult result = client.listTools();
                                      // retorna a lista de ferramentas
      Tool            tool   = result.tools()
                                     // cria um fluxo para percorrer a lista
    			                     .stream()
    			                     // filtra a lista para um elemento
    			                     .filter(item ->
    			                          "obter_documento".equals(item.name()))
    			                     // verifica se há alguma elemento...
    			                     .findFirst()
    			                     // ...caso contrário, retorna "null"
    			                     .orElse(null);

      assertNotNull(tool,"A ferramenta obter_documento não foi encontrada.");
      assertNotNull(tool.description(),"A ferramenta deveria possuir " +
                                       "descrição.");
      assertNotNull(tool.inputSchema(),"A ferramenta deveria possuir " +
                                       "'inputSchema'.");
   }

   @Test
   void deveExecutarObterDocumento() {

       CallToolRequest criarRequest =
               CallToolRequest.builder("criar_documento")
                       .arguments(Map.of(
                               "titulo", "Documento para Consulta",
                               "descricao", "Documento criado para testar a consulta"
                       ))
                       .build();

       CallToolResult criarResult = client.callTool(criarRequest);

       assertNotNull(criarResult);
       assertFalse(criarResult.isError());

       CallToolRequest obterRequest =
               CallToolRequest.builder("obter_documento")
                       .arguments(Map.of("id", 1))
                       .build();

       CallToolResult obterResult = client.callTool(obterRequest);

       assertNotNull(obterResult);
       assertFalse(obterResult.isError());
       assertNotNull(obterResult.content());
       assertFalse(obterResult.content().isEmpty());
   }
   
   @Test
   void deveExecutarCriarDocumento() {

       CallToolRequest request =
               CallToolRequest.builder("criar_documento")
                       .arguments(Map.of(
                               "titulo", "Documento de Integração",
                               "descricao", "Documento criado durante o teste MCP"
                       ))
                       .build();

       CallToolResult result = client.callTool(request);

       assertNotNull(result);
       assertFalse(result.isError());
       assertNotNull(result.content());
       assertFalse(result.content().isEmpty());

       String resposta = ((TextContent) result.content().get(0)).text();

       assertTrue(resposta.contains("Documento criado com sucesso"));
   }
   
   @Test
   void deveExecutarListarDocumentos() {

       CallToolRequest criarRequest =
               CallToolRequest.builder("criar_documento")
                       .arguments(Map.of(
                               "titulo", "Documento para Listagem",
                               "descricao", "Documento criado para testar a listagem"
                       ))
                       .build();

       CallToolResult criarResult = client.callTool(criarRequest);

       assertNotNull(criarResult);
       assertFalse(criarResult.isError());

       CallToolRequest listarRequest =
               CallToolRequest.builder("listar_documentos")
                       .arguments(Map.of())
                       .build();

       CallToolResult listarResult = client.callTool(listarRequest);

       assertNotNull(listarResult);
       assertFalse(listarResult.isError());
       assertNotNull(listarResult.content());
       assertFalse(listarResult.content().isEmpty());
   }
   
   @Test
   void deveExecutarAtualizarDocumento() throws IOException {

       CallToolRequest criarRequest =
               CallToolRequest.builder("criar_documento")
                       .arguments(Map.of(
                               "titulo", "Documento para Atualização",
                               "descricao", "Documento criado para o teste"
                       ))
                       .build();

       CallToolResult criarResult = client.callTool(criarRequest);

       assertNotNull(criarResult);
       assertFalse(criarResult.isError());

       CallToolRequest listarRequest =
               CallToolRequest.builder("listar_documentos")
                       .arguments(Map.of())
                       .build();

       CallToolResult listarResult = client.callTool(listarRequest);

       assertNotNull(listarResult);
       assertFalse(listarResult.isError());
       assertNotNull(listarResult.content());
       assertFalse(listarResult.content().isEmpty());

       CallToolRequest atualizarRequest =
               CallToolRequest.builder("atualizar_documento")
                       .arguments(Map.of(
                               "id", 1,
                               "titulo", "Documento Atualizado",
                               "descricao", "Descrição atualizada pelo teste MCP",
                               "status", "EM_ANDAMENTO"
                       ))
                       .build();

       CallToolResult atualizarResult =
               client.callTool(atualizarRequest);

       assertNotNull(atualizarResult);
       assertFalse(atualizarResult.isError());
       assertNotNull(atualizarResult.content());
       assertFalse(atualizarResult.content().isEmpty());
   }
   
   @Test
   void deveExecutarBuscarDocumentos() {

       CallToolRequest criarRequest =
               CallToolRequest.builder("criar_documento")
                       .arguments(Map.of(
                               "titulo", "Relatório de Integração",
                               "descricao", "Documento criado para testar a busca"
                       ))
                       .build();

       CallToolResult criarResult = client.callTool(criarRequest);

       assertNotNull(criarResult);
       assertFalse(criarResult.isError());

       CallToolRequest buscarRequest =
               CallToolRequest.builder("buscar_documentos")
                       .arguments(Map.of(
                               "termo", "Relatório"
                       ))
                       .build();

       CallToolResult buscarResult = client.callTool(buscarRequest);

       assertNotNull(buscarResult);
       assertFalse(buscarResult.isError());
       assertNotNull(buscarResult.content());
       assertFalse(buscarResult.content().isEmpty());
   }
   
   @Test
   void deveExecutarExcluirDocumento() throws IOException {

       CallToolRequest criarRequest =
               CallToolRequest.builder("criar_documento")
                       .arguments(Map.of(
                               "titulo", "Documento para Exclusão",
                               "descricao", "Documento criado para o teste"
                       ))
                       .build();

       CallToolResult criarResult = client.callTool(criarRequest);

       assertNotNull(criarResult);
       assertFalse(criarResult.isError());

       CallToolRequest excluirRequest =
               CallToolRequest.builder("excluir_documento")
                       .arguments(Map.of(
                               "id", 1
                       ))
                       .build();

       CallToolResult excluirResult =
               client.callTool(excluirRequest);

       assertNotNull(excluirResult);
       assertFalse(excluirResult.isError());
       assertNotNull(excluirResult.content());
       assertFalse(excluirResult.content().isEmpty());
   }
   
   @Test
   void deveInformarDocumentoInexistente() {

       CallToolRequest request =
               CallToolRequest.builder("obter_documento")
                       .arguments(Map.of("id", 999))
                       .build();

       CallToolResult result = client.callTool(request);

       assertNotNull(result);
       assertFalse(result.isError());
       assertNotNull(result.content());
       assertFalse(result.content().isEmpty());

       String resposta =
               ((TextContent) result.content().get(0)).text();

       assertTrue(resposta.contains("não encontrado"));
   }
   
   @Test
   void deveInformarDocumentoInexistenteAoAtualizar() {

       CallToolRequest request =
               CallToolRequest.builder("atualizar_documento")
                       .arguments(Map.of(
                               "id", 999,
                               "titulo", "Título atualizado",
                               "descricao", "Descrição atualizada",
                               "status", "FINALIZADO"
                       ))
                       .build();

       CallToolResult result = client.callTool(request);

       assertNotNull(result);
       assertFalse(result.isError());
       assertNotNull(result.content());
       assertFalse(result.content().isEmpty());

       String resposta =
               ((TextContent) result.content().get(0)).text();

       assertTrue(resposta.contains("não encontrado"));
   }
   
   @Test
   void deveInformarDocumentoInexistenteAoExcluir() {

       CallToolRequest request =
               CallToolRequest.builder("excluir_documento")
                       .arguments(Map.of("id", 999))
                       .build();

       CallToolResult result = client.callTool(request);

       assertNotNull(result);
       assertFalse(result.isError());
       assertNotNull(result.content());
       assertFalse(result.content().isEmpty());

       String resposta =
               ((TextContent) result.content().get(0)).text();

       assertTrue(resposta.contains("não encontrado"));
   }
   
   @Test
   void deveRejeitarCriacaoComTituloVazio() {

       CallToolRequest request =
               CallToolRequest.builder("criar_documento")
                       .arguments(Map.of(
                               "titulo", "   ",
                               "descricao", "Descrição de teste"
                       ))
                       .build();

       assertThrows(McpError.class,
               () -> client.callTool(request));
   }
   
   @Test
   void deveRejeitarCriacaoComDescricaoVazia() {

       CallToolRequest request =
               CallToolRequest.builder("criar_documento")
                       .arguments(Map.of(
                               "titulo", "Documento de teste",
                               "descricao", "   "
                       ))
                       .build();

       assertThrows(McpError.class,
               () -> client.callTool(request));
   }
   
   @Test
   void deveRejeitarBuscaComTermoVazio() {

       CallToolRequest request =
               CallToolRequest.builder("buscar_documentos")
                       .arguments(Map.of("termo", "   "))
                       .build();

       assertThrows(McpError.class,
               () -> client.callTool(request));
   }
   
   @Test
   void deveRejeitarAtualizacaoComStatusInvalido() {

       CallToolRequest request =
               CallToolRequest.builder("atualizar_documento")
                       .arguments(Map.of(
                               "id", 1,
                               "titulo", "Documento de teste",
                               "descricao", "Descrição de teste",
                               "status", "INVALIDO"
                       ))
                       .build();

       CallToolResult result = client.callTool(request);

       assertNotNull(result);
       assertTrue(result.isError());
   }
   
}
