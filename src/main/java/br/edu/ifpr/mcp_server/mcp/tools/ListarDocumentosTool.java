
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

public class ListarDocumentosTool implements McpToolDefinition {

    private final DocumentoService service;

    public ListarDocumentosTool(DocumentoService service) {
        this.service = service;
    }

    @Override
    public void register(
            SyncSpecification<SingleSessionSyncSpecification> mcp)
            throws Exception {

        Map<String, Object> inputSchema = Map.of(
                "type", "object",
                "properties", Map.of(),
                "additionalProperties", false
        );

        Tool tool = Tool.builder("listar_documentos", inputSchema)
                .title("Listar documentos")
                .description("Retorna todos os documentos cadastrados")
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
            List<Documento> documentos = service.listarDocumentos();

            String mensagem;

            if (documentos.isEmpty()) {
                mensagem = "Nenhum documento cadastrado.";
            } else {
                StringBuilder resultado = new StringBuilder();

                for (Documento documento : documentos) {
                    resultado.append(String.format(
                            "ID: %d%nTítulo: %s%nDescrição: %s%n"
                            + "Status: %s%nData de criação: %s%n"
                            + "Data de atualização: %s%n%n",
                            documento.getId(),
                            documento.getTitulo(),
                            documento.getDescricao(),
                            documento.getStatus(),
                            documento.getDataCriacao(),
                            documento.getDataAtualizacao()
                    ));
                }

                mensagem = resultado.toString();
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
