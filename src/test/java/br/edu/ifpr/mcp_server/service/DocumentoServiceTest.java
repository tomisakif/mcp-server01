
package br.edu.ifpr.mcp_server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.edu.ifpr.mcp_server.model.Documento;
import br.edu.ifpr.mcp_server.model.StatusDocumento;
import br.edu.ifpr.mcp_server.repository.JsonDocumentoRepository;

class DocumentoServiceTest {

    @TempDir
    Path diretorioTemporario;

    private DocumentoService service;

    @BeforeEach
    void setUp() {
        JsonDocumentoRepository repository =
                new JsonDocumentoRepository(
                        diretorioTemporario.resolve("documentos.json")
                );

        service = new DocumentoService(repository);
    }

    @Test
    void deveListarDocumentos() throws IOException {

        service.criarDocumento("Documento A", "Descrição A");
        service.criarDocumento("Documento B", "Descrição B");

        List<Documento> documentos = service.listarDocumentos();

        assertEquals(2, documentos.size());
    }

    @Test
    void deveObterDocumentoExistente() throws IOException {

        Documento criado = service.criarDocumento(
                "Relatório", "Descrição do relatório"
        );

        Optional<Documento> resultado =
                service.obterDocumento(criado.getId());

        assertTrue(resultado.isPresent());
        assertEquals("Relatório", resultado.get().getTitulo());
    }

    @Test
    void naoDeveObterDocumentoInexistente() throws IOException {

        Optional<Documento> resultado = service.obterDocumento(999);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveCriarDocumentoComIdAutomatico() throws IOException {

        Documento primeiro = service.criarDocumento(
                "Documento 1", "Descrição 1"
        );

        Documento segundo = service.criarDocumento(
                "Documento 2", "Descrição 2"
        );

        assertEquals(1, primeiro.getId());
        assertEquals(2, segundo.getId());
        assertEquals(StatusDocumento.PENDENTE, primeiro.getStatus());
        assertNotNull(primeiro.getDataCriacao());
        assertNotNull(primeiro.getDataAtualizacao());
    }

    @Test
    void deveRemoverEspacosDosTextosAoCriarDocumento()
            throws IOException {

        Documento documento = service.criarDocumento(
                "  Relatório  ", "  Descrição  "
        );

        assertEquals("Relatório", documento.getTitulo());
        assertEquals("Descrição", documento.getDescricao());
    }

    @Test
    void naoDeveCriarDocumentoComTituloVazio() {

        assertThrows(IllegalArgumentException.class,
                () -> service.criarDocumento("", "Descrição"));
    }

    @Test
    void naoDeveCriarDocumentoComDescricaoVazia() {

        assertThrows(IllegalArgumentException.class,
                () -> service.criarDocumento("Título", "  "));
    }

    @Test
    void deveAtualizarDocumentoExistente() throws IOException {

        Documento criado = service.criarDocumento(
                "Título Original", "Descrição Original"
        );

        Optional<Documento> resultado =
                service.atualizarDocumento(
                        criado.getId(),
                        "Título Atualizado",
                        "Descrição Atualizada",
                        StatusDocumento.FINALIZADO
                );

        assertTrue(resultado.isPresent());

        Documento atualizado = resultado.get();

        assertEquals(criado.getId(), atualizado.getId());
        assertEquals(criado.getDataCriacao(),
                atualizado.getDataCriacao());
        assertEquals("Título Atualizado", atualizado.getTitulo());
        assertEquals("Descrição Atualizada", atualizado.getDescricao());
        assertEquals(StatusDocumento.FINALIZADO, atualizado.getStatus());
        assertNotNull(atualizado.getDataAtualizacao());
    }

    @Test
    void naoDeveAtualizarDocumentoInexistente() throws IOException {

        Optional<Documento> resultado =
                service.atualizarDocumento(
                        999,
                        "Novo título",
                        "Nova descrição",
                        StatusDocumento.PENDENTE
                );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveExcluirDocumentoExistente() throws IOException {

        Documento documento = service.criarDocumento(
                "Documento", "Descrição"
        );

        assertTrue(service.excluirDocumento(documento.getId()));
        assertTrue(service.listarDocumentos().isEmpty());
    }

    @Test
    void naoDeveExcluirDocumentoInexistente() throws IOException {

        assertFalse(service.excluirDocumento(999));
    }

    @Test
    void deveBuscarDocumentosSemDiferenciarMaiusculas()
            throws IOException {

        service.criarDocumento(
                "Relatório Financeiro", "Descrição A"
        );

        service.criarDocumento(
                "Relatório Técnico", "Descrição B"
        );

        service.criarDocumento(
                "Contrato", "Descrição C"
        );

        List<Documento> resultados =
                service.buscarDocumentos("RELATÓRIO");

        assertEquals(2, resultados.size());
    }

    @Test
    void deveRetornarListaVaziaQuandoBuscaNaoEncontraResultados()
            throws IOException {

        service.criarDocumento("Contrato", "Descrição");

        List<Documento> resultados =
                service.buscarDocumentos("Relatório");

        assertTrue(resultados.isEmpty());
    }

    @Test
    void naoDeveBuscarComTermoVazio() {

        assertThrows(IllegalArgumentException.class,
                () -> service.buscarDocumentos(" "));
    }
    
    @Test
    void naoDeveAtualizarDocumentoComStatusNulo() throws IOException {
        Documento criado = service.criarDocumento(
                "Título", "Descrição"
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.atualizarDocumento(
                        criado.getId(),
                        "Novo título",
                        "Nova descrição",
                        null
                ));
    }
    
}
