
package br.edu.ifpr.mcp_server.mcp.tools;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcp_server.model.Documento;
import br.edu.ifpr.mcp_server.service.DocumentoService;

import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.Annotations;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ObterDocumentoTool implements McpToolDefinition {

    private final DocumentoService service;

    public ObterDocumentoTool(DocumentoService service) {
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
                                "description", "ID do documento"
                        )
                ),
                "required", List.of("id"),
                "additionalProperties", false
        );

        Tool tool = Tool.builder("obter_documento", inputSchema)
                .title("Obter documento")
                .description("Retorna os dados de um documento pelo ID")
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

            Optional<Documento> resultado = service.obterDocumento(id);

            String mensagem;

            if (resultado.isPresent()) {
                Documento documento = resultado.get();

                mensagem = String.format(
                        "ID: %d%nTítulo: %s%nDescrição: %s%n"
                        + "Status: %s%nData de criação: %s%n"
                        + "Data de atualização: %s",
                        documento.getId(),
                        documento.getTitulo(),
                        documento.getDescricao(),
                        documento.getStatus(),
                        documento.getDataCriacao(),
                        documento.getDataAtualizacao()
                );
            } else {
                mensagem = "Documento com ID " + id
                        + " não encontrado.";
            }

            List<Role> roles = List.of(McpSchema.Role.USER);

            Annotations annotations = Annotations.builder()
                    .audience(roles)
                    .priority(1.0)
                    .build();

            TextContent content = TextContent.builder(mensagem)
                    .annotations(annotations)
                    .build();

            return CallToolResult.builder()
                    .content(List.of(content))
                    .build();

        } catch (IOException | RuntimeException ex) {
            throw new RuntimeException(
                    "ERRO EXECUTANDO FERRAMENTA: " + ex.getMessage(), ex);
        }
    }
}
