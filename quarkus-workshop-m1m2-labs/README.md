# Prerequiste

## Create PostgreSQL DB
  * Using Topology to add new PostgreSQL services
    * <img width="792" alt="postgresql" src="https://user-images.githubusercontent.com/58408898/169682524-2b5a81df-21ff-467d-8cb1-7de1cd375ea3.png"> 

## Create AMQ Streams cluster
  * Install AMQ Streams Operator
  
  * Using Topology to create a new kafka cluster
    * <img width="655" alt="kafka-cluster" src="https://user-images.githubusercontent.com/58408898/169682544-3b112d69-4eba-498a-86fb-13e9f87d000e.png">
  
  * Using Topology to create a new kafka topic
    * <img width="637" alt="kafka-topic" src="https://user-images.githubusercontent.com/58408898/169682550-2b1de5b6-bdc0-46c4-ac21-15faf12f3d22.png"> 

## Create Prometheus
  * Create prometheus configmap from prometheus.yml
    ```
    oc create configmap prom --from-file=prometheus.yml=$HOME/quarkus-lab/quarkus-workshop-m1m2-labs/src/main/kubernetes/prometheus.yml
    ```

  * Using Topology to add new prometheus via container image
    * <img width="454" alt="prometheus" src="https://user-images.githubusercontent.com/58408898/169682560-9d114d91-8242-48f6-8a8a-e3a34ae95dbb.png"> 
  
  * Mount configmap
    ```
    oc set volume deployment/prometheus --add -t configmap --configmap-name=prom -m /etc/prometheus/prometheus.yml --sub-path=prometheus.yml
    ```
  
  * Rollout prometheus services
    ```
    oc rollout status -w deployment/prometheus
    ```
  
  * Open prometheus UI
    * <img width="959" alt="prometheus-ui" src="https://user-images.githubusercontent.com/58408898/169682573-4327c4d6-6eaa-4a04-b4af-a8169e755952.png">

## Create Grafana
  * Using Topology to add new grafana via container image
    * <img width="451" alt="grafana" src="https://user-images.githubusercontent.com/58408898/169682590-e5afdd13-65b7-4fce-aa4f-2b2b898daf65.png"> 
  
  * Add datasource to grafana
    * <img width="757" alt="grafana-add-datasource" src="https://user-images.githubusercontent.com/58408898/169682599-41dbcfc8-a360-479a-a80c-a1c4f7768585.png">
    * <img width="388" alt="grafana-add-datasource-proemtheus" src="https://user-images.githubusercontent.com/58408898/169682604-f0652e94-5b06-4db2-9d1d-7e9437b46c3b.png">

## Create Jaeger
  * Install Red Hat Distributed Tracing Operator
  
  * Using Topology to add new Jaeger service
    * <img width="608" alt="jaeger" src="https://user-images.githubusercontent.com/58408898/169682616-6ec97772-09f3-4f22-bc96-ad5cd157d034.png">

## Create Keycloak
  * Install Red Hat SSO Operator
  
  * Create Keycloak using CR
    * <img width="454" alt="SSO-keycloak" src="https://user-images.githubusercontent.com/58408898/169682639-851a3d41-3585-4446-a146-a1ff66afee9f.png"> 
  
  * Login and create new realm (username/password is in the secret)
    * <img width="158" alt="SSO-realm" src="https://user-images.githubusercontent.com/58408898/169682643-d80ae1ec-f6cc-4160-b8af-64fdbf1bb5a2.png"> 
  
  * Create client
    * <img width="691" alt="SSO-client" src="https://user-images.githubusercontent.com/58408898/169682655-ff62f699-bd7a-4908-9283-e07fdd5d5c14.png">

# Create and Build a Basic Quarkus Microservices App

## Build a Java application as an Linux native binary
  * Use maven CLI to start dev mode
    ```
    mvn compile quarkus:dev
    ```
  
  * Test App is running
    ```
    curl http://localhost:8080/hello/lastletter/redhat ; echo
    ```
  
  * Open GreetingResource.java and switch hello method to demonstrate live coding
    ```
    curl http://localhost:8080/hello
    ```

  * Add native build in pom.xml
    ```xml
    <profiles>
      <profile>
        <id>native</id>
        <activation>
        ...
        </activation>
        <build>
        ...
        <properties>
          <quarkus.package.type>native</quarkus.package.type>
        </properties>
      </profile>
    </profiles>
    ```

  * build native binary
    ```
    quarkus build --native -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=podman -DskipTests
    ```

## List and add extensions
  * List quarkus extensions
    ```  
    mvn quarkus:list-extensions -f $HOME/quarkus-lab/quarkus-workshop-m1m2-labs
    ```

  * Check dependecies in dev UI
    ```  
    curl http://localhost:8080/q/dev
    ```

## Package an application for deployment to OpenShift as a native image
  * Add OpenShift extension
    ```
    mvn quarkus:add-extension -Dextensions="openshift"
    ```

  * Add OpenShift deployment config to application.properties
    ```
    %prod.quarkus.kubernetes-client.trust-certs=true
    %prod.quarkus.kubernetes.deploy=true
    %prod.quarkus.kubernetes.deployment-target=openshift
    %prod.quarkus.openshift.build-strategy=docker
    %prod.quarkus.openshift.expose=true
    ```    
    [quarkus opneshift reference](https://quarkus.io/guides/deploying-to-openshift)

  * build native binary and Deploy to OpenShift
    ```
    mvn clean package -Pnative -DskipTests -Dquarkus.package.uber-jar=false -Dquarkus.native.container-runtime=podman -f $HOME/quarkus-lab/quarkus-workshop-m1m2-labs
    ```

# Create a RESTful API services

## Add an externalized configuration to a Quarkus application using MicroProfile Config
  * Add resteasy reactive jackson extension
    ```
    mvn quarkus:add-extension -Dextensions="resteasy-reactive-jackson" -f $HOME/quarkus-lab/quarkus-workshop-m1m2-labs
    ```
    [smallrye rest client reactive reference](https://quarkus.io/guides/rest-client-reactive)
      
  * Inject greeting service
    ```java   
    @Inject
    GreetingService service;
    ```

  * Add Greeting Endpoint
    ```java
    ...
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/greeting/{name}")
    @NonBlocking
    public String greeting(@PathParam("name") String name) {
        return service.greeting(name);
    }
    ...
    ```

  * Add env config to application.properties
    ```
    greeting.message=hi
    greeting.name=quarkus in dev mode
    %prod.greeting.name=production quarkus
    ```
    [quarkus environment configuration](https://quarkus.io/guides/config-reference)

## Create entities, define RESTful endpoints, and add basic queries
  * Add postgresql extension
    ```
    mvn quarkus:add-extension -Dextensions="hibernate-orm-panache, jdbc-h2, jdbc-postgresql" -f $HOME/quarkus-lab/quarkus-workshop-m1m2-labs
    ```
    [hibernate orm panache reference](https://quarkus.io/guides/hibernate-orm-panache)

  * Add database connection configuration to application.properties
    ```
    %prod.quarkus.datasource.db-kind=postgresql
    %prod.quarkus.datasource.jdbc.url=jdbc:postgresql://postgres-database/person
    %prod.quarkus.datasource.jdbc.driver=org.postgresql.Driver
    
    %dev.quarkus.datasource.db-kind=h2
    %dev.quarkus.datasource.jdbc.url=jdbc:h2:mem:person
    %dev.quarkus.datasource.jdbc.driver=org.h2.Driver
    
    %test.quarkus.datasource.db-kind=h2
    %test.quarkus.datasource.jdbc.url=jdbc:h2:mem:person
    %test.quarkus.datasource.jdbc.driver=org.h2.Driver
    ```

  * Annotate Model class with @Entity
    ```java
    @Entity
    public class Person extends PanacheEntity {

    }
    ```

  * Add query using Entity to resource class
    ```java
    ...
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Person> getAll() {
        return Person.listAll();
    }
    ...
    ```

  * Add basic query to Model class
    ```java
    ...
    public static List<Person> findByColor(EyeColor color) {
        return list("eyes", color);
    }
    
    public static List<Person> getBeforeYear(int year) {
        return Person.<Person>streamAll()
        .filter(p -> p.birth.getYear() <= year)
        .collect(Collectors.toList());
    }
    ...
    ```
  
  * Add new query using Entity to resource class
    ```java
    ...
    @GET
    @Path("/eyes/{color}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Person> findByColor(@PathParam(value = "color") EyeColor color) {
        return Person.findByColor(color);
    }

    @GET
    @Path("/birth/before/{year}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Person> getBeforeYear(@PathParam(value = "year") int year) {
       return Person.getBeforeYear(year);
    }
    ...
    ```

## Implement paging and filtering and integrate life-cycle hooks
  * Add datatable endpoint to resource class
    ```java
    @GET
    @Path("/datatable")
    @Produces(MediaType.APPLICATION_JSON)
    public DataTable datatable(
            @QueryParam(value = "draw") int draw,
            @QueryParam(value = "start") int start,
            @QueryParam(value = "length") int length,
            @QueryParam(value = "search[value]") String searchVal) {

        // Begin result
        DataTable result = new DataTable();
        result.setDraw(draw);

        // Filter based on search
        PanacheQuery<Person> filteredPeople;
        if (searchVal != null && !searchVal.isEmpty()) {
            filteredPeople = Person.<Person>find("name like :search",
                    Parameters.with("search", "%" + searchVal + "%"));
        } else {
            filteredPeople = Person.findAll();
        }

        // Page and return
        int page_number = start / length;
        filteredPeople.page(page_number, length);
        result.setRecordsFiltered(filteredPeople.count());
        result.setData(filteredPeople.list());
        result.setRecordsTotal(Person.count());
        return result;
    }
    ```
  
  * Test pageable result
    ```
    curl -s "http://localhost:8080/person/datatable?draw=1&start=0&length=10&search\[value\]=vad" | jq
    ```
  
  * Add lifecycle hook to resource class
    ```java
    @Transactional
    void onStart(@Observes StartupEvent ev) {
        for (int i = 0; i < 1000; i++) {
            String name = CuteNameGenerator.generate();
            LocalDate birth = LocalDate.now().plusWeeks(Math.round(Math.floor(Math.random() * 40 * 52 * -1)));
            EyeColor color = EyeColor.values()[(int)(Math.floor(Math.random() * EyeColor.values().length))];
            Person p = new Person();
            p.birth = birth;
            p.eyes = color;
            p.name = name;
            Person.persist(p);
        }
    }
    ```

  * Test lifecycle hook result
    ```
    curl -s "http://localhost:8080/person/datatable?draw=1&start=0&length=2&search\[value\]=F" | jq
    ```

## Deploy a Quarkus application to OpenShift and test service
  * Label database service
    ```
    oc label dc/postgres-database app.openshift.io/runtime=postgresql --overwrite
    oc label dc/quarkus-person app.kubernetes.io/part-of=quarkus-person --overwrite
    oc label dc/postgres-database app.kubernetes.io/part-of=quarkus-person  --overwrite
    oc annotate dc/quarkus-person app.openshift.io/connects-to=postgres-database --overwrite
    ```
  
  * Test services
    ```
    curl -s $(oc get route quarkus-person -o=go-template --template='{{ .spec.host }}')/person/birth/before/2000 | jq  
    ```
## Documenting and Testing APIs
  * Add smallrye openapi extension
    ```
    mvn quarkus:add-extension -Dextensions="smallrye-openapi" -f $HOME/quarkus-lab/quarkus-workshop-m1m2-labs
    ```
    [openapi swaggerui reference](https://quarkus.io/guides/openapi-swaggerui)

  * Test openapi document
    ```
    # show yaml
    curl http://localhost:8080/q/openapi

    # show json
    curl -H "Accept: application/json" http://localhost:8080/q/openapi
    ```
  
  * Open swagger ui endpoint


  * Annotate @Operation and @ApiResponse to api endpoint
    ```java
    @Operation(summary = "Finds people born before a specific year", description = "Search the people database and return a list of people born before the specified year")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The list of people born before the specified year", content = @Content(schema = @Schema(implementation = Person.class))),
            @APIResponse(responseCode = "500", description = "Something bad happened")
    })
    ```

  * Annotate @Parameter to api method
    ```java
    public List<Person> getBeforeYear(
            @Parameter(description = "Cutoff year for searching for people", required = true, name = "year") @PathParam(value = "year") int year) {

        return Person.getBeforeYear(year);
    }
    ```

# Create and Config Health Check

## Configure a project to use the Quarkus SmallRye Health extension
  * Add SmallRye health extension  
    ```
    mvn quarkus:add-extension -Dextensions="smallrye-health" -f $HOME/quarkus-lab/quarkus-workshop-m1m2-labs
    ```
    [smallrye health reference](https://quarkus.io/guides/smallrye-health)

  * Test default health endpoint
    ```
    curl http://localhost:8080/q/health/ready
    curl http://localhost:8080/q/health/live 
    ```
  
  * Add a simple health check probe
    ```java
    @Readiness
    @ApplicationScoped
    public class SimpleHealthCheck implements HealthCheck {

      @Override
      public HealthCheckResponse call() {
          return HealthCheckResponse.named("Simple health check").up().build();
      }
    }
    ```
  
  * Test simple health endpoint
    ```
    curl http://localhost:8080/q/health/ready
    ```

## Create and add custom data to a custom health check
  * Create custom data health check  
    ```java
    @ApplicationScoped
    @Liveness
    public class DataHealthCheck implements HealthCheck {

      @Override
      public HealthCheckResponse call() {
          return HealthCheckResponse.named("Health check with data")
                .up()
                .withData("red", "redValue")
                .withData("hat", "hatValue")
                .build();
      }
    }
    ```

  * Test custom data health check
    ```
    curl http://localhost:8080/q/health/live
    ```

## Develop and fix negative health checks
  * Create database connection health check
    ```java
    @ApplicationScoped
    @Liveness
    public class DatabaseConnectionHealthCheck implements HealthCheck {

      @ConfigProperty(name = "database.up", defaultValue = "false")
      public boolean databaseUp;

      @Override
      public HealthCheckResponse call() {
          HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Database connection health check");
      
          try {
              simulateDatabaseConnectionVerification();
              responseBuilder.up();
          } catch (IllegalStateException e) {
              responseBuilder.down()
              .withData("error", e.getMessage());
          }

          return responseBuilder.build();
      }

      private void simulateDatabaseConnectionVerification() {
          if (!databaseUp) {
              throw new IllegalStateException("Cannot contact database");
          }
      }
    }
    ```

  * Test database probe connection
    ```
    curl http://localhost:8080/q/health/live
    ```

  * Connect probe on OpenShift
    ```
    oc set probe dc/quarkus-person --readiness --initial-delay-seconds=5 --period-seconds=5 --failure-threshold=20 --get-url=http://:8080/q//health/ready
    oc set probe dc/quarkus-person --liveness --initial-delay-seconds=5 --period-seconds=5 --failure-threshold=20 --get-url=http://:8080/q/health/live
    oc rollout latest dc/quarkus-person

    oc set probe dc/quarkus-person --remove --readiness --liveness
    ```

# Create reactive messageing microservices

## Create a reactive api
  * Add vertx extension
    ```
    mvn quarkus:add-extension -Dextensions="vertx" -f $HOME/quarkus-lab/quarkus-workshop-m1m2-labs
    ```
    [vertx reference](https://quarkus.io/guides/reactive-event-bus)

  * Add kafka extension
    ```
    mvn quarkus:add-extension -Dextensions="messaging-kafka" -f $HOME/quarkus-lab/quarkus-workshop-m1m2-labs
    ```
    [messageing kafka reference](https://quarkus.io/guides/kafka)

  * Inject vertx event bus
   ```java
   @Inject EventBus bus; 
   ```

  * Add reactive endpoint
    ```java
    @POST
    @Path("/{name}")
    public Uni<Person> addPerson(String name) {
        return bus.<Person>request("add-person", name)
                .onItem().transform(response -> response.body());
    }

    @GET
    @Path("/name/{name}")
    public Person byName(String name) {
        return Person.find("name", name).firstResult();
    }
    ```

  * Create reactive service
    ```java
    @ApplicationScoped
    public class PersonService {

        @ConsumeEvent(value = "add-person", blocking = true)
        @Transactional
        public Person addPerson(String name) {
            LocalDate birth = LocalDate.now().plusWeeks(Math.round(Math.floor(Math.random() * 20 * 52 * -1)));
            EyeColor color = EyeColor.values()[(int)(Math.floor(Math.random() * EyeColor.values().length))];
            Person p = new Person();
            p.birth = birth;
            p.eyes = color;
            p.name = name;
            Person.persist(p);
            return p;
        }
    }
    ```
  * Test reactive service
    ```
    curl -s -X POST http://localhost:8080/person/redhat | jq

    curl -s http://localhost:8080/person/name/redhat | jq
    ```

## Publish messages to and consume messages from an address
  * Rebuild app
    ```
    mvn clean package -DskipTests -f /home/mola/winhome/demo/quarkus-lab/quarkus-workshop-m1m2-labs && \
    oc label dc/quarkus-person app.kubernetes.io/part-of=quarkus-person --overwrite && \
    oc label dc/postgres-database app.kubernetes.io/part-of=quarkus-person --overwrite && \
    oc annotate dc/quarkus-person app.openshift.io/connects-to=postgres-database --overwrite && \
    oc rollout status -w dc/quarkus-person
    ```    

  * Test services
    ```
    http://(oc get route quarkus-person -o=go-template --template='{{ .spec.host }}')/names.html
    ```

# Create Microservices Metrics and Tracing

## Generate metrics in the application and query them in Prometheus
  * Add micrometer metrics extensions
    ```
    mvn quarkus:add-extension -Dextensions="micrometer-registry-prometheus" -f $HOME/quarkus-lab/quarkus-workshop-m1m2-labs
    mvn quarkus:add-extension -Dextensions="quarkus-smallrye-metrics" -f $HOME/quarkus-lab/quarkus-workshop-m1m2-labs
    ```
    [micrometer metrics reference](https://quarkus.io/guides/micrometer)

  * Add metrics into service
    ```java
    private final MeterRegistry registry;

    NameConverter(MeterRegistry registry) {
        this.registry = registry;
    }
    ```

    ```java
    public String process(String name) {
        String honorific = honorifics[(int) Math.floor(Math.random() * honorifics.length)];
        registry.counter("nameconvert.process.counter").increment();
        registry.timer("nameconvert.process.timer").record(3000, TimeUnit.MILLISECONDS);
        return honorific + " " + name;
    }
    ```

## Visualize the metrics with Grafana

## Add tracing to a Quarkus application and inspect traces in the Jaeger tracing console
  * Add opentracing extensions
    ```
    mvn quarkus:add-extension -Dextensions="smallrye-opentracing, rest-client-reactive" -f $HOME/quarkus-lab/quarkus-workshop-m1m2-labs
    ```
    [opentracing reference](https://quarkus.io/guides/opentracing)


# Add Fault Tolerance mechanism and inspect outcomes

## Add retry mechanism
  * Add fault tolerance extensions
    ```
    mvn quarkus:add-extension -Dextension=quarkus-smallrye-fault-tolerance -f $HOME/quarkus-lab/quarkus-workshop-m1m2-labs
    ```
    [smallrye fault tolerance reference](https://quarkus.io/guides/smallrye-fault-tolerance)

  * Simulate crash and generate services to see failure outout
    ```
    curl http://localhost:8080/notification/crash
    
    ./parallel.sh "curl localhost:8080/notification" 10
    ```
  
  * Add retry and restart service to see the correct response
    ```java
    @GET
    @Retry(maxRetries = 10)
    public Response getNotifications() {
      ...
    }
    ```

    ```
    curl http://localhost:8080/notification/crash
    
    ./parallel.sh "curl localhost:8080/notification" 10
    ```
    
## Add timeout and Fallback mechanism
  * reset service and trigger timeout, generate service to see some service take more than 1 second
    ```
    comment out @Retry

    curl http://localhost:8080/notification/reset

    curl http://localhost:8080/notification/timeout

    ./parallel.sh "curl localhost:8080/notification" 10
    ```

  * Add Fallback and timeout
    ```java
    @GET
    @Fallback(fallbackMethod = "getFallbackNotifications")
    @Timeout(500)
    public Response getNotifications() {
      ...
    }
    ```

    ```java
    public Response getFallbackNotifications() {
        count++;
        logger.info(String.format("notification request failback from: %s: %d", HOSTNAME, count));
        return Response.ok(String.format(RESPONSE_STRING_FALLBACK_FORMAT, HOSTNAME, count)).build();
    }
    ```
  
  * Restart service and see the correct response
    ```
    curl http://localhost:8080/notification/timeout

    ./parallel.sh "curl localhost:8080/notification" 10
    ```

## Add circuit breaker and Fallback mechanism
  * reset service and trigger crash and timeout, generate service to see deply
    ```
    comment out @Timeout

    curl http://localhost:8080/notification/reset

    curl http://localhost:8080/notification/crash

    curl http://localhost:8080/notification/timeout

    ./sequential.sh "curl -s localhost:8080/notification"
    ```
  
  * Add circuit breaker mechanism
    ```java
    @GET
    @CircuitBreaker(requestVolumeThreshold = 2, failureRatio = 1, delay = 10000)
    @Fallback(fallbackMethod = "getFallbackNotifications")
    public Response getNotifications() {
      ...
    }
    ```

  * Restart service and see the correct response
    ```
    curl http://localhost:8080/notification/crash

    curl http://localhost:8080/notification/timeout

    ./sequential.sh "curl -s localhost:8080/notification"
    ```