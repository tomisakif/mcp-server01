
package br.edu.ifpr.mcp_server.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.edu.ifpr.mcp_server.model.Documento;
import br.edu.ifpr.mcp_server.model.StatusDocumento;

class JsonDocumentoRepositoryTest {

    @TempDir
    Path diretorioTemporario;

    private JsonDocumentoRepository repository;
    private Path arquivoJson;

    @BeforeEach
    void setUp() {
        arquivoJson = diretorioTemporario.resolve("documentos.json");
        repository = new JsonDocumentoRepository(arquivoJson);
    }

    @Test
    void deveRetornarListaVaziaQuandoArquivoNaoExiste()
            throws IOException {

        List<Documento> documentos = repository.findAll();

        assertTrue(documentos.isEmpty());
    }

    @Test
    void deveSalvarEConsultarDocumento() throws IOException {

        Documento documento = new Documento(
                1,
                "Relatório de Teste",
                "Documento utilizado nos testes"
        );

        repository.save(documento);

        Optional<Documento> resultado = repository.findById(1);

        assertTrue(resultado.isPresent());
        assertEquals("Relatório de Teste",
                resultado.get().getTitulo());
        assertEquals("Documento utilizado nos testes",
                resultado.get().getDescricao());
        assertEquals(StatusDocumento.PENDENTE,
                resultado.get().getStatus());
    }

    @Test
    void deveListarDocumentosSalvos() throws IOException {

        Documento documento1 = new Documento(
                1, "Documento A", "Descrição A"
        );

        Documento documento2 = new Documento(
                2, "Documento B", "Descrição B"
        );

        repository.save(documento1);
        repository.save(documento2);

        List<Documento> documentos = repository.findAll();

        assertEquals(2, documentos.size());
    }

    @Test
    void deveAtualizarDocumentoExistente() throws IOException {

        Documento documento = new Documento(
                1, "Título Original", "Descrição Original"
        );

        repository.save(documento);

        documento.setTitulo("Título Atualizado");
        documento.setDescricao("Descrição Atualizada");
        documento.setStatus(StatusDocumento.FINALIZADO);

        repository.save(documento);

        Optional<Documento> resultado = repository.findById(1);

        assertTrue(resultado.isPresent());
        assertEquals("Título Atualizado",
                resultado.get().getTitulo());
        assertEquals("Descrição Atualizada",
                resultado.get().getDescricao());
        assertEquals(StatusDocumento.FINALIZADO,
                resultado.get().getStatus());

        assertEquals(1, repository.findAll().size());
    }

    @Test
    void deveExcluirDocumentoExistente() throws IOException {

        Documento documento = new Documento(
                1, "Documento para excluir", "Descrição"
        );

        repository.save(documento);

        boolean removido = repository.deleteById(1);

        assertTrue(removido);
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void naoDeveExcluirDocumentoInexistente() throws IOException {

        boolean removido = repository.deleteById(999);

        assertFalse(removido);
    }

    @Test
    void deveCriarArquivoJsonAoSalvarDocumento()
            throws IOException {

        Documento documento = new Documento(
                1, "Documento JSON", "Teste de persistência"
        );

        repository.save(documento);

        assertTrue(Files.exists(arquivoJson));
    }
}
