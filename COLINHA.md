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

## Como criar um teste unitário do service (Mockito)

Onde: `src/test/java/<mesmo pacote do service>/<Entidade>ServiceTest.java`
(espelha o pacote do `src/main`).

**Passo a passo:**

1. Classe **sem** `@SpringBootTest` — não sobe contexto Spring, roda rápido:
   ```java
   class CursoServiceTest {
   ```
2. Mocka a dependência (repository) e monta o service manualmente:
   ```java
   @Mock
   private CursoRepository cursoRepository;

   private CursoService cursoService;

   @BeforeEach
   void setUp() {
       MockitoAnnotations.openMocks(this);
       cursoService = new CursoService(cursoRepository);
   }
   ```
3. Um `@Test` por cenário, sempre no formato **arrange → act → assert**:
   ```java
   @Test
   void deletar_marcaCursoComoDeletado() {
       // arrange: prepara o mock
       Curso curso = new Curso("Java", "desc", 40, BigDecimal.TEN);
       curso.setId(1L);
       when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
       when(cursoRepository.save(any(Curso.class))).thenAnswer(inv -> inv.getArgument(0));

       // act: chama o método real
       cursoService.deletar(1L);

       // assert: confere o resultado E que o repositório foi chamado certo
       assertThat(curso.isDeletado()).isTrue();
       verify(cursoRepository).save(eq(curso));
   }
   ```
4. Caminho de **erro** usa `assertThatThrownBy`:
   ```java
   @Test
   void deletar_cursoInexistente_lancaExcecao() {
       when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

       assertThatThrownBy(() -> cursoService.deletar(99L))
               .isInstanceOf(CursoNotFoundException.class)
               .hasMessageContaining("99");

       verify(cursoRepository, never()).save(any());
   }
   ```

**Checklist pra bater 100%** (lembrando que o service tem gate obrigatório —
ver seção "Cobertura de testes" abaixo): todo método público testado, e
dentro de cada método, **todo `if`/`else` com os dois lados exercitados**
(ex: `listar` com filtro nulo/branco E com filtro preenchido são dois testes
diferentes, senão um dos ramos do `if` fica sem cobrir).

## Como criar um teste de integração do controller (Testcontainers)

Onde: `src/test/java/<pacote raiz>/<Entidade>ControllerIntegrationTest.java`.

**Passo a passo:**

1. Sobe o contexto Spring completo com MockMvc e liga o Testcontainers na classe:
   ```java
   @SpringBootTest
   @AutoConfigureMockMvc
   @Testcontainers
   class CursoControllerIntegrationTest {
   ```
2. Declara o container do Postgres — sobe uma vez antes da classe, derruba
   depois, o JUnit cuida do ciclo de vida:
   ```java
   @Container
   static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
           .withDatabaseName("cursos_test")
           .withUsername("test")
           .withPassword("test");
   ```
3. Aponta o datasource do Spring pro container (as portas do Testcontainers
   são aleatórias, por isso não dá pra hardcodar no `application.properties`):
   ```java
   @DynamicPropertySource
   static void configureProperties(DynamicPropertyRegistry registry) {
       registry.add("spring.datasource.url", postgres::getJdbcUrl);
       registry.add("spring.datasource.username", postgres::getUsername);
       registry.add("spring.datasource.password", postgres::getPassword);
   }
   ```
4. Injeta as ferramentas: `MockMvc` (simula requisição HTTP sem precisar
   subir servidor de verdade), o repository (popular dados antes do teste) e
   o `ObjectMapper` (serializar objeto Java → JSON do corpo da requisição):
   ```java
   @Autowired private MockMvc mockMvc;
   @Autowired private CursoRepository cursoRepository;
   @Autowired private ObjectMapper objectMapper;
   ```
5. Limpa o banco antes de cada teste — sem isso, um teste deixa dado sujo
   pro próximo e os testes ficam dependentes da ordem de execução:
   ```java
   @BeforeEach
   void limparBanco() {
       cursoRepository.deleteAll();
   }
   ```
6. Cada `@Test` faz a chamada HTTP de verdade via `mockMvc.perform(...)` e
   confere status + corpo com `jsonPath`:
   ```java
   @Test
   void deveCriarCurso() throws Exception {
       String body = objectMapper.writeValueAsString(
               new NovoCursoPayload("Spring Boot", "desc", 20, BigDecimal.valueOf(199.9)));

       mockMvc.perform(post("/cursos")
                       .contentType("application/json")
                       .content(body))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id", notNullValue()))
               .andExpect(jsonPath("$.nome", is("Spring Boot")));
   }
   ```
7. Pra popular dado de teste sem passar pela API, usa o repository direto no
   `arrange` (mais rápido que fazer um POST antes de cada teste):
   ```java
   @Test
   void deveListarApenasCursosNaoDeletados() throws Exception {
       cursoRepository.save(new Curso("Java Básico", "desc", 40, BigDecimal.TEN));
       Curso deletado = new Curso("Python Básico", "desc", 30, BigDecimal.ONE);
       deletado.setDeletado(true);
       cursoRepository.save(deletado);

       mockMvc.perform(get("/cursos"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)));
   }
   ```

**Matchers do `jsonPath` mais usados:**
```java
jsonPath("$", hasSize(2))              // array com N elementos
jsonPath("$.nome", is("Java"))         // campo == valor
jsonPath("$.id", notNullValue())       // campo existe e não é null
jsonPath("$[0].nome", is("Java"))      // primeiro elemento do array
```

**Status codes mais usados:** `status().isOk()` (200), `.isCreated()` (201),
`.isNoContent()` (204), `.isNotFound()` (404), `.isBadRequest()` (400).

**Regra prática**: uma rota do controller = pelo menos um teste aqui. Rota
nova no enunciado → teste de integração novo, não só o unitário do service
(o unitário não testa roteamento, serialização JSON nem status HTTP).

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

## Cobertura de testes — regra da disciplina

Tem **duas** exigências diferentes de cobertura rodando ao mesmo tempo, cada
uma numa camada:

### 1. `service` tem que ser 100% (gate local, no `pom.xml`)

```xml
<execution>
    <id>check-service-coverage</id>
    <phase>verify</phase>
    <goals><goal>check</goal></goals>
    <configuration>
        <rules>
            <rule>
                <element>CLASS</element>
                <includes>
                    <include>com.example.cursos.service.*</include>
                </includes>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>1.00</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</execution>
```

**Por que 100% só no service e não no projeto inteiro**: é onde mora a regra
de negócio — o que a prova está avaliando de fato. Controller é só fiação
(delega pro service), model é getter/setter, exception é boilerplate. Exigir
100% ali também seria trabalho sem ganho de qualidade real; no service, todo
`if`, todo caminho de erro, tem que ter teste — não sobra `if` sem cobrir.

**Como funciona por dentro**: o Jacoco instrumenta o bytecode em tempo de
teste (`prepare-agent`), gera o relatório (`report`, fase `test`) e o `check`
(fase `verify`) lê esse relatório e **quebra o build** (`mvn verify`/`mvn
clean install`/`mvn package` — qualquer goal que passe pela fase `verify`) se
alguma classe do pacote `service` tiver linha não coberta. Roda tanto local
quanto no CI, porque `tests.yml` chama `mvn clean install`.

**Na prática**: se você escrever um método novo no service e não tiver
nenhum teste exercitando ele (ou um `if`/`else` sem os dois lados testados),
o build quebra com uma mensagem tipo:

```
Rule violated for class CursoService: lines covered ratio is 0.92, but expected minimum is 1.00
```

A correção é sempre a mesma: achar a linha não coberta no relatório
(`tests/index.html` → clica na classe → linhas vermelhas) e escrever o teste
que passa por ali.

### 2. Projeto inteiro tem que ficar acima de 80% (gate no CI, comentado no PR)

Configurado em `.github/workflows/tests.yml`, via `madrapps/jacoco-report` —
lê o mesmo `tests/jacoco.xml` gerado pelo Jacoco, mas olha a cobertura
**geral** do projeto (não só o service) e a cobertura **dos arquivos que
mudaram no PR**. Falha o workflow se qualquer um dos dois ficar abaixo de
80%, e deixa um comentário no PR mostrando o número.

**Por que 80% e não 100% aqui**: é uma rede de segurança pro projeto todo
(controller, exception handler, etc.) — não é razoável exigir 100% em
código que é majoritariamente delegação, mas também não pode ficar
destestado. 80% pega o "esqueceu de testar isso" sem virar burocracia.

### O que conta como "testes de integração suficientes"

Não tem métrica automática pra isso (diferente da cobertura, que o Jacoco
mede em linha de código) — é critério de revisão. Regra prática: **uma rota
exposta pelo controller = pelo menos um teste de integração**, cobrindo:

- o caminho de sucesso (status esperado + corpo da resposta correto)
- pelo menos um caminho de erro relevante daquela rota (404 pra recurso
  inexistente, validação falhando, filtro vazio devolvendo tudo, etc.)

Se o enunciado pedir uma rota nova, ela **precisa** de teste de integração
próprio — não basta o unitário do service, porque o unitário não testa
serialização JSON, status HTTP, nem o roteamento (`@GetMapping`,
`@PathVariable`, etc.), só a lógica pura.

## CI/CD — GitHub Actions

Dois workflows, cada um com sua função (`.github/workflows/`), acionados por
gatilhos diferentes:

```
push numa branch de feature  →  nada dispara
abrir/atualizar PR pra main  →  tests.yml
merge do PR na main          →  deploy.yml
```

### `tests.yml` — passo a passo

Dispara em todo Pull Request pra `main` (`on: pull_request: branches: main`).

1. **`actions/checkout`** — clona o repo na máquina efêmera do GitHub Actions.
2. **`actions/setup-java`** — instala o JDK 25 (Temurin) e configura cache do
   `~/.m2` (deploys seguintes ficam mais rápidos, não rebaixa dependência).
3. **`mvn clean install`** — compila, roda os testes (unitários + integração
   com Testcontainers — o runner do GitHub já tem Docker disponível) e gera
   `tests/jacoco.xml`. Se algum teste falhar, ou o gate de 100% do service
   falhar (fase `verify`, que `install` inclui), o workflow para aqui.
4. **`madrapps/jacoco-report`** — lê `tests/jacoco.xml`, comenta a cobertura
   no PR (geral e dos arquivos que mudaram) e falha o workflow se qualquer
   uma ficar abaixo de 80%.

Resultado: o PR mostra um ✅ ou ❌ ao lado de cada commit, e um comentário
automático com a tabela de cobertura.

### `deploy.yml` — passo a passo

Dispara em todo push direto na `main` (`on: push: branches: main`) — o que
acontece automaticamente quando um PR é mergeado.

1. **checkout + setup-java** — igual ao `tests.yml`.
2. **`mvn -B package --file pom.xml`** — builda o jar (sem rodar teste de
   novo, já passou no `tests.yml` pra chegar até aqui).
3. **`docker/login-action`** — autentica no Docker Hub usando
   `DOCKERHUB_USERNAME`/`DOCKERHUB_TOKEN`.
4. **`docker/setup-buildx-action`** — prepara o builder de imagem (Buildx é o
   motor de build de imagem mais novo do Docker, com cache melhor).
5. **`docker/build-push-action`** — builda a imagem a partir do `Dockerfile`
   (multi-stage: primeiro estágio compila com Maven, segundo só copia o jar
   pra uma imagem `jre-alpine` bem menor) e já publica no Docker Hub, taggeada
   com o SHA do commit (`cursos-api-ci:<sha>` — cada deploy tem uma tag única,
   dá pra rastrear qual commit está rodando em produção).
6. **`appleboy/ssh-action`** — conecta na instância (`HOST_TEST` + `KEY_TEST`)
   e roda o script de deploy: cria a rede Docker se não existir, sobe o
   Postgres se ainda não tiver um rodando (com volume persistente), derruba o
   container antigo da app e sobe um novo com a imagem que acabou de subir
   pro Docker Hub.

Resultado: a instância AWS passa a rodar a versão nova, sem você tocar em
SSH manualmente — só faz `git push`.

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

## Factory + Strategy (processar algo de formas diferentes por tipo)

Usado quando existe um **enum de tipos** e cada tipo processa de um jeito
diferente — no exercício de referência (`pagamento`) era `TipoPagamento`
(`PIX`, `BOLETO`, `CREDITO`), cada um com sua própria regra de processamento.
O padrão evita um `if/else`/`switch` gigante espalhado pelo service: cada
regra vira uma classe, e uma Factory decide qual instanciar.

### As peças

```java
package com.example.cursos.processor;

public interface Processador {
    ResultadoDto processar(PedidoDto dto);
}
```

```java
package com.example.cursos.processor;

import org.springframework.stereotype.Component;

@Component
public class ProcessadorPix implements Processador {

    @Override
    public ResultadoDto processar(PedidoDto dto) {
        // regra específica do PIX
        return new ResultadoDto("aprovado", dto.getValor());
    }
}
```

Cada tipo ganha sua própria classe (`ProcessadorBoleto`, `ProcessadorCredito`,
...), todas `@Component` implementando a mesma interface.

### A Factory — decide qual `Processador` usar

```java
package com.example.cursos.processor;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProcessadorFactory {

    private final Map<TipoPedido, Processador> processadores;

    // Spring injeta a lista de todos os @Component que implementam Processador;
    // aqui a gente indexa por tipo pra buscar em O(1) depois
    public ProcessadorFactory(List<Processador> lista) {
        this.processadores = lista.stream()
                .collect(Collectors.toMap(this::tipoDoProcessador, p -> p));
    }

    public Processador getProcessador(TipoPedido tipo) {
        Processador p = processadores.get(tipo);
        if (p == null) {
            throw new IllegalArgumentException("Tipo não suportado: " + tipo);
        }
        return p;
    }

    private TipoPedido tipoDoProcessador(Processador p) {
        if (p instanceof ProcessadorPix) return TipoPedido.PIX;
        if (p instanceof ProcessadorBoleto) return TipoPedido.BOLETO;
        if (p instanceof ProcessadorCredito) return TipoPedido.CREDITO;
        throw new IllegalStateException("Processador sem tipo mapeado: " + p.getClass());
    }
}
```

**Alternativa mais simples** (sem `Map`, se só tiver 2-3 tipos e não precisar
de O(1)): a Factory recebe o `List<Processador>` e cada `Processador` expõe
um método `suporta(TipoPedido tipo)`; a factory usa `stream().filter(...)`
pra achar o certo. Menos código de setup, um pouco mais lento (irrelevante
pra volume de prova).

### Como plugar no service

```java
@Service
public class PedidoService {

    private final ProcessadorFactory processadorFactory;

    public PedidoService(ProcessadorFactory processadorFactory) {
        this.processadorFactory = processadorFactory;
    }

    public ResultadoDto processar(PedidoDto dto) {
        Processador processador = processadorFactory.getProcessador(dto.getTipo());
        return processador.processar(dto);
    }
}
```

O service **nunca sabe** qual implementação concreta está rodando — só pede
pra factory e chama a interface. Adicionar um tipo novo = criar uma classe
nova `@Component`, nada no service muda.

### Testando (Mockito)

```java
@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock private ProcessadorFactory processadorFactory;
    @Mock private Processador processadorPix;
    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(processadorFactory);
    }

    @Test
    void processar_delegaPraProcessadorCorreto() {
        PedidoDto dto = new PedidoDto(TipoPedido.PIX, BigDecimal.TEN);
        when(processadorFactory.getProcessador(TipoPedido.PIX)).thenReturn(processadorPix);
        when(processadorPix.processar(dto)).thenReturn(new ResultadoDto("aprovado", BigDecimal.TEN));

        ResultadoDto resultado = pedidoService.processar(dto);

        assertThat(resultado.getStatus()).isEqualTo("aprovado");
        verify(processadorPix).processar(dto);
    }
}
```

Pontos pra lembrar na prova:
- **Strategy** é a interface + implementações (o "como processar" varia).
- **Factory** é quem decide **qual** Strategy usar, baseado no tipo — o
  service não faz `if/else` nenhum, só chama `factory.getProcessador(tipo)`.
- Os processadores concretos são `@Component` — Spring os enxerga sozinho,
  não precisa registrar nada manualmente em lugar nenhum.
- É o mesmo truque de injetar `List<Interface>` que o Observer usa — Spring
  junta automaticamente todo mundo que implementa aquele contrato.

## Criando microsserviços (múltiplos Spring Boot separados)

Um microsserviço aqui é só **outro projeto Spring Boot igual a este**, com seu
próprio `pom.xml`, banco, `Dockerfile`, workflow e deploy — rodando em outro
container/porta, na mesma instância ou em outra. Não tem framework de service
mesh nem service discovery nessa disciplina; a comunicação é REST simples.

### Passo a passo pra criar um novo serviço

1. Copiar a estrutura deste projeto (`cursos-api`) pra uma pasta/repo novo,
   trocar `artifactId`, pacote e entidade (mesmo processo da seção
   "O que mudar pra usar isso numa prova nova").
2. Cada serviço tem **seu próprio banco** (`POSTGRES_DB` diferente) — nunca
   compartilhar tabelas entre serviços; se um precisa de dado do outro, pede
   via HTTP.
3. Dar um nome de container único (ex: `pagamento-api`) e conectar na mesma
   rede Docker (`docker network create` / `--network cursos-net`) pra um
   serviço enxergar o outro pelo nome, como já fazemos com `pg-cursos`.
4. Cada serviço mantém seu próprio `deploy.yml` com uma porta de host distinta
   (`8081`, `8082`, ...) — só a porta interna do container continua `8080`.

### Um serviço chamando o outro (client HTTP)

```java
package com.example.cursos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PagamentoClient {

    private final RestClient restClient;

    public PagamentoClient(@Value("${pagamento.service.url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    public RespostaPagamentoDto processarPagamento(PagamentoDto dto) {
        return restClient.post()
                .uri("/pagamentos")
                .body(dto)
                .retrieve()
                .body(RespostaPagamentoDto.class);
    }
}
```

```properties
# application.properties do serviço que chama
pagamento.service.url=http://pagamento-api:8080
```

`pagamento-api` aqui é o **nome do container** do outro serviço — funciona
porque os dois estão na mesma rede Docker, igual ao `DB_HOST=pg-cursos`.
Localhost não serve entre containers diferentes.

### Coisas que a prova pode pedir nesse tema

- **Resiliência básica**: envolver a chamada em `try/catch` e devolver um erro
  de negócio (`RestClientException` vira 4xx/5xx tratado no
  `@RestControllerAdvice`), já que se o outro serviço cair a chamada explode.
- **DTO próprio pra comunicação**: não reusar a entidade JPA do outro serviço
  — criar um DTO local só com os campos que você precisa (desacopla os dois
  serviços; se o schema do outro mudar, só quebra se o campo que você usa
  mudar).
- **docker-compose com os dois serviços** (pra rodar tudo local de uma vez):

```yaml
services:
  cursos-api:
    build: ./cursos-api
    ports: ["8080:8080"]
    networks: [app-net]

  pagamento-api:
    build: ./pagamento-api
    ports: ["8082:8080"]
    networks: [app-net]

networks:
  app-net:
```

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
