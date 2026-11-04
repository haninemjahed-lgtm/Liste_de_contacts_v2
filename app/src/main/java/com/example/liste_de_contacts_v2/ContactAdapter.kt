package com.example.liste_de_contacts_v2

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.liste_de_contacts_v2.databinding.ItemContactBinding
import java.io.File

class ContactAdapter(
    private val context: Context,
    private val contacts: List<Contact>
) : BaseAdapter() {

    override fun getCount(): Int = contacts.size
    override fun getItem(position: Int): Contact = contacts[position]
    override fun getItemId(position: Int): Long = contacts[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null)
            ItemContactBinding.inflate(LayoutInflater.from(context), parent, false)
        else
            ItemContactBinding.bind(convertView)

        val c = contacts[position]

        binding.tvNomPrenom.text = "${c.nom} ${c.prenom}"
        binding.tvTelephone.text = c.telephone

        if (c.photoPath == null) {
            binding.ivPhoto.setImageResource(R.drawable.ic_person)
        } else {
            binding.ivPhoto.setImageURI(Uri.fromFile(File(c.photoPath!!)))
        }

        return binding.root
    }
}