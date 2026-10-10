# Louro

App Android nativo (Kotlin + Compose) para aprender inglês e italiano. Totalmente offline: o conteúdo vai embarcado e o progresso fica só no aparelho.

## Rodando

Precisa do Android SDK (plataforma 37) e de um JDK 17 ou 21.

```
./gradlew :app:installDebug      # instala no aparelho ou emulador conectado
./gradlew :app:testDebugUnitTest # testes, incluindo a validação do conteúdo
```

## Conteúdo

O app não sabe nada sobre nenhum idioma. Ele varre `app/src/main/assets/content/` e desenha uma bolinha por nível encontrado:

```
content/
  italiano/
    language.json          nome, locale da voz e bandeira
    level-0000/
      level.json           tipo, tema, glossário e exercícios
      ...                  áudios e imagens do nível
    level-0001/
```

Para adicionar um nível, crie a próxima pasta `level-NNNN` com um `level.json`. O formato está em [`docs/level.schema.json`](docs/level.schema.json).

Cada exercício declara o que ensina em duas listas: `new` (o que apresenta) e `review` (o que revisa). A repetição espaçada usa só essas etiquetas, nunca o texto do exercício.

### Gerando níveis com uma LLM

Dê ao modelo duas coisas: o esquema e o índice do que já foi ensinado.

```
tools/content_index.py italiano
```

O índice lista cada item já apresentado, com significado e nível de origem, e o nome da próxima pasta. Os testes recusam um nível que revise um item nunca apresentado, que apresente um item sem entrada no `glossary` ou que quebre a numeração.

## O que já existe

- Caminho de bolinhas, um nível por pasta, sem limite.
- Cinco tipos de nível: vocabulário, escuta e fala, escrita, leitura com palavras tocáveis e gramática.
- Nível só fecha com tudo certo; o que foi errado volta para o fim da fila. Sem cronômetro.
- Pontos, com bônus por acertar de primeira, e ofensiva de dias seguidos.
- Repetição espaçada invisível: itens vencidos voltam misturados aos exercícios dos níveis seguintes.
- Teste de nivelamento por vocabulário, que pula os níveis já dominados.
- Lembrete diário da ofensiva, às 19h, só se o dia ainda estiver em aberto.

## O que falta

- Reconhecimento de fala (Parakeet) para avaliar a pronúncia; hoje o nível de fala é autoavaliado.
- LLM local opcional: modo conversa, dica nos exercícios e avaliação do nível de escrita.
- Áudios e imagens gravados; por enquanto a voz é a do próprio aparelho.
- Mascote e ícone próprios.
