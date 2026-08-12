package com.trackwheel.domain.service;

import com.trackwheel.domain.model.Oficina;
import com.trackwheel.domain.model.Papel;
import com.trackwheel.domain.model.Ramo;
import com.trackwheel.domain.model.Usuario;
import com.trackwheel.domain.repository.OficinaRepository;
import com.trackwheel.domain.repository.UsuarioRepository;
import com.trackwheel.domain.validation.DocumentoValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** Onboarding e gestao da oficina (tenant) e dos seus usuarios. */
@Service
public class OficinaService {

    private final OficinaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final TemplateCamposService templateService;

    public OficinaService(OficinaRepository repository,
                          UsuarioRepository usuarioRepository,
                          TemplateCamposService templateService) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.templateService = templateService;
    }

    /**
     * Onboarding: cria a oficina, promove quem cadastrou a OWNER e ja semeia
     * os campos dinamicos dos ramos escolhidos.
     */
    public Oficina onboarding(Oficina oficina, Usuario dono) {
        validar(oficina);

        if (oficina.getCnpj() != null && !oficina.getCnpj().isBlank()) {
            Optional<Oficina> existente = repository.buscarPorCnpj(oficina.getCnpj());
            if (existente.isPresent()) {
                throw new RegraNegocioException("Ja existe uma oficina com o CNPJ "
                        + DocumentoValidator.formatarCnpj(oficina.getCnpj()));
            }
        }

        Oficina salva = repository.salvar(oficina);

        dono.setOficinaId(salva.getId());
        dono.setPapel(Papel.OWNER);
        usuarioRepository.salvar(dono);

        templateService.criarSeedParaRamos(salva.getId(), salva.getRamos(), dono.getId());
        return salva;
    }

    private void validar(Oficina oficina) {
        if (oficina.getNomeFantasia() == null || oficina.getNomeFantasia().isBlank()) {
            throw new RegraNegocioException("Nome fantasia e obrigatorio");
        }
        if (oficina.getRamos() == null || oficina.getRamos().isEmpty()) {
            throw new RegraNegocioException("Escolha ao menos um ramo de atuacao");
        }
        if (oficina.getCnpj() != null && !oficina.getCnpj().isBlank()
                && !DocumentoValidator.cnpjValido(oficina.getCnpj())) {
            throw new RegraNegocioException("CNPJ invalido: " + oficina.getCnpj());
        }
    }

    public Oficina atualizar(String oficinaId, Oficina dados) {
        Oficina oficina = repository.buscarPorId(oficinaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Oficina", oficinaId));

        List<Ramo> ramosAntigos = oficina.getRamos();

        oficina.setNomeFantasia(dados.getNomeFantasia());
        oficina.setRazaoSocial(dados.getRazaoSocial());
        oficina.setCnpj(dados.getCnpj());
        oficina.setInscricaoEstadual(dados.getInscricaoEstadual());
        oficina.setRamos(dados.getRamos());
        oficina.setEndereco(dados.getEndereco());
        oficina.setTelefone(dados.getTelefone());
        oficina.setWhatsapp(dados.getWhatsapp());
        oficina.setEmail(dados.getEmail());
        oficina.setLogoUrl(dados.getLogoUrl());
        oficina.setHorarioFuncionamento(dados.getHorarioFuncionamento());
        if (dados.getConfig() != null) {
            oficina.setConfig(dados.getConfig());
        }
        validar(oficina);

        // Ramo novo entra ja com os campos padrao daquele ramo.
        List<Ramo> novos = oficina.getRamos().stream().filter(r -> !ramosAntigos.contains(r)).toList();
        if (!novos.isEmpty()) {
            templateService.criarSeedParaRamos(oficinaId, novos, null);
        }
        return repository.salvar(oficina);
    }

    public Optional<Oficina> buscarPorId(String id) {
        return repository.buscarPorId(id);
    }

    public List<Usuario> listarUsuarios(String oficinaId) {
        return usuarioRepository.listarPorOficina(oficinaId);
    }

    /** Convida/atualiza um usuario da equipe. So OWNER chama isto (checado na API). */
    public Usuario salvarUsuario(String oficinaId, Usuario usuario) {
        usuario.setOficinaId(oficinaId);
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new RegraNegocioException("E-mail e obrigatorio");
        }
        if (usuario.getPapel() == null) {
            throw new RegraNegocioException("Papel e obrigatorio");
        }
        return usuarioRepository.salvar(usuario);
    }

    /** A oficina nao pode ficar sem dono. */
    public void removerUsuario(String oficinaId, String usuarioId) {
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
                .filter(u -> oficinaId.equals(u.getOficinaId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", usuarioId));

        if (usuario.getPapel() == Papel.OWNER) {
            long owners = usuarioRepository.listarPorOficina(oficinaId).stream()
                    .filter(u -> u.getPapel() == Papel.OWNER)
                    .count();
            if (owners <= 1) {
                throw new RegraNegocioException("A oficina precisa de ao menos um OWNER");
            }
        }
        usuarioRepository.remover(usuarioId);
    }
}
