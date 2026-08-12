package com.trackwheel.domain.service;

import com.trackwheel.domain.model.CampoDinamico;
import com.trackwheel.domain.model.Ramo;
import com.trackwheel.domain.model.TemplateCampos;
import com.trackwheel.domain.model.TipoCampo;
import com.trackwheel.domain.repository.TemplateCamposRepository;
import com.trackwheel.domain.seed.TemplatesPadrao;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Gerencia o catalogo de campos dinamicos.
 * Editar um template cria uma nova versao: OS ja emitidas continuam apontando para a versao antiga.
 */
@Service
public class TemplateCamposService {

    private final TemplateCamposRepository repository;

    public TemplateCamposService(TemplateCamposRepository repository) {
        this.repository = repository;
    }

    /** Chamado no onboarding: a oficina escolhe os ramos e ja sai com os campos prontos. */
    public List<TemplateCampos> criarSeedParaRamos(String oficinaId, List<Ramo> ramos, String criadoPor) {
        List<TemplateCampos> criados = new ArrayList<>();
        for (Ramo ramo : ramos) {
            if (repository.buscarAtivoPorRamo(oficinaId, ramo).isPresent()) {
                continue;
            }
            TemplateCampos template = new TemplateCampos();
            template.setOficinaId(oficinaId);
            template.setRamo(ramo);
            template.setVersao(1);
            template.setCriadoPor(criadoPor);
            template.setCampos(TemplatesPadrao.comFiltroDeVeiculo(TemplatesPadrao.para(ramo), ramo));
            criados.add(repository.salvar(template));
        }
        return criados;
    }

    public Optional<TemplateCampos> buscarAtivo(String oficinaId, Ramo ramo) {
        return repository.buscarAtivoPorRamo(oficinaId, ramo);
    }

    public Optional<TemplateCampos> buscarVersao(String oficinaId, Ramo ramo, int versao) {
        return repository.buscarPorRamoEVersao(oficinaId, ramo, versao);
    }

    public List<TemplateCampos> listar(String oficinaId) {
        return repository.listarPorOficina(oficinaId);
    }

    /**
     * Salva uma nova versao do template. O anterior continua existindo (mas inativo),
     * para que OS antigas ainda renderizem com o schema original.
     */
    public TemplateCampos novaVersao(String oficinaId, Ramo ramo, List<CampoDinamico> campos, String autor) {
        validarCampos(campos);

        Optional<TemplateCampos> atual = repository.buscarAtivoPorRamo(oficinaId, ramo);
        atual.ifPresent(t -> {
            t.setAtivo(false);
            repository.salvar(t);
        });

        TemplateCampos novo = new TemplateCampos();
        novo.setOficinaId(oficinaId);
        novo.setRamo(ramo);
        novo.setVersao(atual.map(t -> t.getVersao() + 1).orElse(1));
        novo.setCampos(campos);
        novo.setCriadoPor(autor);
        novo.setAtivo(true);
        return repository.salvar(novo);
    }

    private void validarCampos(List<CampoDinamico> campos) {
        if (campos == null || campos.isEmpty()) {
            throw new RegraNegocioException("O template precisa de ao menos um campo");
        }
        Set<String> chaves = new HashSet<>();
        for (CampoDinamico campo : campos) {
            if (campo.getChave() == null || campo.getChave().isBlank()) {
                throw new RegraNegocioException("Todo campo precisa de uma chave");
            }
            if (!chaves.add(campo.getChave())) {
                throw new RegraNegocioException("Chave de campo duplicada: " + campo.getChave());
            }
            if (campo.getRotulo() == null || campo.getRotulo().isBlank()) {
                throw new RegraNegocioException("Campo sem rotulo: " + campo.getChave());
            }
            if (campo.getTipo() == null) {
                throw new RegraNegocioException("Campo sem tipo: " + campo.getChave());
            }
            if (campo.getTipo().exigeOpcoes() && campo.getOpcoes().isEmpty()) {
                throw new RegraNegocioException("Campo " + campo.getChave()
                        + " do tipo " + campo.getTipo() + " precisa de opcoes");
            }
        }
        // Uma condicional so pode apontar para um campo que existe no mesmo template.
        for (CampoDinamico campo : campos) {
            if (campo.getCondicional() != null && !chaves.contains(campo.getCondicional().campo())) {
                throw new RegraNegocioException("Campo " + campo.getChave()
                        + " condiciona a um campo inexistente: " + campo.getCondicional().campo());
            }
        }
    }

    /** Valida os valores preenchidos na OS contra o schema do template. */
    public void validarPreenchimento(TemplateCampos template, Map<String, Object> valores) {
        for (CampoDinamico campo : template.getCampos()) {
            if (!campo.isObrigatorio()) {
                continue;
            }
            // Campo condicional so e cobrado quando a condicao esta satisfeita.
            if (campo.getCondicional() != null) {
                Object valorCondicao = valores.get(campo.getCondicional().campo());
                if (!String.valueOf(campo.getCondicional().valor()).equals(String.valueOf(valorCondicao))) {
                    continue;
                }
            }
            Object valor = valores.get(campo.getChave());
            boolean vazio = valor == null
                    || (valor instanceof String s && s.isBlank())
                    || (valor instanceof List<?> l && l.isEmpty());
            if (vazio) {
                throw new RegraNegocioException("Campo obrigatorio nao preenchido: " + campo.getRotulo());
            }
            if (campo.getTipo() == TipoCampo.SELECT && !campo.getOpcoes().isEmpty()
                    && !campo.getOpcoes().contains(String.valueOf(valor))) {
                throw new RegraNegocioException("Valor invalido para " + campo.getRotulo() + ": " + valor);
            }
        }
    }
}
