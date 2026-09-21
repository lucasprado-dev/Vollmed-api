package med.voll.api.domain.usuario.repository;

import med.voll.api.domain.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

// Repository = camada de acesso ao banco. Aqui é uma INTERFACE: você não escreve a implementação.
// Na inicialização, o Spring Data JPA gera uma classe por trás dos panos e a registra como bean
// (por isso o @Autowired no AutenticacaoService funciona sem você ter criado nenhuma classe).
// Analogia: é o "cardápio" de operações que você pode pedir ao banco. A cozinha (implementação) o Spring monta sozinho.
//
// extends JpaRepository<Usuario, Long>:
//   - Usuario = a entidade que este repository manipula (tabela "usuarios")
//   - Long    = o tipo da chave primária (o campo "id" da entidade)
// Só de estender, você ganha de graça os métodos prontos: save(), findById(), findAll(),
// deleteById(), count(), existsById(), etc.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Metodo derivado (query method): o Spring Data lê o NOME do metodo e monta o SQL sozinho.
    //   findBy + Login  ->  SELECT * FROM usuarios WHERE login = ?
    // O nome do atributo precisa bater com o campo da entidade ("login" em Usuario).
    // Se você escrevesse findByLoginn (typo), a aplicação nem subiria: o Spring reclama na inicialização.
    //
    // Retorna UserDetails (e não Usuario) porque quem consome é o AutenticacaoService,
    // que precisa devolver exatamente esse tipo. Funciona porque Usuario implementa UserDetails,
    // então o Spring Data devolve o objeto Usuario e ele é tratado como UserDetails.
    UserDetails findByLogin(String login);
}