package com.ajax.motrechko.democontract.model

data class Pet(
    val id: Long,
    val name: String,
    val type: String,
    val age: Int,
    val breed: String? = null,
    val description: String? = null,
)
