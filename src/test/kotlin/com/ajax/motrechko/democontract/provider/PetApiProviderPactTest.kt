package com.ajax.motrechko.democontract.provider

import au.com.dius.pact.provider.junit5.HttpTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.loader.PactFolder
import com.ajax.motrechko.democontract.DemoContractApplication
import com.ajax.motrechko.democontract.model.Pet
import com.ajax.motrechko.democontract.service.PetService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean

@Provider("pet_provider")
@PactFolder("build/pacts")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [PetApiProviderPactTest.TestConfig::class, DemoContractApplication::class]
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PetApiProviderPactTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        fun petService(): PetService = mock(PetService::class.java)
    }

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var petService: PetService

    @BeforeEach
    fun before(context: PactVerificationContext) {
        context.target = HttpTestTarget("localhost", port)
    }

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
