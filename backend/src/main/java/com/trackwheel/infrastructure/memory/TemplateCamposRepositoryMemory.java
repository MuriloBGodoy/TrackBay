package com.trackwheel.infrastructure.memory;

import com.trackwheel.domain.model.Ramo;
import com.trackwheel.domain.model.TemplateCampos;
import com.trackwheel.domain.repository.TemplateCamposRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("dev")
public class TemplateCamposRepositoryMemory extends MemoryStore<TemplateCampos>
        implements TemplateCamposRepository {

    @Override
    protected String id(TemplateCampos t) {
        return t.getId();
    }

    @Override
    protected void atribuirId(TemplateCampos t, String id) {
        t.setId(id);
    }

    @Override
    protected String oficinaId(TemplateCampos t) {
        return t.getOficinaId();
    }

    @Override
    public TemplateCampos salvar(TemplateCampos template) {
        return persistir(template);
    }

    @Override
    public Optional<TemplateCampos> buscarPorId(String oficinaId, String id) {
        return porId(oficinaId, id);
    }

    /** O ativo e sempre a maior versao marcada como ativa. */
    @Override
    public Optional<TemplateCampos> buscarAtivoPorRamo(String oficinaId, Ramo ramo) {
        return filtrar(oficinaId, t -> t.getRamo() == ramo && t.isAtivo()).stream()
                .max(Comparator.comparingInt(TemplateCampos::getVersao));
    }

    @Override
    public Optional<TemplateCampos> buscarPorRamoEVersao(String oficinaId, Ramo ramo, int versao) {
        return primeiro(oficinaId, t -> t.getRamo() == ramo && t.getVersao() == versao);
    }

    @Override
    public List<TemplateCampos> listarPorOficina(String oficinaId) {
        return doTenant(oficinaId);
    }
}
