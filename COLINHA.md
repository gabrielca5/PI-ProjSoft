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
./mvnw test                          # só roda os testes (precisa do Docker rodando p/ Testcontainers)
./mvnw test -Dtest=CursoServiceTest  # só uma classe de teste
./mvnw test -Dtest=CursoServiceTest#deletar_cursoInexistente_lancaExcecao  # só um método
./mvnw clean install                 # testes + build + relatório jacoco (tests/jacoco.xml)
./mvnw clean package -DskipTests     # gera o jar em target/ sem rodar teste (deploy usa isso)
./mvnw spring-boot:run               # roda local (precisa das env vars DB_* setadas)
```

Ver cobertura local sem esperar o CI: `./mvnw clean install` e abrir `tests/index.html` no navegador.

## O que mudar pra usar isso numa prova nova (outro domínio)

1. **Renomear o pacote/entidade**: trocar `com.example.cursos` → `com.example.<dominio>`
   em todos os arquivos (`src/main` e `src/test`), e `Curso` → `<Entidade>`.
2. **`pom.xml`**: `artifactId` (ex: `cursos-api` → `<dominio>-api`).
3. **`application.properties`**: nome do banco na URL
   (`jdbc:postgresql://...:5432/cursos` → `.../<dominio>`).
4. **`deploy.yml`**: 3 lugares que citam `cursos`:
   - `POSTGRES_DB=cursos` → `POSTGRES_DB=<dominio>`
   - `--name cursos-api` → `--name <dominio>-api`
   - tag da imagem `cursos-api-ci` → `<dominio>-api-ci`
   - se o nome do container mudar, o `--name pg-cursos` também pode mudar
     (mas não precisa — pode reaproveitar o mesmo Postgres pra bancos diferentes
     desde que troque só o `POSTGRES_DB`/nome do banco na URL)
5. **Porta no host** (`-p 8081:8080` em `deploy.yml`): só mexer se a instância já
   tiver outra coisa usando aquela porta (ver checklist de troubleshooting abaixo).
6. **`docker-compose.yml`**: `container_name` e nomes de env var, se quiser manter
   coerência com o novo domínio — não é obrigatório, só cosmético.
7. **Secrets do GitHub continuam os mesmos** (`DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`,
   `HOST_TEST`, `KEY_TEST`, `DB_USER`, `DB_PASSWORD`) — não precisa recriar.

## Observer Pattern (notificações de evento)

Usado quando uma ação do service precisa "avisar" outras partes do sistema
(auditoria, e-mail, log) sem o service conhecer os detalhes de cada uma —
desacopla quem gera o evento de quem reage a ele.

### As peças

```java
package com.example.cursos.observer;

public interface CursoObserver {
    void onCursoCriado(Curso curso);
    void onCursoDeletado(Curso curso);
}
```

```java
package com.example.cursos.observer;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CursoObservable {

    private final List<CursoObserver> observers;

    // Spring injeta automaticamente TODOS os beans que implementam CursoObserver
    public CursoObservable(List<CursoObserver> observers) {
        this.observers = observers;
    }

    public void notificarCriado(Curso curso) {
        observers.forEach(o -> o.onCursoCriado(curso));
    }

    public void notificarDeletado(Curso curso) {
        observers.forEach(o -> o.onCursoDeletado(curso));
    }
}
```

```java
package com.example.cursos.observer;

import org.springframework.stereotype.Component;

@Component
public class AuditLoggerObserver implements CursoObserver {

    @Override
    public void onCursoCriado(Curso curso) {
        System.out.println("[AUDIT] curso criado: " + curso.getNome());
    }

    @Override
    public void onCursoDeletado(Curso curso) {
        System.out.println("[AUDIT] curso deletado: " + curso.getId());
    }
}
```

Outro observer (ex: `EmailNotifierObserver`) implementa a mesma interface —
Spring injeta os dois na lista do `CursoObservable` automaticamente, sem
precisar registrar nada manualmente.

### Como plugar no service

```java
@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final CursoObservable cursoObservable;

    public CursoService(CursoRepository cursoRepository, CursoObservable cursoObservable) {
        this.cursoRepository = cursoRepository;
        this.cursoObservable = cursoObservable;
    }

    public Curso criar(CursoRequestDTO dto) {
        Curso salvo = cursoRepository.save(new Curso(dto.getNome(), dto.getDescricao(), dto.getCargaHoraria(), dto.getPreco()));
        cursoObservable.notificarCriado(salvo);
        return salvo;
    }

    public void deletar(Long id) {
        Curso curso = cursoRepository.findById(id).orElseThrow(() -> new CursoNotFoundException(id));
        curso.setDeletado(true);
        cursoRepository.save(curso);
        cursoObservable.notificarDeletado(curso);
    }
}
```

### Testando (Mockito)

```java
@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

    @Mock private CursoRepository cursoRepository;
    @Mock private CursoObservable cursoObservable;
    private CursoService cursoService;

    @BeforeEach
    void setUp() {
        cursoService = new CursoService(cursoRepository, cursoObservable);
    }

    @Test
    void criar_notificaObservers() {
        // ... arrange igual aos outros testes

        cursoService.criar(dto);

        verify(cursoObservable).notificarCriado(any(Curso.class));
    }
}
```

Pontos pra lembrar na prova:
- A **interface** define o contrato (`onXxx`), os **observers concretos**
  implementam e viram `@Component` (Spring detecta sozinho).
- O **observable** só conhece a interface, nunca as implementações — é isso
  que desacopla.
- Injetar `List<Interface>` no construtor é o jeito idiomático de "registrar"
  vários observers no Spring, sem precisar de um método `subscribe()` manual.
- O `service` chama `observable.notificarX(...)` depois da ação principal
  (save/delete) — nunca antes, senão notifica algo que pode falhar em seguida.

## Debug na instância AWS (SSH)

```bash
ssh -i projsoft26b.pem ubuntu@3.236.181.241

docker ps -a                      # ver todos os containers (rodando e parados)
docker logs -f cursos-api         # acompanhar log da aplicação em tempo real
docker logs --tail 100 pg-cursos  # últimas linhas do log do Postgres
docker exec -it pg-cursos psql -U <DB_USER> -d cursos   # abrir psql dentro do container
docker restart cursos-api         # reiniciar sem esperar novo deploy
docker network inspect cursos-net # ver quais containers estão na rede
```

### Checklist quando o deploy falha

- **`port is already allocated`** → outra coisa na instância já usa aquela porta
  (`docker ps -a` pra achar quem). Troca o mapeamento de porta em `deploy.yml`
  (`-p <nova>:8080`) e libera a porta nova no Security Group da AWS.
- **`password authentication failed`** → `DB_USER`/`DB_PASSWORD` errados ou
  o container `pg-cursos` já existia com credenciais antigas antes de trocar os
  secrets (o Postgres só lê `POSTGRES_USER`/`PASSWORD` na criação do container —
  se já existe, precisa `docker rm -f pg-cursos` + apagar o volume `pgdata` pra
  recriar com as credenciais novas: `docker volume rm pgdata`).
- **`Unable to find image` / erro de login no Docker Hub** → conferir
  `DOCKERHUB_USERNAME`/`DOCKERHUB_TOKEN` (token, não senha) nos secrets.
- **SSH falha (`Permission denied` ou timeout)** → conferir `HOST_TEST` (IP atual
  da instância, pode mudar se ela foi reiniciada) e `KEY_TEST` (conteúdo completo
  do `.pem`, incluindo as linhas `BEGIN`/`END`).
- **App sobe mas não responde de fora** → porta não liberada no Security Group
  da instância (Inbound rules → TCP → porta usada → 0.0.0.0/0).
