package com.capstone.healthradar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var fullNameTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var municipalityTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize TextViews
        fullNameTextView = view.findViewById(R.id.fullNameTextView)
        phoneTextView = view.findViewById(R.id.phoneTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        municipalityTextView = view.findViewById(R.id.municipalityTextView)

        // Initialize Edit Profile button
        val editProfileButton = view.findViewById<Button>(R.id.editProfileButton)

        // Load user data
        loadUserData()

        // Handle logout
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // Handle edit profile navigation
        editProfileButton.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out
            )
            transaction.replace(
                R.id.nav_host_fragment,
                EditProfileFragment()
            )
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName") ?: ""
                        val lastName = document.getString("lastName") ?: ""
                        val phone = document.getString("phone") ?: ""
                        val email = document.getString("email") ?: ""
                        val municipality = document.getString("municipality") ?: ""

                        fullNameTextView.text = "$firstName $lastName"
                        phoneTextView.text = phone
                        emailTextView.text = email
                        municipalityTextView.text = municipality
                    } else {
                        Toast.makeText(requireContext(), "Profile not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
