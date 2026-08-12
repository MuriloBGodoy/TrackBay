package com.trackwheel.api;

import com.trackwheel.domain.model.CampoDinamico;
import com.trackwheel.domain.model.Ramo;
import com.trackwheel.domain.model.TemplateCampos;
import com.trackwheel.domain.service.RecursoNaoEncontradoException;
import com.trackwheel.domain.service.TemplateCamposService;
import com.trackwheel.security.AcessoNegadoException;
import com.trackwheel.security.ContextoTenant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** O diferencial do produto: o catalogo de campos que muda conforme o ramo. */
@RestController
@RequestMapping("/api/templates")
@Tag(name = "Campos dinamicos", description = "Templates versionados de campos por ramo")
public class TemplateCamposController {

    private final TemplateCamposService service;

    public TemplateCamposController(TemplateCamposService service) {
        this.service = service;
    }

    @GetMapping("/ramos")
    @Operation(summary = "Ramos disponiveis no sistema")
    public List<Map<String, String>> ramos() {
        return Arrays.stream(Ramo.values())
                .map(r -> Map.of("valor", r.name(), "rotulo", r.getRotulo()))
                .toList();
    }

    @GetMapping
    @Operation(summary = "Lista os templates da oficina")
    public List<TemplateCampos> listar() {
        return service.listar(ContextoTenant.oficinaId());
    }

    @GetMapping("/{ramo}")
    @Operation(summary = "Template vigente de um ramo",
            description = "Passe versao para recuperar o schema com que uma OS antiga foi criada.")
    public TemplateCampos porRamo(@PathVariable Ramo ramo,
                                  @RequestParam(required = false) Integer versao) {
        String oficinaId = ContextoTenant.oficinaId();
        return (versao == null
                ? service.buscarAtivo(oficinaId, ramo)
                : service.buscarVersao(oficinaId, ramo, versao))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Template do ramo", ramo.name()));
    }

    @PutMapping("/{ramo}")
    @Operation(summary = "Salva uma nova versao do template",
            description = "Cria uma versao nova em vez de sobrescrever: OS antigas continuam "
                    + "renderizando o schema original. So o dono pode alterar.")
    public TemplateCampos salvar(@PathVariable Ramo ramo, @RequestBody List<CampoDinamico> campos) {
        if (!ContextoTenant.usuario().podeGerenciarOficina()) {
            throw new AcessoNegadoException("Apenas o dono pode alterar os campos da OS");
        }
        return service.novaVersao(ContextoTenant.oficinaId(), ramo, campos,
                ContextoTenant.usuario().getId());
    }
}
