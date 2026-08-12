package com.trackwheel.api;

import com.trackwheel.domain.service.RecursoNaoEncontradoException;
import com.trackwheel.domain.service.RegraNegocioException;
import com.trackwheel.security.AcessoNegadoException;
import com.trackwheel.security.NaoAutenticadoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/** Tratamento de erro no padrao RFC 7807 (Problem Details). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BASE_TIPO = "https://trackwheel.app/erros/";

    @ExceptionHandler(RegraNegocioException.class)
    public ProblemDetail regraNegocio(RegraNegocioException e, HttpServletRequest req) {
        return montar(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negocio violada",
                e.getMessage(), "regra-negocio", req);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(RecursoNaoEncontradoException e, HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, "Recurso nao encontrado",
                e.getMessage(), "nao-encontrado", req);
    }

    @ExceptionHandler(NaoAutenticadoException.class)
    public ProblemDetail naoAutenticado(NaoAutenticadoException e, HttpServletRequest req) {
        return montar(HttpStatus.UNAUTHORIZED, "Nao autenticado",
                e.getMessage(), "nao-autenticado", req);
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ProblemDetail acessoNegado(AcessoNegadoException e, HttpServletRequest req) {
        return montar(HttpStatus.FORBIDDEN, "Acesso negado",
                e.getMessage(), "acesso-negado", req);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail estadoInvalido(IllegalStateException e, HttpServletRequest req) {
        return montar(HttpStatus.CONFLICT, "Operacao invalida no estado atual",
                e.getMessage(), "estado-invalido", req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail argumentoInvalido(IllegalArgumentException e, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "Requisicao invalida",
                e.getMessage(), "requisicao-invalida", req);
    }

    /** Sem isto, a foto grande do celular vira um 500 generico. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail arquivoGrande(MaxUploadSizeExceededException e, HttpServletRequest req) {
        return montar(HttpStatus.PAYLOAD_TOO_LARGE, "Arquivo muito grande",
                "A imagem passa do limite de 10 MB. Tire a foto com resolucao menor.",
                "arquivo-grande", req);
    }

    /** Erros de @Valid: devolve o detalhe campo a campo para o front destacar cada input. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacao(MethodArgumentNotValidException e, HttpServletRequest req) {
        Map<String, String> erros = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erro -> erros.put(erro.getField(), erro.getDefaultMessage()));

        ProblemDetail pd = montar(HttpStatus.BAD_REQUEST, "Dados invalidos",
                "Confira os campos destacados", "validacao", req);
        pd.setProperty("errors", erros);
        return pd;
    }

    /**
     * Rota inexistente. Sem isto o handler generico abaixo engoliria o 404 que o Spring
     * ja traz e devolveria 500 — com stack trace no log a cada varredura de robo.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail rotaNaoEncontrada(NoResourceFoundException e, HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, "Recurso nao encontrado",
                "Rota inexistente: " + req.getRequestURI(), "nao-encontrado", req);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception e, HttpServletRequest req) {
        // Erro nao mapeado: loga o stack completo mas nao vaza detalhe interno na resposta.
        log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), e);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente.", "erro-interno", req);
    }

    private ProblemDetail montar(HttpStatus status, String titulo, String detalhe,
                                 String tipo, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalhe);
        pd.setTitle(titulo);
        pd.setType(URI.create(BASE_TIPO + tipo));
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }
}
