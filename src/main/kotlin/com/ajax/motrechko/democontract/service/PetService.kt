package com.ajax.motrechko.democontract.service

import com.ajax.motrechko.democontract.model.Pet
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong

@Service
class PetService {

    // In-memory storage for pets
    private val pets = mutableListOf<Pet>()

    // Atomic counter for generating unique IDs
    private val idCounter = AtomicLong(1)

    fun getAllPets(): List<Pet> {
        return pets.toList()
    }

    fun getPetById(id: Long): Pet? {
        return pets.find { it.id == id }
    }

    fun createPet(pet: Pet): Pet {

        val newId = if (pet.id <= 0 || pets.any { it.id == pet.id }) {
            idCounter.getAndIncrement()
        } else {
            pet.id
        }

        val newPet = pet.copy(id = newId)
        pets.add(newPet)
        return newPet
    }

    fun updatePet(id: Long, pet: Pet): Pet? {
        val index = pets.indexOfFirst { it.id == id }
        if (index == -1) return null

        val updatedPet = pet.copy(id = id)
        pets[index] = updatedPet
        return updatedPet
    }

    fun deletePet(id: Long): Boolean {
        return pets.removeIf { it.id == id }
    }
}
