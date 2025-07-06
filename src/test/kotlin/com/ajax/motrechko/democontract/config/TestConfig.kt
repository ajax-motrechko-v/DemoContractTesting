package com.ajax.motrechko.democontract.config

import com.ajax.motrechko.democontract.service.PetService
import org.mockito.Mockito.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestConfig {
    @Bean
    fun petService(): PetService = mock(PetService::class.java)
}
