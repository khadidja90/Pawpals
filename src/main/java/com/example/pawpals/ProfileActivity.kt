package com.example.pawpals

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private val postList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PostAdapter(postList)
        recyclerView.adapter = adapter

        // Charger les posts de l’utilisateur courant
        fetchUserPosts()

        // Bouton d'abonnement
        val btnFollow = findViewById<Button>(R.id.btnFollow)

        //  Ce userId doit venir d’un profil que tu consultes (et non l'utilisateur courant)
        val targetUserId = intent.getStringExtra("targetUserId") ?: return

        FollowManager.isFollowing(targetUserId) { isFollowing ->
            btnFollow.text = if (isFollowing) "Se désabonner" else "S’abonner"

            btnFollow.setOnClickListener {
                if (isFollowing) {
                    FollowManager.unfollowUser(targetUserId,
                        onSuccess = {
                            btnFollow.text = "S’abonner"
                            Toast.makeText(this, "Désabonné", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = {
                            Toast.makeText(this, "Erreur : ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    FollowManager.followUser(targetUserId,
                        onSuccess = {
                            btnFollow.text = "Se désabonner"
                            Toast.makeText(this, "Abonné", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = {
                            Toast.makeText(this, "Erreur : ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    private fun fetchUserPosts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp") // tri par date
            .get()
            .addOnSuccessListener { documents ->
                postList.clear()
                for (doc in documents) {
                    val post = Post(
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: ""
                    )
                    postList.add(post)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show()
            }
    }
}
