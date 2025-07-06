import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.PactDslJsonArray
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.V4Pact
import au.com.dius.pact.core.model.annotations.Pact
import com.ajax.motrechko.democontract.client.api.PetApi
import com.ajax.motrechko.democontract.client.model.Pet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = "pet_provider")
class PetApiConsumerPactTest {

    // Pact consumer test lifecycle (clear and concise)
    // 1. Define the expected interaction using the @Pact method (request + expected response).
    // 2. Start a mock HTTP server with Pact using @PactTestFor and the defined interaction.
    // 3. The consumer client (e.g., PetApi) sends a real HTTP request to the mock server.
    // 4. The mock server responds based on the Pact interaction defined earlier.
    // 5. Assertions verify that the consumer correctly handles and parses the response.
    // 6. If the interaction matches and assertions pass, Pact generates a Pact contract file (.json).
    // 7. This file can later be verified by the provider to ensure both sides are in sync.

    // --- CONTRACT: GET /api/pets (with DSL)
    @Pact(consumer = CONSUMER, provider = PROVIDER)
    fun getAllPetsPact(builder: PactDslWithProvider): V4Pact {
        val body = PactDslJsonArray
            .arrayMinLike(1)
            .integerType("id", 1.toInt())
            .stringType("name", "Buddy")
            .stringType("type", "Dog")
            .integerType("age", 3.toInt())
            .stringType("breed", "Golden Retriever")
            .stringType("description", "Friendly and playful")

        return builder
            .given("Pets exist in the system")
            .uponReceiving("GET /api/pets returns list of available pets")
            .path("/api/pets")
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(body)
            .toPact(V4Pact::class.java)
    }

    // --- CONTRACT: GET /api/pets/1 (raw JSON variant)
    @Pact(consumer = CONSUMER, provider = PROVIDER)
    fun getPetByIdPact(builder: PactDslWithProvider): V4Pact {
        return builder
            .given("Pet with ID 1 exists")
            .uponReceiving("GET /api/pets/1 returns specific pet")
            .path("/api/pets/1")
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(
                """
                {
                  "id": 1,
                  "name": "Buddy",
                  "type": "Dog",
                  "age": 3,
                  "breed": "Golden Retriever",
                  "description": "Friendly and playful"
                }
                """.trimIndent()
            )
            .toPact(V4Pact::class.java)
    }

    // --- CONTRACT: POST /api/pets (uses PactDslJsonBody for request and response)
    @Pact(consumer = CONSUMER, provider = PROVIDER)
    fun createPetPact(builder: PactDslWithProvider): V4Pact {
        val requestBody = buildPetBody(
            "Rex",
            "Dog",
            2,
            "German Shepherd",
            "Loyal and protective"
        )

        val responseBody = buildPetBody(
            "Rex",
            "Dog",
            2,
            "German Shepherd",
            "Loyal and protective",
            6
        )

        return builder
            .given("Pet service is available")
            .uponReceiving("POST /api/pets creates new pet")
            .path("/api/pets")
            .method("POST")
            .body(requestBody)
            .willRespondWith()
            .status(201)
            .body(responseBody)
            .toPact(V4Pact::class.java)
    }

    // --- TEST: GET /api/pets using OpenAPI-generated PetApi client
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
        assertEquals("Dog", pets[0].type)
        assertEquals(3, pets[0].age)
        assertEquals("Golden Retriever", pets[0].breed)
    }

    // --- TEST: GET /api/pets using HttpClient directly
    @Test
    @PactTestFor(pactMethod = "getAllPetsPact")
    fun `should get all pets using HttpClient`(mockServer: MockServer) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${mockServer.getUrl()}/api/pets"))
            .GET()
            .build()

        val response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString())

        assertEquals(200, response.statusCode())
        val body = response.body()
        assertNotNull(body)
        assert(body.contains("Buddy"))
        assert(body.contains("Dog"))
        assert(body.contains("Golden Retriever"))
        assert(body.contains("id"))
        assert(body.contains("name"))
    }

    // --- TEST: GET /api/pets/1 using OpenAPI client
    @Test
    @PactTestFor(pactMethod = "getPetByIdPact")
    fun testGetPetById(mockServer: MockServer) {
        val apiClient = PetApi(mockServer.getUrl())
        val pet = apiClient.getPetById(1)

        assertNotNull(pet)
        assertEquals(1, pet.id)
        assertEquals("Buddy", pet.name)
        assertEquals("Dog", pet.type)
        assertEquals(3, pet.age)
        assertEquals("Golden Retriever", pet.breed)
    }

    // --- TEST: POST /api/pets using OpenAPI client
    @Test
    @PactTestFor(pactMethod = "createPetPact")
    fun testCreatePet(mockServer: MockServer) {
        val apiClient = PetApi(mockServer.getUrl())
        val newPet = Pet(
            id = null,
            name = "Rex",
            type = "Dog",
            age = 2,
            breed = "German Shepherd",
            description = "Loyal and protective"
        )

        val createdPet = apiClient.createPet(newPet)

        assertNotNull(createdPet)
        assertEquals(6, createdPet.id)
        assertEquals("Rex", createdPet.name)
        assertEquals("Dog", createdPet.type)
        assertEquals(2, createdPet.age)
        assertEquals("German Shepherd", createdPet.breed)
    }

    private fun buildPetBody(
        name: String,
        type: String,
        age: Int,
        breed: String,
        description: String,
        id: Int? = null,
    ): PactDslJsonBody {
        val body = PactDslJsonBody()
            .stringType("name", name)
            .stringType("type", type)
            .integerType("age", age)
            .stringType("breed", breed)
            .stringType("description", description)

        id?.let { body.integerType("id", it) }

        return body
    }

    private companion object {
        const val CONSUMER = "pet_consumer"
        const val PROVIDER = "pet_provider"
    }
}
