# Colinha — API de cursos (Spring Boot)

Projeto completo funcionando neste diretório. Na prova: copiar a estrutura,
trocar nome de domínio/entidade e ajustar rotas conforme o enunciado.

## Estrutura mental (ordem para escrever do zero)

1. `pom.xml` — starters: web, data-jpa, validation, postgresql (runtime), h2 (test), starter-test
2. `model/Curso.java` — entidade JPA, campo `deletado` (boolean, default false)
3. `repository/CursoRepository.java` — extends `JpaRepository`, query methods derivados
4. `service/CursoService.java` — regra de negócio, é o que precisa de 100% de cobertura
5. `controller/CursoController.java` — REST, injeta o service
6. `exception/` — exceção customizada + `@RestControllerAdvice`
7. `application.properties` — tudo via `${VAR}` de ambiente, nunca hardcoded
8. Testes: `*ServiceTest` (Mockito, unitário) + `*IntegrationTest` (`@SpringBootTest` + `MockMvc`)
9. `.github/workflows/ci-cd.yml` — job `test` (sempre) + job `deploy` (só push em `main`)

## Filtro "startWith"

Spring Data JPA já resolve por nome de método, sem escrever JPQL:

```java
List<Curso> findByDeletadoFalseAndNomeStartingIgnoreCase(String nome);
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
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT:5432}/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

Rodar local exportando as env vars antes (PowerShell):

```powershell
$env:DB_HOST="localhost"; $env:DB_PORT="5432"; $env:DB_NAME="cursos"
$env:DB_USER="postgres"; $env:DB_PASSWORD="postgres"
mvn spring-boot:run
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

## PostgreSQL na instância AWS (setup rápido)

```bash
sudo apt update && sudo apt install -y postgresql
sudo -u postgres psql -c "CREATE DATABASE cursos;"
sudo -u postgres psql -c "CREATE USER cursos_user WITH PASSWORD 'senha';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE cursos TO cursos_user;"
```

Liberar conexão remota se a app rodar fora da instância do banco: editar
`postgresql.conf` (`listen_addresses = '*'`) e `pg_hba.conf` (adicionar
`host cursos cursos_user <IP_ORIGEM>/32 md5`), depois `sudo systemctl restart postgresql`.
Liberar a porta 5432 no Security Group da AWS **apenas** para o IP necessário.

## Deploy do jar no servidor (systemd)

Arquivos de referência em `deploy/`:
- `cursos-api.service` — unit do systemd, lê `EnvironmentFile=/opt/cursos-api/.env`
- `env.example` — modelo do `.env` que fica **só no servidor**, nunca no git

```bash
sudo mkdir -p /opt/cursos-api
sudo cp deploy/cursos-api.service /etc/systemd/system/cursos-api.service
sudo cp deploy/env.example /opt/cursos-api/.env   # depois editar com valores reais
sudo chmod 600 /opt/cursos-api/.env
sudo systemctl daemon-reload
sudo systemctl enable --now cursos-api
```

## GitHub Actions — secrets a cadastrar no repositório

`Settings > Secrets and variables > Actions > New repository secret`:

- `AWS_HOST` — IP público da instância
- `AWS_USER` — usuário SSH (ex: `ubuntu`)
- `AWS_SSH_KEY` — conteúdo da chave privada `.pem`

O pipeline (`.github/workflows/ci-cd.yml`) tem 2 jobs:
- `test`: roda em todo push/PR — `mvn -B verify` (testes + gate de 100% de cobertura no service via Jacoco)
- `deploy`: só roda em push na `main`, depois do `test` passar — builda o jar, copia via SCP e reinicia o serviço via SSH

## Fluxo do "criar uma rota via Pull Request"

```bash
git init                       # se ainda não for repo
git checkout -b feat/delete-curso
# implementar/ajustar a rota (ex: DELETE /cursos/{id})
git add .
git commit -m "feat: rota DELETE /cursos/{id} com delecao logica"
git push -u origin feat/delete-curso
gh pr create --title "feat: DELETE /cursos/{id}" --body "Implementa delecao logica" --base main
```

Abrir o PR já dispara o job `test` do workflow (trigger `pull_request`).
Só depois do merge em `main` é que o job `deploy` roda.

## Comandos Maven que vou precisar

```bash
mvn test              # só roda os testes
mvn verify             # testes + checagem de cobertura (jacoco:check)
mvn clean package      # gera o jar em target/
mvn spring-boot:run    # roda local
```
