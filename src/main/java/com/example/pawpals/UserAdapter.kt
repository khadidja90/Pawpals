// UserAdapter.kt
package com.example.pawpals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import android.util.Log


class UserAdapter(private val users: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emailText: TextView = view.findViewById(R.id.emailText)
        val followButton: Button = view.findViewById(R.id.followButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.emailText.text = user.email

        FollowManager.isFollowing(user.id) { isFollowing ->
            holder.followButton.text = if (isFollowing) "Se désabonner" else "S’abonner"

            holder.followButton.setOnClickListener {
                if (isFollowing) {
                    FollowManager.unfollowUser(user.id,
                        onSuccess = {
                            holder.followButton.text = "S’abonner"
                        },
                        onFailure = {
                            Log.e("Follow", "Erreur follow : ${it.message}")
                        }

                    )
                } else {
                    FollowManager.followUser(user.id,
                        onSuccess = {
                            holder.followButton.text = "Se désabonner"
                        },
                        onFailure = {
                            Log.e("Follow", "Erreur follow : ${it.message}")
                        }

                    )
                }
            }
        }
    }

    override fun getItemCount() = users.size
}
