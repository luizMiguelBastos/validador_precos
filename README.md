# Validador de Preços de Venda

Protótipo de uma API que confere se o preço proposto por um vendedor cobre o custo do produto e a margem mínima da empresa, antes de o pedido ser fechado.

> Projeto de estudo. Todos os produtos, custos e margens são **fictícios**.

## O problema

Em uma indústria de plásticos que importa matéria-prima em dólar, o custo de cada peça muda junto com o câmbio. Um preço que dava lucro no mês passado pode dar prejuízo hoje, e o vendedor não percebe na hora de negociar.

O validador responde uma pergunta simples: **esse preço cobre o custo e a margem mínima?**

## Como funciona

```
Vendedor informa produto e preço
        │
        ▼
Busca o produto no banco (H2)
        │
        ▼
Busca a cotação do dólar na API do Banco Central
        │
        ▼
Código calcula o preço mínimo e decide: aprovado ou bloqueado
        │
        ▼
IA (Ollama) explica o resultado em português simples
```

### A regra do cálculo

```
custo do plástico = peso da peça (kg) × preço da matéria-prima (US$/kg) × dólar
custo total       = custo do plástico + outros custos (energia, máquina, mão de obra)
preço mínimo      = custo total × 1,15   (margem mínima de 15%)
```

Exemplo com o produto ABS cadastrado e dólar a R$ 5,0096:

| Etapa | Conta | Resultado |
|---|---|---|
| Custo do plástico | 0,05 × 2,00 × 5,0096 | R$ 0,50 |
| Custo total | 0,50 + 0,40 | R$ 0,90 |
| Preço mínimo | 0,90 × 1,15 | **R$ 1,04** |

Um preço proposto de R$ 1,00 é **bloqueado**. Um de R$ 1,50 é **aprovado**.

## Onde entra a IA (e onde não entra)

Princípio do projeto: **números, o código calcula; interpretação, a IA faz.**

- A conta, a cotação e a decisão de aprovar ou bloquear são feitas **em código**, porque número não pode ter erro.
- A IA recebe os números já calculados e só transforma o resultado em uma frase para o vendedor.
- Se o Ollama estiver fora do ar, a validação **continua funcionando** e devolve uma mensagem padrão no lugar da explicação.

## Tecnologias

- Java 25 e Spring Boot 4
- Spring Data JPA e H2 (banco em memória, com carga inicial no `data.sql`)
- Bean Validation
- RestClient, para consumir a API pública do Banco Central (série SGS 1, dólar comercial)
- Spring AI com Ollama (modelo `llama3.2`)
- JUnit 5 e Mockito
- HTML, CSS e JavaScript puro no front-end

## Como rodar

**Pré-requisitos:** Java 25 e [Ollama](https://ollama.com) com o modelo `llama3.2`.

```bash
ollama pull llama3.2
./mvnw spring-boot:run
```

Depois, abra **http://localhost:8080** no navegador.

> A primeira validação pode levar alguns segundos enquanto o Ollama carrega o modelo.

## Endpoint

### `POST /validar`

Requisição:

```json
{
  "nome": "ABS",
  "valorProposto": 1.00
}
```

Resposta `200 OK`:

```json
{
  "nomeproduto": "ABS",
  "valorProposto": 1.00,
  "valorMinimo": 1.04,
  "cotacaoDolar": 5.0096,
  "aprovado": false,
  "explicacao": "O preço foi bloqueado, falta R$ 0.04 para atingir o preço mínimo de R$ 1.04. Sugiro que você negocie ou peça aprovação do gerente para ajustar o preço."
}
```

| Situação | Status |
|---|---|
| Validação feita (aprovada ou bloqueada) | `200 OK` |
| Nome em branco, preço vazio ou negativo | `400 Bad Request` |
| Produto não cadastrado | `404 Not Found` |

## Testes

```bash
./mvnw test
```

Testes unitários com Mockito simulando o banco, a API do dólar e a IA:

- cálculo do preço mínimo e arredondamento para 2 casas
- preço acima, igual e abaixo do mínimo
- cotação do dólar buscada uma única vez por validação
- produto inexistente retorna 404 sem chamar o dólar nem a IA
- falha da IA não derruba a validação

## Próximos passos

- **Cache diário da cotação**, para não depender da API do Banco Central a cada validação
- **Histórico de validações**, registrando quem pediu, qual preço e o resultado
- **Fluxo de aprovação do gerente** para preços bloqueados
- Cadastro de produtos e matérias-primas pela API
- Banco de dados real ou integração com o sistema da empresa (ERP)
