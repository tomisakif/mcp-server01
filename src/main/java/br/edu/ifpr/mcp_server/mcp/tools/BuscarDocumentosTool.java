
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

public class BuscarDocumentosTool implements McpToolDefinition {

    private final DocumentoService service;

    public BuscarDocumentosTool(DocumentoService service) {
        this.service = service;
    }

    @Override
    public void register(
            SyncSpecification<SingleSessionSyncSpecification> mcp)
            throws Exception {

        Map<String, Object> inputSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "termo", Map.of(
                                "type", "string",
                                "description",
                                "Palavra ou trecho do título a pesquisar"
                        )
                ),
                "required", List.of("termo"),
                "additionalProperties", false
        );

        Tool tool = Tool.builder("buscar_documentos", inputSchema)
                .title("Buscar documentos")
                .description(
                        "Pesquisa documentos pelo título, "
                        + "sem diferenciar maiúsculas de minúsculas"
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
            String termo = (String) request.arguments().get("termo");

            List<Documento> documentos =
                    service.buscarDocumentos(termo);

            String mensagem;

            if (documentos.isEmpty()) {
                mensagem = "Nenhum documento encontrado para o termo: "
                        + termo;
            } else {
                StringBuilder resultado = new StringBuilder();

                resultado.append("Documentos encontrados: ")
                        .append(documentos.size())
                        .append("\n\n");

                for (Documento documento : documentos) {
                    resultado.append(String.format(
                            "ID: %d%nTítulo: %s%nDescrição: %s%n"
                            + "Status: %s%n%n",
                            documento.getId(),
                            documento.getTitulo(),
                            documento.getDescricao(),
                            documento.getStatus()
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
