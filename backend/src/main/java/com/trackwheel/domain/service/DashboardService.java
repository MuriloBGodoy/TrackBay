package com.trackwheel.domain.service;

import com.trackwheel.domain.model.OrdemServico;
import com.trackwheel.domain.model.Produto;
import com.trackwheel.domain.model.StatusOS;
import com.trackwheel.domain.repository.OrdemServicoRepository;
import com.trackwheel.domain.repository.ProdutoRepository;
import com.trackwheel.domain.repository.VendaBalcaoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/** Numeros da tela inicial: faturamento, OS abertas, ticket medio e alertas. */
@Service
public class DashboardService {

    /** Oficinas sao da regiao do usuario: o dia fecha no fuso de Sao Paulo. */
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private final OrdemServicoRepository osRepository;
    private final ProdutoRepository produtoRepository;
    private final VendaBalcaoRepository vendaRepository;

    public DashboardService(OrdemServicoRepository osRepository,
                            ProdutoRepository produtoRepository,
                            VendaBalcaoRepository vendaRepository) {
        this.osRepository = osRepository;
        this.produtoRepository = produtoRepository;
        this.vendaRepository = vendaRepository;
    }

    public record Resumo(
            BigDecimal faturamentoDia,
            BigDecimal faturamentoMes,
            long osAbertas,
            long osAguardandoPeca,
            long osProntas,
            BigDecimal ticketMedio,
            long produtosEstoqueBaixo,
            List<AlertaEstoque> alertasEstoque
    ) {
    }

    public record AlertaEstoque(String produtoId, String nome, BigDecimal estoqueAtual, BigDecimal estoqueMinimo) {
    }

    public Resumo resumo(String oficinaId) {
        LocalDate hoje = LocalDate.now(FUSO);
        Instant inicioDia = hoje.atStartOfDay(FUSO).toInstant();
        Instant fimDia = hoje.plusDays(1).atStartOfDay(FUSO).toInstant();
        Instant inicioMes = hoje.withDayOfMonth(1).atStartOfDay(FUSO).toInstant();

        // Faturamento conta o que foi entregue, nao o que foi orcado.
        List<OrdemServico> entreguesNoDia = osRepository.listarPorPeriodo(oficinaId, inicioDia, fimDia)
                .stream().filter(os -> os.getStatus() == StatusOS.ENTREGUE).toList();
        List<OrdemServico> entreguesNoMes = osRepository.listarPorPeriodo(oficinaId, inicioMes, fimDia)
                .stream().filter(os -> os.getStatus() == StatusOS.ENTREGUE).toList();

        BigDecimal faturamentoDia = somar(entreguesNoDia)
                .add(somarVendas(oficinaId, inicioDia, fimDia));
        BigDecimal faturamentoMes = somar(entreguesNoMes)
                .add(somarVendas(oficinaId, inicioMes, fimDia));

        List<OrdemServico> todas = osRepository.listarPorOficina(oficinaId);
        long abertas = todas.stream().filter(os -> !os.getStatus().isFinal()).count();
        long aguardandoPeca = todas.stream().filter(os -> os.getStatus() == StatusOS.AGUARDANDO_PECA).count();
        long prontas = todas.stream().filter(os -> os.getStatus() == StatusOS.PRONTA).count();

        BigDecimal ticketMedio = entreguesNoMes.isEmpty()
                ? BigDecimal.ZERO
                : somar(entreguesNoMes).divide(BigDecimal.valueOf(entreguesNoMes.size()), 2, RoundingMode.HALF_UP);

        List<Produto> estoqueBaixo = produtoRepository.listarEstoqueBaixo(oficinaId);
        List<AlertaEstoque> alertas = estoqueBaixo.stream()
                .map(p -> new AlertaEstoque(p.getId(), p.getNome(), p.getEstoqueAtual(), p.getEstoqueMinimo()))
                .toList();

        return new Resumo(faturamentoDia, faturamentoMes, abertas, aguardandoPeca, prontas,
                ticketMedio, estoqueBaixo.size(), alertas);
    }

    private BigDecimal somar(List<OrdemServico> ordens) {
        return ordens.stream()
                .map(OrdemServico::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal somarVendas(String oficinaId, Instant de, Instant ate) {
        return vendaRepository.listarPorPeriodo(oficinaId, de, ate).stream()
                .filter(v -> !v.isCancelada())
                .map(v -> v.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
