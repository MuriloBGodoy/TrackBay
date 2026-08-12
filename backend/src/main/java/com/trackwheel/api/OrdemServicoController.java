package com.trackwheel.api;

import com.trackwheel.domain.model.Oficina;
import com.trackwheel.domain.model.OrdemServico;
import com.trackwheel.domain.model.StatusOS;
import com.trackwheel.domain.model.TemplateCampos;
import com.trackwheel.domain.service.OficinaService;
import com.trackwheel.domain.service.OrdemServicoService;
import com.trackwheel.domain.service.RecursoNaoEncontradoException;
import com.trackwheel.domain.service.TemplateCamposService;
import com.trackwheel.infrastructure.pdf.OrdemServicoPdf;
import com.trackwheel.security.AcessoNegadoException;
import com.trackwheel.security.ContextoTenant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ordens")
@Tag(name = "Ordens de Servico", description = "Fluxo completo: orcamento, aprovacao, execucao e entrega")
public class OrdemServicoController {

    private final OrdemServicoService service;
    private final OficinaService oficinaService;
    private final TemplateCamposService templateService;
    private final OrdemServicoPdf pdf;

    public OrdemServicoController(OrdemServicoService service, OficinaService oficinaService,
                                  TemplateCamposService templateService, OrdemServicoPdf pdf) {
        this.service = service;
        this.oficinaService = oficinaService;
        this.templateService = templateService;
        this.pdf = pdf;
    }

    @GetMapping
    @Operation(summary = "Lista as OS",
            description = "Mecanico ve apenas as OS atribuidas a ele. Filtro opcional por status.")
    public List<OrdemServico> listar(@RequestParam(required = false) StatusOS status) {
        String oficinaId = ContextoTenant.oficinaId();
        if (status != null) {
            return service.listarPorStatus(oficinaId, status);
        }
        return service.listar(oficinaId, ContextoTenant.usuario());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma OS por id")
    public OrdemServico porId(@PathVariable String id) {
        return service.buscarOuFalhar(ContextoTenant.oficinaId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Abre uma OS como orcamento",
            description = "Gera o numero sequencial, congela a versao do schema de campos dinamicos "
                    + "e valida os campos obrigatorios do ramo.")
    public OrdemServico criar(@RequestBody OrdemServico os) {
        if (!ContextoTenant.usuario().podeCriarOS()) {
            throw new AcessoNegadoException("Seu papel nao permite abrir OS");
        }
        os.setId(null);
        return service.criar(ContextoTenant.oficinaId(), os, ContextoTenant.usuario());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza itens, valores e campos dinamicos da OS")
    public OrdemServico atualizar(@PathVariable String id, @RequestBody OrdemServico os) {
        return service.atualizar(ContextoTenant.oficinaId(), id, os, ContextoTenant.usuario());
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Muda o status da OS",
            description = "Respeita o fluxo permitido. Ao entregar, baixa o estoque das pecas proprias.")
    public OrdemServico mudarStatus(@PathVariable String id, @RequestBody MudancaStatus corpo) {
        return service.mudarStatus(ContextoTenant.oficinaId(), id, corpo.status(),
                corpo.observacao(), ContextoTenant.usuario());
    }

    public record MudancaStatus(StatusOS status, String observacao) {
    }

    @PostMapping("/{id}/aprovar")
    @Operation(summary = "Aprova o orcamento",
            description = "Aceita a assinatura do cliente coletada no canvas do mobile.")
    public OrdemServico aprovar(@PathVariable String id, @RequestBody(required = false) Aprovacao corpo) {
        String assinatura = corpo == null ? null : corpo.assinaturaUrl();
        return service.aprovar(ContextoTenant.oficinaId(), id, assinatura, ContextoTenant.usuario());
    }

    public record Aprovacao(String assinaturaUrl) {
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "PDF da OS/orcamento",
            description = "Documento pronto para imprimir ou enviar ao cliente: dados da oficina, "
                    + "veiculo, campos do ramo, itens, totais e garantia.")
    public ResponseEntity<byte[]> pdf(@PathVariable String id) {
        String oficinaId = ContextoTenant.oficinaId();
        OrdemServico os = service.buscarOuFalhar(oficinaId, id);
        Oficina oficina = oficinaService.buscarPorId(oficinaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Oficina", oficinaId));
        TemplateCampos template = os.getRamo() == null ? null
                : templateService.buscarVersao(oficinaId, os.getRamo(), os.getSchemaVersion()).orElse(null);

        String nomeArquivo = (os.getNumero() == null ? "os" : os.getNumero())
                .replaceAll("[^A-Za-z0-9-]", "-") + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(nomeArquivo).build().toString())
                .body(pdf.gerar(oficina, os, template));
    }

    @GetMapping("/{id}/transicoes")
    @Operation(summary = "Status para os quais esta OS pode ir a partir do atual")
    public Map<String, Object> transicoes(@PathVariable String id) {
        OrdemServico os = service.buscarOuFalhar(ContextoTenant.oficinaId(), id);
        return Map.of(
                "atual", os.getStatus(),
                "permitidos", os.getStatus().proximosPermitidos()
        );
    }
}
