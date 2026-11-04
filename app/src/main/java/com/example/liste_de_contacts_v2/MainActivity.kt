package com.example.liste_de_contacts_v2

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import com.example.liste_de_contacts_v2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ContactAdapter

    private val contacts = mutableListOf(
        Contact(1, "Dupont", "Claire", "0612345678", "claire@example.com"),
        Contact(2, "Martin", "Thomas", "0723456789", "thomas@example.com"),
        Contact(3, "Bernard", "Sophie", "0698765432", "sophie@example.com"),
        Contact(4, "Petit", "Lucas", "0711223344", "lucas@example.com"),
        Contact(5, "Moreau", "Julien", "0655443322", "julien@example.com")
    )

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data!!
            val modifie = IntentCompat.getSerializableExtra(data, "contact", Contact::class.java)
            val pos = data.getIntExtra("position", -1)
            if (modifie != null && pos != -1) {
                contacts[pos] = modifie
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ContactAdapter(this, contacts)
        binding.listView.adapter = adapter

        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("contact", contacts[position])
            intent.putExtra("position", position)
            detailLauncher.launch(intent)
        }
    }
}