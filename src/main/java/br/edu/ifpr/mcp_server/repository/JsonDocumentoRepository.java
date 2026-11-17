
package br.edu.ifpr.mcp_server.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import br.edu.ifpr.mcp_server.model.Documento;
import br.edu.ifpr.mcp_server.model.StatusDocumento;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

public class JsonDocumentoRepository implements DocumentoRepository {

    private final Path arquivo;
    private final JsonMapper mapper;

    public JsonDocumentoRepository() {
        this(Path.of("data", "documentos.json"));
    }

    public JsonDocumentoRepository(Path arquivo) {
        this.arquivo = arquivo;
        this.mapper = new JsonMapper();
    }

    @Override
    public synchronized List<Documento> findAll() throws IOException {
        return lerDocumentos();
    }

    @Override
    public synchronized Optional<Documento> findById(int id)
            throws IOException {

        return lerDocumentos().stream()
                .filter(documento -> documento.getId() == id)
                .findFirst();
    }

    @Override
    public synchronized Documento save(Documento documento)
            throws IOException {

        List<Documento> documentos = lerDocumentos();

        boolean atualizado = false;

        for (int i = 0; i < documentos.size(); i++) {
            if (documentos.get(i).getId() == documento.getId()) {
                documentos.set(i, documento);
                atualizado = true;
                break;
            }
        }

        if (!atualizado) {
            documentos.add(documento);
        }

        gravarDocumentos(documentos);

        return documento;
    }

    @Override
    public synchronized boolean deleteById(int id)
            throws IOException {

        List<Documento> documentos = lerDocumentos();

        boolean removido = documentos.removeIf(
                documento -> documento.getId() == id);

        if (removido) {
            gravarDocumentos(documentos);
        }

        return removido;
    }

    private List<Documento> lerDocumentos() throws IOException {

        if (!Files.exists(arquivo)) {
            return new ArrayList<>();
        }

        try {
            List<DocumentoJson> registros = mapper.readValue(
                    arquivo,
                    new TypeReference<List<DocumentoJson>>() { }
            );

            List<Documento> documentos = new ArrayList<>();

            if (registros == null) {
                return documentos;
            }

            for (DocumentoJson registro : registros) {
                documentos.add(registro.toDocumento());
            }

            return documentos;

        } catch (JacksonException e) {
            throw new IOException(
                    "Erro ao ler o arquivo de documentos JSON.", e);
        }
    }

    private void gravarDocumentos(List<Documento> documentos)
            throws IOException {

        Path diretorio = arquivo.getParent();

        if (diretorio != null) {
            Files.createDirectories(diretorio);
        }

        List<DocumentoJson> registros = documentos.stream()
                .map(DocumentoJson::new)
                .toList();

        try {
            mapper.writeValue(arquivo, registros);
        } catch (JacksonException e) {
            throw new IOException(
                    "Erro ao gravar o arquivo de documentos JSON.", e);
        }
    }

    public static class DocumentoJson {

        public int id;
        public String titulo;
        public String descricao;
        public String status;
        public String dataCriacao;
        public String dataAtualizacao;

        public DocumentoJson() {
        }

        public DocumentoJson(Documento documento) {
            this.id = documento.getId();
            this.titulo = documento.getTitulo();
            this.descricao = documento.getDescricao();
            this.status = documento.getStatus().name();
            this.dataCriacao = documento.getDataCriacao().toString();
            this.dataAtualizacao =
                    documento.getDataAtualizacao().toString();
        }

        public Documento toDocumento() {

            Documento documento = new Documento();

            documento.setId(id);
            documento.setTitulo(titulo);
            documento.setDescricao(descricao);
            documento.setStatus(StatusDocumento.valueOf(status));
            documento.setDataCriacao(
                    LocalDateTime.parse(dataCriacao));
            documento.setDataAtualizacao(
                    LocalDateTime.parse(dataAtualizacao));

            return documento;
        }
    }
}
