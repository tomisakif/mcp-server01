
package br.edu.ifpr.mcp_server.main;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.ifpr.mcp_server.mcp.tools.AtualizarDocumentoTool;
import br.edu.ifpr.mcp_server.mcp.tools.BuscarDocumentosTool;
import br.edu.ifpr.mcp_server.mcp.tools.CriarDocumentoTool;
import br.edu.ifpr.mcp_server.mcp.tools.ExcluirDocumentoTool;
import br.edu.ifpr.mcp_server.mcp.tools.ListarDocumentosTool;
import br.edu.ifpr.mcp_server.mcp.tools.ObterDocumentoTool;
import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;
import br.edu.ifpr.mcp_server.repository.JsonDocumentoRepository;
import br.edu.ifpr.mcp_server.service.DocumentoService;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.Implementation;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import tools.jackson.databind.json.JsonMapper;

public class McpServer01SyncStdio {

    private static final Logger LOG =
            LoggerFactory.getLogger(McpServer01SyncStdio.class);

    private McpSyncServer server;

    private void registerTools(
            SyncSpecification<SingleSessionSyncSpecification> mcp)
            throws Exception {

    	String caminhoArquivo = System.getProperty(
    	        "documentos.arquivo",
    	        "data/documentos.json"
    	);

    	JsonDocumentoRepository repository =
    	        new JsonDocumentoRepository(
    	                java.nio.file.Path.of(caminhoArquivo)
    	        );

        DocumentoService service =
                new DocumentoService(repository);

        List<McpToolDefinition> tools = List.of(
                new ObterDocumentoTool(service),
                new ListarDocumentosTool(service),
                new CriarDocumentoTool(service),
                new AtualizarDocumentoTool(service),
                new ExcluirDocumentoTool(service),
                new BuscarDocumentosTool(service)
        );

        for (McpToolDefinition tool : tools) {
            tool.register(mcp);
        }
    }

    private SyncSpecification<SingleSessionSyncSpecification>
            configMcpServer() {

        JacksonMcpJsonMapper jsonMapper =
                new JacksonMcpJsonMapper(new JsonMapper());

        StdioServerTransportProvider transportProvider =
                new StdioServerTransportProvider(jsonMapper);

        ServerCapabilities capabilities =
                ServerCapabilities.builder()
                        .tools(true)
                        .logging()
                        .build();

        Implementation implementation =
                Implementation.builder("MCP Server", "1.0.0")
                        .build();

        return McpServer.sync(transportProvider)
                .serverInfo(implementation)
                .capabilities(capabilities);
    }

    public McpServer01SyncStdio() throws Exception {

        SyncSpecification<SingleSessionSyncSpecification> srv =
                configMcpServer();

        registerTools(srv);

        this.server = srv.build();

        LOG.info("MCP Server 01 iniciado via STDIO...");
    }

    public static void main(String[] args) throws Exception {
        new McpServer01SyncStdio();
    }
}
