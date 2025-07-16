package com.example.pawpals

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private val postList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PostAdapter(postList)
        recyclerView.adapter = adapter

        // Affiche email utilisateur connecté
        val currentUser = auth.currentUser
        if (currentUser != null) {
            welcomeText.text = "Bienvenue, ${currentUser.email} !"
        } else {
            welcomeText.text = "Utilisateur non connecté."
        }

        // Déconnexion
        logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, login::class.java))
            finish()
        }

        // Navigation entre les pages
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_add -> {
                    startActivity(Intent(this, AddPostActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Charger tous les posts sans filtre (test temporaire)
        loadFollowedPosts { posts ->
            postList.clear()
            postList.addAll(posts)
            adapter.notifyDataSetChanged()
        }

        findViewById<Button>(R.id.btnSeeAllUsers).setOnClickListener {
            startActivity(Intent(this, AllUsersActivity::class.java))
        }
    }

    // ⚠️ Version temporaire : charger TOUS les posts pour test
    private fun loadFollowedPosts(onResult: (List<Post>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .collection("following")
            .get()
            .addOnSuccessListener { followingSnapshot ->
                val followedUserIds = followingSnapshot.documents.map { it.id }

                if (followedUserIds.isEmpty()) {
                    Toast.makeText(this, "Aucun utilisateur suivi.", Toast.LENGTH_SHORT).show()
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                // Firestore limite à 10 éléments pour whereIn
                val filteredUserIds = followedUserIds.take(10)

                db.collection("posts")
                    .whereIn("userId", filteredUserIds)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { postSnap ->
                        val posts = postSnap.documents.mapNotNull { it.toObject(Post::class.java) }
                        Toast.makeText(this, "Posts filtrés récupérés : ${posts.size}", Toast.LENGTH_SHORT).show()
                        onResult(posts)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erreur Firestore : ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur chargement des suivis : ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

}






