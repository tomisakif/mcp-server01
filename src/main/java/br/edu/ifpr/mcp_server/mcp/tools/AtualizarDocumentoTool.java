
package br.edu.ifpr.mcp_server.mcp.tools;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcp_server.model.Documento;
import br.edu.ifpr.mcp_server.model.StatusDocumento;
import br.edu.ifpr.mcp_server.service.DocumentoService;

import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class AtualizarDocumentoTool implements McpToolDefinition {

    private final DocumentoService service;

    public AtualizarDocumentoTool(DocumentoService service) {
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
                        ),
                        "titulo", Map.of(
                                "type", "string",
                                "description", "Novo título do documento"
                        ),
                        "descricao", Map.of(
                                "type", "string",
                                "description", "Nova descrição do documento"
                        ),
                        "status", Map.of(
                                "type", "string",
                                "enum", List.of(
                                        "PENDENTE",
                                        "EM_ANDAMENTO",
                                        "FINALIZADO"
                                ),
                                "description", "Novo status do documento"
                        )
                ),
                "required", List.of(
                        "id", "titulo", "descricao", "status"
                ),
                "additionalProperties", false
        );

        Tool tool = Tool.builder("atualizar_documento", inputSchema)
                .title("Atualizar documento")
                .description(
                        "Atualiza o título, a descrição e o status "
                        + "de um documento existente"
                )
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
            String titulo = (String) request.arguments().get("titulo");
            String descricao =
                    (String) request.arguments().get("descricao");
            String statusTexto = (String) request.arguments().get("status");

            StatusDocumento status =
                    StatusDocumento.valueOf(statusTexto);

            Optional<Documento> resultado =
                    service.atualizarDocumento(
                            id, titulo, descricao, status
                    );

            String mensagem;

            if (resultado.isPresent()) {
                Documento documento = resultado.get();

                mensagem = String.format(
                        "Documento atualizado com sucesso!%n"
                        + "ID: %d%nTítulo: %s%nDescrição: %s%n"
                        + "Status: %s%nData de atualização: %s",
                        documento.getId(),
                        documento.getTitulo(),
                        documento.getDescricao(),
                        documento.getStatus(),
                        documento.getDataAtualizacao()
                );
            } else {
                mensagem = "Documento com ID " + id
                        + " não encontrado.";
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
