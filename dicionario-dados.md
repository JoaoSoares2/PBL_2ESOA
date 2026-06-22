# Dicionário de Dados

## Usuario

| Campo | Tipo | PK/FK | Descrição |
|---|---|---|---|
| id | Long | PK | Identificador único do usuário |
| nome | String | | Nome completo |
| email | String | | Endereço de e-mail |
| senha | String | | Senha de acesso |
| cpf | String | | Cadastro de Pessoa Física |
| telefone | String | | Número de telefone |
| autenticacao | Autenticacao | FK | Credenciais de login do usuário |
| perfilInvestidor | PerfilInvestidor | FK | Perfil de risco do investidor |

---

## Autenticacao

| Campo | Tipo | PK/FK | Descrição |
|---|---|---|---|
| id | Long | PK | Identificador único |
| login | String | | Login de acesso ao sistema |
| senha | String | | Senha criptografada |
| autenticacaoMultifator | AutenticacaoMultifator | FK | Configuração de autenticação multifator |

---

## AutenticacaoMultifator

| Campo | Tipo | PK/FK | Descrição |
|---|---|---|---|
| id | Long | PK | Identificador único |
| codigo | String | | Código de verificação de 6 dígitos |
| expiracao | Date | | Data e hora de expiração do código (5 minutos) |

---

## PerfilInvestidor

| Campo | Tipo | PK/FK | Descrição |
|---|---|---|---|
| id | Long | PK | Identificador único |
| nomePerfil | String | | Nome do perfil (Conservador, Moderado, Agressivo) |
| descricao | String | | Descrição detalhada do perfil |
| nivelRisco | String | | Nível de risco: BAIXO, MEDIO ou ALTO |

---

## Empresa

| Campo | Tipo | PK/FK | Descrição |
|---|---|---|---|
| id | Long | PK | Identificador único |
| nome | String | | Razão social da empresa |
| cnpj | String | | CNPJ da empresa |

---

## UsuarioEmpresa

Entidade associativa que resolve o relacionamento N:M entre Usuario e Empresa.

| Campo | Tipo | PK/FK | Descrição |
|---|---|---|---|
| id | Long | PK | Identificador único do vínculo |
| usuario | Usuario | FK | Referência ao usuário vinculado |
| empresa | Empresa | FK | Referência à empresa vinculada |
| cargo | String | | Cargo do usuário na empresa |
| dataVinculo | Date | | Data de início do vínculo |

---

## Carteira

| Campo | Tipo | PK/FK | Descrição |
|---|---|---|---|
| id | Long | PK | Identificador único |
| nome | String | | Nome da carteira de investimentos |
| saldoTotal | Double | | Valor total calculado da carteira |
| dataCriacao | Date | | Data de criação da carteira |

---

## CarteiraAtivo

Entidade associativa que resolve o relacionamento N:M entre Carteira e Ativo.

| Campo | Tipo | PK/FK | Descrição |
|---|---|---|---|
| id | Long | PK | Identificador único |
| carteira | Carteira | FK | Referência à carteira |
| ativo | Ativo | FK | Referência ao ativo |
| quantidade | Double | | Quantidade de unidades do ativo na carteira |
| precoMedio | Double | | Preço médio de aquisição por unidade |

---

## Ativo (abstract)

Classe abstrata base para todos os tipos de ativos financeiros.

| Campo | Tipo | PK/FK | Descrição |
|---|---|---|---|
| id | Long | PK | Identificador único |
| nome | String | | Nome do ativo financeiro |
| valorAtual | Double | | Cotação ou valor atual por unidade |
| quantidade | Double | | Quantidade base do ativo |

---

## Acao (extends Ativo)

Ação negociada em bolsa de valores. Herda todos os campos de Ativo.

| Campo | Tipo | PK/FK | Descrição |
|---|---|---|---|
| ticker | String | | Código de negociação (ex: PETR4, VALE3) |
| bolsa | String | | Bolsa de valores (ex: B3, NYSE) |
| variacaoDiaria | Double | | Variação percentual do dia |

---

## FundoInvestimento (extends Ativo)

Fundo de investimento administrado. Herda todos os campos de Ativo.

| Campo | Tipo | PK/FK | Descrição |
|---|---|---|---|
| taxaAdministracao | Double | | Taxa de administração em % ao ano |
| tipoFundo | String | | Tipo do fundo: RENDA_FIXA, MULTIMERCADO ou ACOES |

---

## Transacao

| Campo | Tipo | PK/FK | Descrição |
|---|---|---|---|
| id | Long | PK | Identificador único |
| tipo | String | | Tipo da operação: COMPRA ou VENDA |
| valor | Double | | Valor financeiro da transação |
| data | Date | | Data e hora da transação |
| descricao | String | | Descrição ou observação da transação |
| ativo | Ativo | FK | Referência ao ativo negociado |

---

## Relatorio (abstract)

Classe abstrata base para relatórios gerados pelo sistema.

| Campo | Tipo | PK/FK | Descrição |
|---|---|---|---|
| id | Long | PK | Identificador único |
| tipo | String | | Tipo do relatório (ANUAL, MENSAL) |
| dataGeracao | Date | | Data de geração do relatório |
| conteudo | String | | Conteúdo textual do relatório |

---

## RelatorioAnual (extends Relatorio)

Relatório com período de referência anual. Herda todos os campos de Relatorio.

| Campo | Tipo | PK/FK | Descrição |
|---|---|---|---|
| ano | int | | Ano de referência do relatório |

---

## RelatorioMensal (extends Relatorio)

Relatório com período de referência mensal. Herda todos os campos de Relatorio.

| Campo | Tipo | PK/FK | Descrição |
|---|---|---|---|
| mes | int | | Mês de referência (1 a 12) |
| ano | int | | Ano de referência |
