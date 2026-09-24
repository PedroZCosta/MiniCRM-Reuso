package com.miniCRM.miniCRM.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/** Prova do Singleton nº 1 (SPEC-01 §6): o container entrega sempre a mesma instancia. */
@SpringBootTest
class EscopoCarteiraSingletonTest {

    @Autowired
    private EscopoCarteira primeiraInjecao;

    @Autowired
    private EscopoCarteira segundaInjecao;

    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("duas injecoes recebem o mesmo objeto")
    void mesmaInstanciaEmDoisPontos() {
        assertThat(primeiraInjecao).isSameAs(segundaInjecao);
    }

    @Test
    @DisplayName("buscar o bean duas vezes devolve o mesmo objeto")
    void mesmaInstanciaPeloContexto() {
        EscopoCarteira a = contexto.getBean(EscopoCarteira.class);
        EscopoCarteira b = contexto.getBean(EscopoCarteira.class);

        assertThat(a).isSameAs(b);
        assertThat(a).isSameAs(primeiraInjecao);
    }
}
