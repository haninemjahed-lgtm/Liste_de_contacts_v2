package com.example.liste_de_contacts_v2

data class Contact(
    val id: Int,
    var nom: String,
    var prenom: String,
    var telephone: String,
    var email: String,
    var photoPath: String? = null
) : java.io.Serializable