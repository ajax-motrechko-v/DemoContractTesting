# Pet Management API - Contract Testing Demo

A proof-of-concept project demonstrating contract testing with Pact in a Spring Boot application. This project implements a simple RESTful API for managing pets, with both consumer and provider sides of contract testing.

## Project Overview

This project showcases how to implement contract testing in a microservices architecture using the Pact framework. It consists of:

- A Spring Boot REST API for pet management (provider)
- An OpenAPI-generated client for consuming the API (consumer)
- Pact contract tests for both consumer and provider sides

The application uses an in-memory storage for simplicity, making it perfect for learning and demonstration purposes.

## Technologies Used

- **Kotlin 1.9.25** - Modern, concise JVM language
- **Spring Boot 3.5.3** - Framework for building web applications
- **Java 21** - Latest LTS Java version
- **Pact 4.6.8** - Contract testing framework
- **OpenAPI Generator 7.4.0** - For generating API clients from Swagger specs
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework for testing

## Project Structure

```
src/
├── main/
│   ├── kotlin/
│   │   └── com/ajax/motrechko/democontract/
│   │       ├── controller/      # REST API controllers
│   │       ├── model/           # Data models
│   │       ├── service/         # Business logic
│   │       └── DemoContractApplication.kt  # Main application class
│   └── resources/
│       ├── application.properties  # Application configuration
│       └── static/
│           └── swagger.yaml        # OpenAPI specification
└── test/
    └── kotlin/
        └── com/ajax/motrechko/democontract/
            ├── config/           # Test configuration
            ├── consumer/         # Consumer contract tests
            └── provider/         # Provider contract tests
```

## Getting Started

### Prerequisites

- JDK 21 or later
- Gradle 8.x (or use the included Gradle wrapper)

### Building the Project

```bash
./gradlew build
```

### Running the Application

```bash
./gradlew bootRun
```

The application will start on http://localhost:8080.

## API Endpoints

The API provides the following endpoints:

- `GET /api/pets` - Get all pets
- `GET /api/pets/{id}` - Get a pet by ID
- `POST /api/pets` - Create a new pet
- `PUT /api/pets/{id}` - Update an existing pet
- `DELETE /api/pets/{id}` - Delete a pet

## Contract Testing with Pact

This project demonstrates contract testing using the Pact framework, which helps ensure that services can communicate with each other as expected. Contract testing is a technique that validates the interactions between two services by testing the contracts (interfaces) between them, rather than testing the entire integration.

### Contract Testing Workflow in This Project

The contract testing workflow in this project follows these steps:

1. **Define Contracts**: The consumer defines contracts that specify the expected requests and responses for each API endpoint.
2. **Generate Pact Files**: Running the consumer tests generates Pact files (JSON files) that contain the contracts.
3. **Share Pact Files**: The Pact files are shared with the provider (in this project, they're stored in the `build/pacts/` directory).
4. **Verify Provider**: The provider tests load the Pact files and verify that the provider implementation meets the expectations defined in the contracts.
5. **Continuous Integration**: In a real-world scenario, these tests would be part of a CI/CD pipeline to ensure contracts are always honored.

### Consumer Tests in Detail

The consumer tests (`PetApiConsumerPactTest`) define the expectations that the consumer has of the provider. These tests use the Pact Consumer DSL to define contracts and verify that the consumer code works against these contracts.

#### Key Components of Consumer Tests

1. **Pact Annotations**:
   - `@ExtendWith(PactConsumerTestExt::class)`: Enables Pact consumer testing with JUnit 5
   - `@PactTestFor(providerName = "pet_provider")`: Specifies the provider name
   - `@Pact(consumer = "pet_consumer", provider = "pet_provider")`: Defines a Pact contract

2. **Contract Definition Methods**:
   Each contract is defined in a method annotated with `@Pact`. For example:

   ```kotlin
   @Pact(consumer = "pet_consumer", provider = "pet_provider")
   fun getAllPetsPact(builder: PactDslWithProvider): V4Pact {
       val body = PactDslJsonArray
           .arrayMinLike(1)
           .integerType("id", 1)
           .stringType("name", "Buddy")
           .stringType("type", "Dog")
           .integerType("age", 3)
           .stringType("breed", "Golden Retriever")
           .stringType("description", "Friendly and playful")

       return builder
           .given("Pets exist in the system")  // Provider state
           .uponReceiving("GET /api/pets returns list of available pets")  // Interaction description
           .path("/api/pets")  // API endpoint path
           .method("GET")  // HTTP method
           .willRespondWith()  // Expected response
           .status(200)  // HTTP status code
           .body(body)  // Expected response body
           .toPact(V4Pact::class.java)
   }
   ```

3. **Test Methods**:
   Each test method is annotated with `@Test` and `@PactTestFor(pactMethod = "...")` to link it to a specific contract. The test method uses the `MockServer` provided by Pact to test the consumer code against the defined contract:

   ```kotlin
   @Test
   @PactTestFor(pactMethod = "getAllPetsPact")
   fun testGetAllPets(mockServer: MockServer) {
       val apiClient = PetApi(mockServer.getUrl())
       val pets = apiClient.getAllPets()

       assertNotNull(pets)
       assert(pets.isNotEmpty())
       assertEquals(1, pets.size)
       assertEquals(1, pets[0].id)
       assertEquals("Buddy", pets[0].name)
       // ... more assertions
   }
   ```

4. **Different Ways to Define Contracts**:
   The project demonstrates multiple ways to define contracts:
   - Using the Pact DSL for type-based matching (e.g., `PactDslJsonArray`, `PactDslJsonBody`)
   - Using raw JSON for example-based matching
   - Defining both request and response bodies for POST/PUT endpoints

5. **Different Ways to Test Contracts**:
   The project demonstrates multiple ways to test contracts:
   - Using the OpenAPI-generated client (`PetApi`)
   - Using the Java HTTP client directly

To run the consumer tests:

```bash
./gradlew test --tests "*.consumer.*"
```

### Provider Tests in Detail

The provider tests (`PetApiProviderPactTest`) verify that the provider meets the expectations defined by the consumer. These tests load the Pact files generated by the consumer and verify that the provider implementation responds as expected.

#### Key Components of Provider Tests

1. **Pact Annotations**:
   - `@Provider("pet_provider")`: Specifies the provider name (must match the consumer's definition)
   - `@PactFolder("build/pacts")`: Specifies where to find the Pact files
   - `@SpringBootTest`: Starts the Spring Boot application for testing
   - `@TestInstance(TestInstance.Lifecycle.PER_CLASS)`: Ensures a single instance of the test class is used

2. **Test Configuration**:
   The provider test uses a custom test configuration (`TestConfig`) that provides mock implementations of the service layer:

   ```kotlin
   @TestConfiguration
   class TestConfig {
       @Bean
       fun petService(): PetService = mock(PetService::class.java)
   }
   ```

3. **Provider State Methods**:
   Each provider state defined in the consumer contracts is implemented as a method annotated with `@State`:

   ```kotlin
   @State("Pets exist in the system")
   fun setupPetsExist() {
       whenever(petService.getAllPets()).thenReturn(
           listOf(
               Pet(1, "Buddy", "Dog", 3, "Golden Retriever", "Friendly and playful"),
               Pet(2, "Whiskers", "Cat", 5, "Siamese", "Independent and curious")
           )
       )
   }
   ```

4. **Verification Method**:
   A single `@TestTemplate` method is used to verify all interactions defined in the Pact files:

   ```kotlin
   @TestTemplate
   @ExtendWith(PactVerificationInvocationContextProvider::class)
   fun pactVerificationTestTemplate(context: PactVerificationContext) {
       context.verifyInteraction()
   }
   ```

To run the provider tests:

```bash
./gradlew test --tests "*.provider.*"
```

### Provider States Explained

Provider states are a key concept in Pact. They represent the state that the provider must be in to satisfy a particular interaction. In this project:

1. **"Pets exist in the system"**: Used for the GET /api/pets endpoint, ensures that the provider returns a list of pets.
2. **"Pet with ID 1 exists"**: Used for the GET /api/pets/1 endpoint, ensures that the provider returns a specific pet.
3. **"Pet service is available"**: Used for the POST /api/pets endpoint, ensures that the provider can create a new pet.

Provider states help to set up the necessary preconditions for each test, making the tests more reliable and focused.

### OpenAPI Client Generation and Its Role in Testing

This project uses the OpenAPI Generator to generate a client for the API based on the Swagger specification. This generated client is used in the consumer tests to interact with the mock server:

```kotlin
val apiClient = PetApi(mockServer.getUrl())
val pets = apiClient.getAllPets()
```

Using a generated client has several advantages:
1. **Type Safety**: The client provides type-safe methods for interacting with the API.
2. **Consistency**: The client is generated from the same specification that defines the API, ensuring consistency.
3. **Realistic Testing**: Using the actual client that consumers would use makes the tests more realistic.

### Benefits of Contract Testing

- **Early Detection of Breaking Changes**: Catch integration issues before they reach production
- **Independent Service Evolution**: Services can evolve independently as long as they honor their contracts
- **Documentation**: Contracts serve as living documentation of service interactions
- **Confidence**: Greater confidence when deploying changes to production
- **Faster Feedback**: Tests run quickly without requiring full integration environments
- **Focused Testing**: Tests focus on the boundaries between services, not their internal implementation

### Best Practices Demonstrated in This Project

1. **Clear Provider States**: Each provider state is clearly defined and implemented.
2. **Type-Based Matching**: Using type-based matching (e.g., `stringType`, `integerType`) instead of exact values makes the tests more flexible.
3. **Multiple Testing Approaches**: Testing with both the generated client and direct HTTP calls demonstrates different ways to verify contracts.
4. **Mocking Dependencies**: The provider tests mock the service layer to focus on the API contract, not the business logic.
5. **Descriptive Interaction Names**: Each interaction has a descriptive name that explains what it's testing.
6. **Separation of Concerns**: Consumer and provider tests are kept separate, with clear responsibilities.
