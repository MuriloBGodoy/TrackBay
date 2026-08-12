package com.trackwheel.domain.repository;

import com.trackwheel.domain.model.Ramo;
import com.trackwheel.domain.model.TemplateCampos;

import java.util.List;
import java.util.Optional;

public interface TemplateCamposRepository {

    TemplateCampos salvar(TemplateCampos template);

    Optional<TemplateCampos> buscarPorId(String oficinaId, String id);

    /** Template vigente de um ramo — o que novas OS usam. */
    Optional<TemplateCampos> buscarAtivoPorRamo(String oficinaId, Ramo ramo);

    /** Versao especifica: OS antiga renderiza com o schema que tinha na criacao. */
    Optional<TemplateCampos> buscarPorRamoEVersao(String oficinaId, Ramo ramo, int versao);

    List<TemplateCampos> listarPorOficina(String oficinaId);
}
