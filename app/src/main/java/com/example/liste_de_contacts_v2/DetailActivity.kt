package com.example.liste_de_contacts_v2

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import com.example.liste_de_contacts_v2.databinding.ActivityDetailBinding
import java.io.File
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var contact: Contact
    private var position: Int = -1
    private var photoPath: String? = null
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {
            Toast.makeText(this, "Sélection annulée", Toast.LENGTH_SHORT).show()
        } else {
            photoPath = copierDansStockageInterne(uri)
            binding.ivPhoto.setImageURI(Uri.fromFile(File(photoPath!!)))
        }
    }
    private var fichierPhoto: File? = null

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { succes ->
        if (succes) {
            photoPath = fichierPhoto?.absolutePath
            binding.ivPhoto.setImageURI(Uri.fromFile(fichierPhoto))
        } else {
            Toast.makeText(this, "Prise de photo annulée", Toast.LENGTH_SHORT).show()
        }
    }
    private fun lancerCamera() {
        fichierPhoto = File(filesDir, "photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", fichierPhoto!!)
        try {
            takePicture.launch(uri)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Aucune application caméra", Toast.LENGTH_SHORT).show()
        }
    }

    private val demandePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { accordee ->
        if (accordee) {
            lancerCamera()
        } else {
            Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_LONG).show()
        }
    }
    private fun copierDansStockageInterne(uri: Uri): String {
        val fichier = File(filesDir, "contact_${System.currentTimeMillis()}.jpg")
        contentResolver.openInputStream(uri)?.use { input ->
            fichier.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return fichier.absolutePath
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contact = IntentCompat.getSerializableExtra(intent, "contact", Contact::class.java)!!
        position = intent.getIntExtra("position", -1)
        photoPath = contact.photoPath

        // Afficher les données
        binding.etNom.setText(contact.nom)
        binding.etPrenom.setText(contact.prenom)
        binding.etTelephone.setText(contact.telephone)
        binding.etEmail.setText(contact.email)
        binding.btnGalerie.setOnClickListener {
            pickImage.launch("image/*")
        }
        binding.btnCamera.setOnClickListener {
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                Toast.makeText(this, "Caméra indisponible", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                lancerCamera()
            } else {
                demandePermission.launch(Manifest.permission.CAMERA)
            }
        }

        // Afficher la photo
        if (photoPath == null) {
            binding.ivPhoto.setImageResource(R.drawable.ic_person)
        } else {
            binding.ivPhoto.setImageURI(Uri.fromFile(File(photoPath!!)))
        }

        // Bouton Annuler
        binding.btnAnnuler.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Bouton Appeler (Intent implicite)
        binding.btnAppeler.setOnClickListener {
            val tel = binding.etTelephone.text.toString().trim()
            if (tel.isEmpty()) {
                binding.etTelephone.error = "Numéro vide"
                return@setOnClickListener
            }
            val i = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))
            try {
                startActivity(i)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Aucune application d'appel", Toast.LENGTH_SHORT).show()
            }
        }

        // Bouton Envoyer un e-mail (Intent implicite)
        binding.btnEmail.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                binding.etEmail.error = "E-mail vide"
                return@setOnClickListener
            }
            val i = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            }
            try {
                startActivity(i)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Aucune application de messagerie", Toast.LENGTH_SHORT).show()
            }
        }

        // Bouton Enregistrer
        binding.btnEnregistrer.setOnClickListener {
            val nom = binding.etNom.text.toString().trim()
            val prenom = binding.etPrenom.text.toString().trim()
            val tel = binding.etTelephone.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()

            if (nom.isEmpty()) {
                binding.etNom.error = "Champ obligatoire"
                return@setOnClickListener
            }
            if (prenom.isEmpty()) {
                binding.etPrenom.error = "Champ obligatoire"
                return@setOnClickListener
            }
            if (tel.isEmpty()) {
                binding.etTelephone.error = "Champ obligatoire"
                return@setOnClickListener
            }

            val modifie = contact.copy(
                nom = nom,
                prenom = prenom,
                telephone = tel,
                email = email,
                photoPath = photoPath
            )

            val result = Intent()
            result.putExtra("contact", modifie)
            result.putExtra("position", position)
            setResult(RESULT_OK, result)
            finish()
        }
    }
}