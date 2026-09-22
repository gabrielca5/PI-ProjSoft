# Colinha — API de cursos (Spring Boot)

Projeto completo funcionando neste diretório, seguindo o padrão oficial da
disciplina (baseado no projeto de referência `pagamento`). Na prova: copiar
a estrutura, trocar nome de domínio/entidade e ajustar rotas conforme o
enunciado.

## Estrutura mental (ordem para escrever do zero)

1. `pom.xml` — parent `spring-boot-starter-parent:4.1.0`, Java 25, starters:
   `data-jpa`, `webmvc`, `validation`, `postgresql` (runtime), `spring-boot-starter-test`,
   `spring-boot-starter-webmvc-test`, `testcontainers`/`testcontainers-postgresql`/`testcontainers-junit-jupiter`
2. `model/Curso.java` — entidade JPA, campo `deletado` (boolean, default false)
3. `repository/CursoRepository.java` — extends `JpaRepository`, query methods derivados
4. `service/CursoService.java` — regra de negócio, alvo de cobertura de testes
5. `controller/CursoController.java` — REST, injeta o service
6. `exception/` — exceção customizada + `@RestControllerAdvice`
7. `application.properties` — tudo via `${VAR}` de ambiente, nunca hardcoded
8. Testes: `*ServiceTest` (Mockito, unitário) + `*ControllerIntegrationTest` (`@SpringBootTest` + `@Testcontainers` + `PostgreSQLContainer`)
9. `.github/workflows/tests.yml` — roda em todo PR, `mvn clean install` + gate de cobertura via `madrapps/jacoco-report` (80%)
10. `.github/workflows/deploy.yml` — só em push na `main`: builda, publica imagem no Docker Hub e reinicia o container via SSH

## Filtro "startWith"

Spring Data JPA já resolve por nome de método, sem escrever JPQL:

```java
List<Curso> findByDeletadoFalseAndNomeStartingWithIgnoreCase(String nome);
```

Outras variações úteis para decorar:
- `Containing` → `LIKE %x%`
- `EndingWith` → `LIKE %x`
- `StartingWith` → `LIKE x%` (o que este exercício pede)
- Tirar `IgnoreCase` se o enunciado quiser case-sensitive

## Deleção lógica

Nunca usar `deleteById`. Sempre: buscar, marcar `deletado = true`, `save`.
O `GET` e qualquer query de listagem devem sempre filtrar `deletadoFalse`.

## application.properties (variáveis de ambiente / secrets)

```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:5432/cursos
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

## Rodando local

### Banco (Docker)

```bash
docker run -d --name pg-cursos -e POSTGRES_DB=cursos -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres
```

### Aplicação (Maven wrapper)

```powershell
$env:DB_HOST="localhost"; $env:DB_USER="postgres"; $env:DB_PASSWORD="postgres"
./mvnw spring-boot:run
```

### Tudo via docker-compose (app containerizada)

```bash
DB_USER=postgres DB_PASSWORD=postgres docker compose up --build
```

## Testar as rotas (curl)

```bash
curl http://localhost:8080/cursos
curl "http://localhost:8080/cursos?nome=Java"

curl -X POST http://localhost:8080/cursos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Spring Boot","descricao":"curso","cargaHoraria":20,"preco":99.9}'

curl -X DELETE http://localhost:8080/cursos/1
```

## CI/CD — GitHub Actions

Dois workflows, cada um com sua função (`.github/workflows/`):

- **`tests.yml`** — dispara em todo Pull Request pra `main`. Roda `mvn clean install`
  e usa `madrapps/jacoco-report` pra comentar a cobertura no PR e falhar o build
  se ficar abaixo de 80%.
- **`deploy.yml`** — dispara em todo push na `main`. Builda o jar, builda a imagem
  Docker (`Dockerfile` multi-stage), publica no Docker Hub e reinicia o container
  na instância via SSH (`docker stop` + `docker run` da imagem nova).

### Secrets a cadastrar no repositório

`Settings > Secrets and variables > Actions > New repository secret`:

- `DOCKERHUB_USERNAME` — usuário do Docker Hub
- `DOCKERHUB_TOKEN` — access token do Docker Hub (não a senha)
- `HOST_TEST` — IP público da instância onde a app roda
- `KEY_TEST` — conteúdo da chave privada SSH (`.pem`)
- `DB_USER`, `DB_PASSWORD` — credenciais do Postgres (usadas tanto para subir o
  container do banco quanto para a app se conectar nele)

### Rede Docker na instância

O `deploy.yml` cria (se ainda não existir) uma rede `cursos-net` e sobe o
Postgres nela como container `pg-cursos` — só na primeira vez, com volume
nomeado (`pgdata`) pra persistir os dados entre deploys. A cada push na
`main`, só o container `cursos-api` é recriado, conectado na mesma rede,
usando `DB_HOST=pg-cursos` (nome do container resolve como hostname dentro
da rede — não precisa de IP nem de `localhost`).

## Fluxo do "criar uma rota via Pull Request"

```bash
git checkout -b feat/delete-curso
# implementar/ajustar a rota (ex: DELETE /cursos/{id})
git add .
git commit -m "feat: rota DELETE /cursos/{id} com delecao logica"
git push -u origin feat/delete-curso
gh pr create --title "feat: DELETE /cursos/{id}" --body "Implementa delecao logica" --base main
```

Abrir o PR já dispara `tests.yml`. Só depois do merge em `main` é que `deploy.yml` roda.

## Comandos Maven (wrapper) que vou precisar

```bash
./mvnw test                # só roda os testes
./mvnw clean install       # testes + build + relatório jacoco (tests/jacoco.xml)
./mvnw clean package        # gera o jar em target/
./mvnw spring-boot:run      # roda local
```
