
package br.edu.ifpr.mcp_server.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import br.edu.ifpr.mcp_server.model.Documento;
import br.edu.ifpr.mcp_server.model.StatusDocumento;
import br.edu.ifpr.mcp_server.repository.DocumentoRepository;

public class DocumentoService {

    private final DocumentoRepository repository;

    public DocumentoService(DocumentoRepository repository) {
        this.repository = repository;
    }

    public List<Documento> listarDocumentos() throws IOException {
        return repository.findAll();
    }

    public Optional<Documento> obterDocumento(int id)
            throws IOException {
        return repository.findById(id);
    }

    public Documento criarDocumento(String titulo, String descricao)
            throws IOException {

        validarTexto(titulo, "O título");
        validarTexto(descricao, "A descrição");

        List<Documento> documentos = repository.findAll();

        int proximoId = documentos.stream()
                .mapToInt(Documento::getId)
                .max()
                .orElse(0) + 1;

        Documento documento = new Documento(
                proximoId,
                titulo.trim(),
                descricao.trim()
        );

        return repository.save(documento);
    }

    public Optional<Documento> atualizarDocumento(
            int id, String titulo, String descricao,
            StatusDocumento status) throws IOException {

        validarTexto(titulo, "O título");
        validarTexto(descricao, "A descrição");
        
        if (status == null) {
            throw new IllegalArgumentException(
                    "O status é obrigatório."
            );
        }

        Optional<Documento> resultado = repository.findById(id);

        if (resultado.isEmpty()) {
            return Optional.empty();
        }

        Documento documento = resultado.get();

        documento.setTitulo(titulo.trim());
        documento.setDescricao(descricao.trim());
        documento.setStatus(status);
        documento.setDataAtualizacao(LocalDateTime.now());

        return Optional.of(repository.save(documento));
    }

    public boolean excluirDocumento(int id) throws IOException {
        return repository.deleteById(id);
    }

    public List<Documento> buscarDocumentos(String termo)
            throws IOException {

        validarTexto(termo, "O termo de busca");

        String termoNormalizado = termo.trim()
                .toLowerCase(Locale.ROOT);

        return repository.findAll().stream()
                .filter(documento -> documento.getTitulo()
                        .toLowerCase(Locale.ROOT)
                        .contains(termoNormalizado))
                .toList();
    }

    private void validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    campo + " não pode ser vazio."
            );
        }
    }
}
