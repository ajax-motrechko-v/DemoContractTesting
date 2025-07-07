package com.ajax.motrechko.democontract.provider

import au.com.dius.pact.provider.junit5.HttpTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.VerificationReports
import au.com.dius.pact.provider.junitsupport.loader.PactBroker
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth
import com.ajax.motrechko.democontract.DemoContractApplication
import com.ajax.motrechko.democontract.config.TestConfig
import com.ajax.motrechko.democontract.model.Pet
import com.ajax.motrechko.democontract.service.PetService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@Provider("pet_provider")
@PactBroker(
    host = "localhost",
    port = "9292",
    scheme = "http",
    authentication = PactBrokerAuth(username = "pact", password = "pact")
)
@VerificationReports("console")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [TestConfig::class, DemoContractApplication::class]
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PetApiProviderPactTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var petService: PetService

    // Pact provider test lifecycle (corrected and clarified)
    // 1. Set up the target (e.g., HttpTestTarget) to direct Pact where to send real HTTP requests.
    // 2. Load Pact contract files (e.g., from build/pacts or a Pact Broker).
    // 3. For each interaction, initialize provider state using @State methods.
    // 4. Inside each state method, use mocks (e.g., Mockito) or seed test data to simulate expected behavior.
    // 5. Pact sends a real HTTP request to the provider and verifies that the response matches the contract.
    // 6. If all interactions pass, the provider test succeeds and confirms contract compliance.

    // Set up the target for Pact verification
    @BeforeEach
    fun before(context: PactVerificationContext) {
        context.target = HttpTestTarget("localhost", port)
        System.setProperty("pact.verifier.publishResults", "true")
    }


    // Pact verification test runner
    // This method will be invoked for each interaction defined in the Pact files
    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider::class)
    fun pactVerificationTestTemplate(context: PactVerificationContext) {
        context.verifyInteraction()
    }

    // --- STATE: GET /api/pets
    @State("Pets exist in the system")
    fun setupPetsExist() {
        whenever(petService.getAllPets()).thenReturn(
            listOf(
                Pet(1, "Buddy", "Dog", 3, "Golden Retriever", "Friendly and playful"),
                Pet(2, "Whiskers", "Cat", 5, "Siamese", "Independent and curious")
            )
        )
    }

    // --- STATE: GET /api/pets/1
    @State("Pet with ID 1 exists")
    fun setupPetWithId1Exists() {
        whenever(petService.getPetById(1)).thenReturn(
            Pet(1, "Buddy", "Dog", 3, "Golden Retriever", "Friendly and playful")
        )
    }

    // --- STATE: POST /api/pets
    @State("Pet service is available")
    fun setupPetServiceAvailable() {
        whenever(petService.createPet(any())).thenReturn(
            Pet(6, "Rex", "Dog", 2, "German Shepherd", "Loyal and protective")
        )
    }
}
