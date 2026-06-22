# Normalização da Base de Dados

Demonstração de que a modelagem do Sistema de Gestão de Investimentos atende às três primeiras formas normais.

---

## 1FN – Primeira Forma Normal

**Regra:** Todos os atributos devem ser atômicos (indivisíveis) e não devem existir grupos repetitivos.

### Verificação de atomicidade

| Entidade | Atributos | Atômicos? |
|---|---|---|
| Usuario | nome, email, cpf, telefone, senha | ✅ Cada campo armazena um único valor |
| Empresa | nome, cnpj | ✅ Valores indivisíveis |
| Carteira | nome, saldoTotal, dataCriacao | ✅ Valores indivisíveis |
| Ativo | nome, valorAtual, quantidade | ✅ Valores indivisíveis |
| Acao | ticker, bolsa, variacaoDiaria | ✅ Valores indivisíveis |
| FundoInvestimento | taxaAdministracao, tipoFundo | ✅ Valores indivisíveis |
| Transacao | tipo, valor, data, descricao | ✅ Valores indivisíveis |
| Relatorio | tipo, dataGeracao, conteudo | ✅ Valores indivisíveis |
| Autenticacao | login, senha | ✅ Valores indivisíveis |
| AutenticacaoMultifator | codigo, expiracao | ✅ Valores indivisíveis |
| PerfilInvestidor | nomePerfil, descricao, nivelRisco | ✅ Valores indivisíveis |

### Eliminação de grupos repetitivos

| Situação original | Problema | Solução |
|---|---|---|
| Usuario possuía `List<Empresa>` diretamente | Grupo repetitivo embutido, sem atributos da relação | Entidade associativa `UsuarioEmpresa` com cargo e dataVinculo |
| Carteira possuía `List<Ativo>` diretamente | Grupo repetitivo embutido, sem atributos da relação | Entidade associativa `CarteiraAtivo` com quantidade e precoMedio |

**Conclusão:** Todas as entidades atendem à 1FN — atributos atômicos e grupos repetitivos resolvidos por entidades associativas.

---

## 2FN – Segunda Forma Normal

**Regra:** Deve atender à 1FN e todos os atributos não-chave devem depender totalmente da chave primária (sem dependências parciais).

### Entidades com chave simples

Nas entidades com chave primária simples (`id`), não existe possibilidade de dependência parcial — todos os atributos dependem integralmente do `id`. Exemplos:

- `Usuario`: nome, email, cpf → dependem totalmente de `id`
- `Empresa`: nome, cnpj → dependem totalmente de `id`
- `Carteira`: nome, saldoTotal, dataCriacao → dependem totalmente de `id`

### Entidades associativas (chave composta conceitual)

| Entidade | Chave Conceitual | Atributo | Depende totalmente da chave? |
|---|---|---|---|
| UsuarioEmpresa | usuario + empresa | cargo | ✅ O cargo só existe na relação entre um usuário e uma empresa específicos |
| UsuarioEmpresa | usuario + empresa | dataVinculo | ✅ A data de vínculo pertence à relação, não ao usuário nem à empresa |
| CarteiraAtivo | carteira + ativo | quantidade | ✅ A quantidade só faz sentido para um ativo específico em uma carteira específica |
| CarteiraAtivo | carteira + ativo | precoMedio | ✅ O preço médio depende de quando e quanto foi comprado naquela carteira |

**Conclusão:** Todas as entidades atendem à 2FN — não há dependências parciais.

---

## 3FN – Terceira Forma Normal

**Regra:** Deve atender à 2FN e não devem existir dependências transitivas (atributo não-chave dependendo de outro atributo não-chave).

### Separações que eliminam dependências transitivas

| Entidade separada | Separada de | Justificativa |
|---|---|---|
| PerfilInvestidor | Usuario | Se `nomePerfil`, `nivelRisco` e `descricao` estivessem em Usuario, haveria dependência transitiva: `id_usuario → perfilInvestidor → nivelRisco`. Ao separar, cada atributo depende diretamente do `id` de PerfilInvestidor |
| AutenticacaoMultifator | Autenticacao | `codigo` e `expiracao` pertencem ao mecanismo MFA, não à autenticação base. Se estivessem juntos, `id_autenticacao → mfa → codigo` seria transitiva |
| Empresa | Usuario | `nome` e `cnpj` da empresa são independentes do usuário. A relação passa pela entidade associativa UsuarioEmpresa |
| Transacao | CarteiraAtivo | `tipo`, `valor` e `data` são eventos pontuais, independentes do estado atual do ativo na carteira |

### Verificação nas entidades associativas

| Entidade | Atributos não-chave | Dependência transitiva? |
|---|---|---|
| UsuarioEmpresa | cargo, dataVinculo | ❌ Nenhum depende do outro; ambos dependem diretamente da relação |
| CarteiraAtivo | quantidade, precoMedio | ❌ Nenhum depende do outro; ambos dependem diretamente da relação |

**Conclusão:** Não existem dependências transitivas. A modelagem atende à 3FN.

---

## Conclusão Final

| Forma Normal | Status | Evidência Principal |
|---|---|---|
| 1FN | ✅ | Atributos atômicos; grupos repetitivos resolvidos por `UsuarioEmpresa` e `CarteiraAtivo` |
| 2FN | ✅ | Atributos de `UsuarioEmpresa` (cargo, dataVinculo) e `CarteiraAtivo` (quantidade, precoMedio) dependem totalmente da chave composta |
| 3FN | ✅ | Entidades separadas (`PerfilInvestidor`, `AutenticacaoMultifator`) eliminam dependências transitivas |

A modelagem está normalizada até a 3FN.
