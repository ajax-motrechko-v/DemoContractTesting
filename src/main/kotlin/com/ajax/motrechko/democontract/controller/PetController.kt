package com.ajax.motrechko.democontract.controller

import com.ajax.motrechko.democontract.model.Pet
import com.ajax.motrechko.democontract.service.PetService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/pets")
class PetController(private val petService: PetService) {

    // OPERATION: Get all pets
    @GetMapping
    fun getAllPets(): List<Pet> {
        return petService.getAllPets()
    }

    // OPERATION: Get pet by ID
    @GetMapping("/{id}")
    fun getPetById(@PathVariable id: Long): ResponseEntity<Pet> {
        val pet = petService.getPetById(id)
        return if (pet != null) {
            ResponseEntity.ok(pet)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    // OPERATION: Create a new pet
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPet(@RequestBody pet: Pet): Pet {
        return petService.createPet(pet)
    }

    // OPERATION: Update an existing pet
    @PutMapping("/{id}")
    fun updatePet(
        @PathVariable id: Long,
        @RequestBody pet: Pet,
    ): ResponseEntity<Pet> {
        val updatedPet = petService.updatePet(id, pet)
        return if (updatedPet != null) {
            ResponseEntity.ok(updatedPet)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    // OPERATION: Delete a pet
    @DeleteMapping("/{id}")
    fun deletePet(@PathVariable id: Long): ResponseEntity<Void> {
        val deleted = petService.deletePet(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
