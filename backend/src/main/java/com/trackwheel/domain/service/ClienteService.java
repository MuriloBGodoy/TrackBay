package com.trackwheel.domain.service;

import com.trackwheel.domain.model.Cliente;
import com.trackwheel.domain.model.TipoPessoa;
import com.trackwheel.domain.repository.ClienteRepository;
import com.trackwheel.domain.validation.ContatoValidator;
import com.trackwheel.domain.validation.DocumentoValidator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Regras de cadastro de cliente. A validacao muda conforme PF ou PJ. */
@Service
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public Cliente salvar(String oficinaId, Cliente cliente) {
        cliente.setOficinaId(oficinaId);
        validar(cliente);

        if (cliente.isConsentimentoLgpd() && cliente.getConsentimentoEm() == null) {
            cliente.setConsentimentoEm(Instant.now());
        }

        String documento = cliente.documento();
        if (documento != null && !documento.isBlank()) {
            Optional<Cliente> existente = repository.buscarPorDocumento(oficinaId, documento);
            if (existente.isPresent() && !existente.get().getId().equals(cliente.getId())) {
                throw new RegraNegocioException("Ja existe um cliente com este documento: "
                        + cliente.documentoFormatado());
            }
        }
        return repository.salvar(cliente);
    }

    private void validar(Cliente cliente) {
        if (cliente.getNome() == null || cliente.getNome().isBlank()) {
            throw new RegraNegocioException("Nome e obrigatorio");
        }
        if (cliente.getTipoPessoa() == null) {
            throw new RegraNegocioException("Tipo de pessoa e obrigatorio");
        }
        if (cliente.getEmail() != null && !cliente.getEmail().isBlank()
                && !ContatoValidator.emailValido(cliente.getEmail())) {
            throw new RegraNegocioException("E-mail invalido: " + cliente.getEmail());
        }
        if (cliente.getTelefone() != null && !cliente.getTelefone().isBlank()
                && !ContatoValidator.telefoneValido(cliente.getTelefone())) {
            throw new RegraNegocioException("Telefone invalido: " + cliente.getTelefone());
        }

        if (cliente.getTipoPessoa() == TipoPessoa.FISICA) {
            validarPF(cliente);
        } else {
            validarPJ(cliente);
        }
    }

    private void validarPF(Cliente cliente) {
        if (cliente.getDadosPJ() != null) {
            throw new RegraNegocioException("Cliente PF nao pode ter dados de PJ");
        }
        Cliente.DadosPF pf = cliente.getDadosPF();
        if (pf != null && pf.getCpf() != null && !pf.getCpf().isBlank()
                && !DocumentoValidator.cpfValido(pf.getCpf())) {
            throw new RegraNegocioException("CPF invalido: " + pf.getCpf());
        }
    }

    private void validarPJ(Cliente cliente) {
        if (cliente.getDadosPF() != null) {
            throw new RegraNegocioException("Cliente PJ nao pode ter dados de PF");
        }
        Cliente.DadosPJ pj = cliente.getDadosPJ();
        if (pj == null || pj.getCnpj() == null || pj.getCnpj().isBlank()) {
            throw new RegraNegocioException("CNPJ e obrigatorio para cliente PJ");
        }
        if (!DocumentoValidator.cnpjValido(pj.getCnpj())) {
            throw new RegraNegocioException("CNPJ invalido: " + pj.getCnpj());
        }
    }

    public Optional<Cliente> buscarPorId(String oficinaId, String id) {
        return repository.buscarPorId(oficinaId, id);
    }

    public List<Cliente> listar(String oficinaId) {
        return repository.listarPorOficina(oficinaId);
    }

    public List<Cliente> buscar(String oficinaId, String termo) {
        if (termo == null || termo.isBlank()) {
            return listar(oficinaId);
        }
        return repository.buscar(oficinaId, termo);
    }

    /** LGPD: exclusao a pedido do titular. */
    public void remover(String oficinaId, String id) {
        repository.remover(oficinaId, id);
    }
}
