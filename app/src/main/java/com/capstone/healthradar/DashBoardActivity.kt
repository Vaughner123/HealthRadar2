package com.capstone.healthradar

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlin.jvm.java

class DashBoardActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check user login status
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            // If no user is logged in, redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_dashboard)

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Default fragment
        bottomNavigationView.selectedItemId = R.id.nav_home
        loadFragment(HomeFragment())

        // Navigation listener
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_news-> {
                    loadFragment(NewsFragment())
                    true
                }
                R.id.nav_map -> {
                    loadFragment(MapFragment())
                    true
                }
                R.id.nav_records -> {
                    loadFragment(RecordFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}
