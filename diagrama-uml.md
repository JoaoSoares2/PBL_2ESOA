```mermaid
classDiagram
    class Usuario {
        -Long id
        -String nome
        -String email
        -String senha
        -String cpf
        -String telefone
        -Autenticacao autenticacao
        -PerfilInvestidor perfilInvestidor
        -List~Relatorio~ relatorios
        -List~Empresa~ empresas
        +cadastrar()
        +autenticar(String email, String senha) boolean
        +atualizarCadastro(String nome, String email, String telefone)
        +vizualizarCarteira() List
        +consultarRelatorios() List
    }

    class Autenticacao {
        -Long id
        -String login
        -String senha
        -AutenticacaoMultifator autenticacaoMultifator
        +validarCredenciais(String login, String senha) boolean
        +encerrarSessao()
    }

    class AutenticacaoMultifator {
        -Long id
        -String codigo
        -Date expiracao
        +gerarCodigo() String
        +validarCodigo(String codigo) boolean
    }

    class PerfilInvestidor {
        -Long id
        -String nomePerfil
        -String descricao
        -String nivelRisco
        +exibirPerfil()
        +avaliarRisco() String
    }

    class Relatorio {
        <<abstract>>
        -Long id
        -String tipo
        -Date dataGeracao
        -String conteudo
        +gerarRelatorio()
        +gerarRelatorio(String filtro)
        +gerarRelatorio(String filtro, Date dataInicio)
    }

    class RelatorioAnual {
        -int ano
        +gerarRelatorio()
    }

    class RelatorioMensal {
        -int mes
        -int ano
        +gerarRelatorio()
    }

    class Empresa {
        -Long id
        -String nome
        -String cnpj
        -List~Carteira~ carteiras
        +cadastrarEmpresa()
        +atualizarEmpresa(String nome)
        +atualizarEmpresa(String nome, String cnpj)
        +listarCarteiras() List
        +adicionarCarteira(Carteira carteira)
    }

    class Carteira {
        -Long id
        -String nome
        -Double saldoTotal
        -Date dataCriacao
        -List~Ativo~ ativos
        -List~Transacao~ transacoes
        +adicionarAtivo(Ativo ativo)
        +adicionarAtivo(String nome, Double valor, Double quantidade)
        +removerAtivo(Ativo ativo)
        +removerAtivo(String nome)
        +calcularSaldo() Double
        +listarTransacoes() List
        +registrarTransacao(Transacao transacao)
    }

    class Ativo {
        <<abstract>>
        -Long id
        -String nome
        -Double valorAtual
        -Double quantidade
        +atualizarCotacao(Double novoValor)
        +atualizarCotacao(Double variacao, boolean percentual)
        +calcularValorTotal() Double
    }

    class AtivoRendaFixa {
        -Double taxaJurosAnual
        +calcularValorTotal() Double
    }

    class AtivoRendaVariavel {
        -String ticker
        -Double variacaoDiaria
        +calcularValorTotal() Double
    }

    class Transacao {
        -Long id
        -String tipo
        -Double valor
        -Date data
        -String descricao
        -Ativo ativo
        +registrarCompra(Double valor) Transacao
        +registrarCompra(Double valor, String descricao) Transacao
        +registrarCompra(Ativo ativo) Transacao
        +registrarVenda(Double valor) Transacao
        +registrarVenda(Double valor, String descricao) Transacao
        +registrarVenda(Ativo ativo) Transacao
        +consultarTransacoes(List transacoes)
    }

    %% Herança
    AtivoRendaFixa --|> Ativo
    AtivoRendaVariavel --|> Ativo
    RelatorioAnual --|> Relatorio
    RelatorioMensal --|> Relatorio

    %% Relacionamentos
    Usuario "1" -- "1" Autenticacao : possui
    Autenticacao "1" -- "1" AutenticacaoMultifator : utiliza
    Usuario "1" -- "1" PerfilInvestidor : define
    Usuario "1" -- "0..*" Relatorio : solicita
    Usuario "1" -- "0..*" Empresa : gerencia
    Empresa "1" *-- "1..*" Carteira : contém
    Carteira "1" -- "0..*" Ativo : armazena
    Carteira "1" *-- "0..*" Transacao : registra
    Transacao "0..*" --> "0..1" Ativo : vincula
```
