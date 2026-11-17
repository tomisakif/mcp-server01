
package br.edu.ifpr.mcp_server.mcp.tools;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcp_server.model.Documento;
import br.edu.ifpr.mcp_server.service.DocumentoService;

import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class CriarDocumentoTool implements McpToolDefinition {

    private final DocumentoService service;

    public CriarDocumentoTool(DocumentoService service) {
        this.service = service;
    }

    @Override
    public void register(
            SyncSpecification<SingleSessionSyncSpecification> mcp)
            throws Exception {

        Map<String, Object> inputSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "titulo", Map.of(
                                "type", "string",
                                "description", "Título do documento"
                        ),
                        "descricao", Map.of(
                                "type", "string",
                                "description", "Descrição do documento"
                        )
                ),
                "required", List.of("titulo", "descricao"),
                "additionalProperties", false
        );

        Tool tool = Tool.builder("criar_documento", inputSchema)
                .title("Criar documento")
                .description("Cadastra um novo documento")
                .build();

        SyncToolSpecification toolSpec =
                SyncToolSpecification.builder()
                        .tool(tool)
                        .callHandler(this::run)
                        .build();

        mcp.tools(toolSpec);
    }

    @Override
    public CallToolResult run(
            McpSyncServerExchange exchange,
            CallToolRequest request) {

        try {
            String titulo = (String) request.arguments().get("titulo");
            String descricao = (String) request.arguments().get("descricao");

            Documento documento =
                    service.criarDocumento(titulo, descricao);

            String mensagem = String.format(
                    "Documento criado com sucesso!%n"
                    + "ID: %d%nTítulo: %s%nDescrição: %s%n"
                    + "Status: %s%nData de criação: %s",
                    documento.getId(),
                    documento.getTitulo(),
                    documento.getDescricao(),
                    documento.getStatus(),
                    documento.getDataCriacao()
            );

            TextContent content = TextContent.builder(mensagem).build();

            return CallToolResult.builder()
                    .content(List.of(content))
                    .build();

        } catch (IOException | RuntimeException ex) {
            throw new RuntimeException(
                    "ERRO EXECUTANDO FERRAMENTA: " + ex.getMessage(), ex);
        }
    }
}
