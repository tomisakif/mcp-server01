
package br.edu.ifpr.mcp_server.repository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import br.edu.ifpr.mcp_server.model.Documento;

public interface DocumentoRepository {

    List<Documento> findAll() throws IOException;

    Optional<Documento> findById(int id) throws IOException;

    Documento save(Documento documento) throws IOException;

    boolean deleteById(int id) throws IOException;

}
