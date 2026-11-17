
package br.edu.ifpr.mcp_server.mcp.tools;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcp_server.service.DocumentoService;

import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ExcluirDocumentoTool implements McpToolDefinition {

    private final DocumentoService service;

    public ExcluirDocumentoTool(DocumentoService service) {
        this.service = service;
    }

    @Override
    public void register(
            SyncSpecification<SingleSessionSyncSpecification> mcp)
            throws Exception {

        Map<String, Object> inputSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "id", Map.of(
                                "type", "integer",
                                "description", "ID do documento a excluir"
                        )
                ),
                "required", List.of("id"),
                "additionalProperties", false
        );

        Tool tool = Tool.builder("excluir_documento", inputSchema)
                .title("Excluir documento")
                .description("Exclui um documento cadastrado pelo seu ID")
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
            int id = ((Number) request.arguments().get("id")).intValue();

            boolean removido = service.excluirDocumento(id);

            String mensagem;

            if (removido) {
                mensagem = "Documento com ID " + id
                        + " excluído com sucesso.";
            } else {
                mensagem = "Documento com ID " + id
                        + " não encontrado. Nenhuma exclusão foi realizada.";
            }

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
