package com.example.pawpals

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


object FollowManager {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun followUser(targetUserId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return

        val followRef = db.collection("users").document(currentUserId)
            .collection("following").document(targetUserId)

        val followerRef = db.collection("users").document(targetUserId)
            .collection("followers").document(currentUserId)

        val data = mapOf("timestamp" to FieldValue.serverTimestamp())

        db.runBatch { batch ->
            batch.set(followRef, data)
            batch.set(followerRef, data)
        }.addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun unfollowUser(targetUserId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return

        val followRef = db.collection("users").document(currentUserId)
            .collection("following").document(targetUserId)

        val followerRef = db.collection("users").document(targetUserId)
            .collection("followers").document(currentUserId)

        db.runBatch { batch ->
            batch.delete(followRef)
            batch.delete(followerRef)
        }.addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun isFollowing(targetUserId: String, callback: (Boolean) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUserId)
            .collection("following").document(targetUserId)
            .get()
            .addOnSuccessListener { callback(it.exists()) }
    }

}
