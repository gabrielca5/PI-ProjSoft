# Comandos rápidos — referência

Cola de comandos prontos pra copiar/colar. Pra entender o "porquê" de cada
coisa (arquitetura, padrões, o que mudar em prova nova), ver [`COLINHA.md`](COLINHA.md).

## Git

```bash
git status
git add .
git commit -m "feat: mensagem"
git push origin main

# fluxo de PR
git checkout -b feat/nome-da-rota
git add .
git commit -m "feat: descricao"
git push -u origin feat/nome-da-rota
gh pr create --title "feat: titulo" --body "descricao" --base main
gh pr view --web          # abrir o PR no navegador
gh pr checks               # ver status do CI do PR atual

# desfazer commit sem perder mudanças (antes do push)
git reset --soft HEAD~1

# corrigir mensagem do último commit (antes do push)
git commit --amend -m "nova mensagem"

# reescrever commit já pushado (cuidado: reescreve histórico remoto)
git commit --amend -m "nova mensagem"
git push --force origin main
```

## Maven (wrapper)

```bash
./mvnw spring-boot:run                # roda local
./mvnw test                           # roda todos os testes (precisa Docker p/ Testcontainers)
./mvnw test -Dtest=NomeDaClasse       # só uma classe
./mvnw test -Dtest=Classe#metodo      # só um método
./mvnw clean install                  # build + testes + jacoco (tests/jacoco.xml)
./mvnw clean verify                   # igual acima + quebra se o service não estiver 100% coberto
./mvnw clean package -DskipTests      # só o jar, sem rodar teste
./mvnw dependency:tree                # ver árvore de dependências (debug de conflito)
```

## Docker — local

### Postgres avulso (sem compose)

```bash
docker run -d --name pg-cursos -e POSTGRES_DB=cursos -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres
docker stop pg-cursos                 # para sem apagar (dados ficam)
docker start pg-cursos                # religa
docker rm -f pg-cursos                # remove de vez (perde os dados se não tiver volume)
```

### Build e run manual da imagem da app (sem compose)

```bash
docker build -t cursos-api .                    # builda a imagem a partir do Dockerfile
docker images                                    # confirma que a imagem foi criada

docker network create cursos-net                 # cria a rede (uma vez só)
docker network connect cursos-net pg-cursos       # conecta um container já existente numa rede

docker run -d --name cursos-api --network cursos-net -p 8080:8080 \
  -e DB_HOST=pg-cursos -e DB_USER=postgres -e DB_PASSWORD=postgres \
  cursos-api

docker tag cursos-api meuusuario/cursos-api:v1    # renomeia/marca pra push
docker push meuusuario/cursos-api:v1              # sobe pro Docker Hub (precisa docker login antes)
```

### docker-compose

```bash
DB_USER=postgres DB_PASSWORD=postgres docker compose up --build   # builda e sobe tudo
docker compose up -d                  # sobe em background (detached)
docker compose down                   # derruba tudo (containers, mantém volumes)
docker compose down -v                # derruba tudo E apaga os volumes (perde dados)
docker compose ps                     # status dos serviços do compose
docker compose logs -f cursos-api-app # acompanha log de um serviço
docker compose build                  # só rebuilda a imagem, sem subir
docker compose restart cursos-api-app # reinicia um serviço só
```

### Inspeção e debug

```bash
docker ps                             # containers rodando
docker ps -a                          # todos, incluindo parados
docker logs <nome>                    # log completo do container
docker logs -f <nome>                 # acompanha em tempo real (like tail -f)
docker logs --since 10m <nome>        # só os últimos 10 minutos
docker exec -it <nome> sh             # abre um shell dentro do container rodando
docker inspect <nome>                 # detalhes completos (JSON) do container
docker stats                          # uso de CPU/memória em tempo real
```

### Limpeza

```bash
docker rm -f <nome>                   # remove um container (força, mesmo rodando)
docker rmi <imagem>                   # remove uma imagem
docker volume ls                      # lista volumes (dados persistentes)
docker volume rm <nome>               # apaga um volume (perde os dados!)
docker network ls                     # lista redes
docker network rm <nome>              # remove uma rede (não pode ter container conectado)
docker container prune                # remove TODOS os containers parados
docker system prune -a                # limpa containers/imagens/redes não usados (cuidado)
```

## curl — testar rotas

```bash
curl http://localhost:8080/cursos
curl "http://localhost:8080/cursos?nome=Java"

curl -X POST http://localhost:8080/cursos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Spring Boot","descricao":"curso","cargaHoraria":20,"preco":99.9}'

curl -X DELETE http://localhost:8080/cursos/1

curl -i http://localhost:8080/cursos          # -i mostra headers/status também
curl -w "\n%{http_code}\n" http://localhost:8080/cursos  # só o status code no final
```

## SSH / instância AWS

```bash
ssh -i projsoft26b.pem ubuntu@3.236.181.241

# na instância:
docker ps -a
docker logs -f cursos-api
docker logs --tail 100 pg-cursos
docker exec -it pg-cursos psql -U <DB_USER> -d cursos
docker restart cursos-api
docker network inspect cursos-net
docker stats                          # uso de CPU/memória dos containers
free -h                                # memória livre na instância
df -h                                  # espaço em disco
```

## GitHub Secrets necessários

`Settings > Secrets and variables > Actions`:

| Secret | Valor |
|---|---|
| `DOCKERHUB_USERNAME` | usuário do Docker Hub |
| `DOCKERHUB_TOKEN` | access token (não a senha) |
| `HOST_TEST` | IP público da instância |
| `KEY_TEST` | conteúdo completo do `.pem` |
| `DB_USER` | usuário do Postgres |
| `DB_PASSWORD` | senha do Postgres |

## GitHub CLI (`gh`)

```bash
gh auth login                          # autenticar
gh repo view --web                     # abrir o repo no navegador
gh run list                            # últimas execuções de workflow
gh run watch                           # acompanhar a execução atual em tempo real
gh run view --log-failed               # ver só o log da etapa que falhou
gh secret list                         # ver quais secrets existem (não mostra valor)
gh secret set DB_PASSWORD              # cadastrar/atualizar um secret (pede o valor)
```

## Spring Data JPA — keywords de query method

```java
findByNome(String nome)                          // = 
findByNomeContaining(String x)                   // LIKE %x%
findByNomeStartingWith(String x)                 // LIKE x%
findByNomeEndingWith(String x)                   // LIKE %x
findByNomeIgnoreCase(String x)                   // case-insensitive
findByPrecoGreaterThan(BigDecimal x)              // >
findByPrecoLessThanEqual(BigDecimal x)            // <=
findByPrecoBetween(BigDecimal min, BigDecimal max)
findByDeletadoFalse()                             // = false
findByNomeOrderByPrecoDesc(String nome)           // ORDER BY
findByNomeAndCargaHoraria(String n, Integer c)    // AND
findByNomeOrDescricao(String n, String d)         // OR
existsByNome(String nome)                         // boolean
countByDeletadoFalse()                            // long
deleteByNome(String nome)                         // delete direto (evitar — preferir deleção lógica)
```

## Checklist rápido antes de entregar a prova

- [ ] `./mvnw clean install` passa local (testes + jacoco)
- [ ] Rotas testadas manualmente via curl/Postman
- [ ] `.gitignore` cobre `.env`, `target/`, `.idea/`
- [ ] Nenhuma credencial hardcoded em `application.properties` (tudo `${VAR}`)
- [ ] PR aberto dispara `tests.yml` e passa
- [ ] Push na `main` dispara `deploy.yml` e a app responde no IP da instância
- [ ] Secrets do GitHub cadastrados (ver tabela acima)
