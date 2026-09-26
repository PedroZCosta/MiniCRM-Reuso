package com.miniCRM.miniCRM.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

/** Traduz exceções para o envelope de erro da SPEC-00 §4. Ninguém cria formato próprio. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException e) {
        return ResponseEntity.unprocessableEntity()
                .body(ErroResponse.de(422, "REGRA_NEGOCIO", e.getMessage()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErroResponse.de(404, "NAO_ENCONTRADO", e.getMessage()));
    }

    @ExceptionHandler(SemPermissaoException.class)
    public ResponseEntity<ErroResponse> semPermissao(SemPermissaoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErroResponse.de(403, "SEM_PERMISSAO", e.getMessage()));
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResponse> conflito(ConflitoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResponse.de(409, "CONFLITO", e.getMessage()));
    }

    @ExceptionHandler(CredencialInvalidaException.class)
    public ResponseEntity<ErroResponse> credencialInvalida(CredencialInvalidaException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErroResponse.de(401, "NAO_AUTORIZADO", e.getMessage()));
    }

    @ExceptionHandler(UsuarioBloqueadoException.class)
    public ResponseEntity<ErroResponse> usuarioBloqueado(UsuarioBloqueadoException e) {
        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(ErroResponse.de(423, "USUARIO_BLOQUEADO", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException e) {
        List<String> detalhes = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(new ErroResponse(
                LocalDateTime.now(), 400, "REQUISICAO_INVALIDA", "Campos inválidos", detalhes));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> acessoNegado(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErroResponse.de(403, "ACESSO_NEGADO", "Você não tem permissão para esta ação"));
    }



}
