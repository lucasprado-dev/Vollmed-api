package med.voll.api.domain.medico.repository;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import med.voll.api.domain.medico.Especialidade;
import med.voll.api.domain.medico.Medico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface MedicoRepository extends JpaRepository<Medico, Long> {
    Page<Medico> findAllByAtivoTrue(Pageable paginacao);

    @Query("""
    select m 
    from Medico m 
    where m.ativo = true 
    and m.especialidade = :especialidade 
    and m.id not in (
        select c.medico.id 
        from Consulta c 
        where c.data = :data
    )
    order by rand() 
    limit 1
    """)
// select m from Medico m
//   -> busca objetos Medico inteiros, apelidados de "m"

// where m.ativo = true
//   -> filtra apenas médicos que estão ativos no sistema

// and m.especialidade = :especialidade
//   -> filtra pela especialidade recebida como parâmetro do metodo

// and m.id not in (...)
//   -> subquery: pega os IDs de médicos que JÁ têm consulta
//      marcada na data informada (:data), e exclui esses médicos
//      do resultado (ou seja: só sobram os médicos livres)

// order by rand()
//   -> embaralha o resultado, pra não pegar sempre o mesmo médico
//      (atenção: rand() é função nativa do MySQL; no Oracle seria
//      dbms_random.value, com nativeQuery = true)

// limit 1
//   -> pega só 1 resultado após o embaralhamento (o "sorteado")
    Medico escolherMedicoAleatorioLivreNaData(Especialidade especialidade, LocalDateTime data);

    @Query("""
        select m.ativo
        from Medico m
        where m.id = :id
        """)
    Boolean findAtivoById(Long id);
}
