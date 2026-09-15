package com.miniCRM.miniCRM.config;

import com.miniCRM.miniCRM.model.MotivoPerda;
import com.miniCRM.miniCRM.repository.MotivoPerdaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SeedMotivosPerda extends SeedBase {

    private final MotivoPerdaRepository motivoPerdaRepository;

    @Override
    protected boolean jaExecutou() {
        return motivoPerdaRepository.count() > 0;
    }

    @Override
    protected void criarDados() {
        motivoPerdaRepository.saveAll(List.of(
                new MotivoPerda(null, "preço"),
                new MotivoPerda(null, "concorrente"),
                new MotivoPerda(null, "sem verba"),
                new MotivoPerda(null, "sem resposta")));
    }

    @Override
    protected String nome() {
        return "SeedMotivosPerda";
    }
}
