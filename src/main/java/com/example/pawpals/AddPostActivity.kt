package com.example.pawpals

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import com.example.pawpals.network.CloudinaryResponse
import com.example.pawpals.network.RetrofitClient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth


class AddPostActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var previewImage: ImageView
    private lateinit var selectMediaButton: Button
    private lateinit var publishButton: Button
    private lateinit var db: FirebaseFirestore

    private var selectedMediaUri: Uri? = null
    private var selectedMediaType: String? = null

    companion object {
        private const val PICK_MEDIA_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        titleInput = findViewById(R.id.titleInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        previewImage = findViewById(R.id.previewImage)
        selectMediaButton = findViewById(R.id.selectMediaButton)
        publishButton = findViewById(R.id.publishButton)
        db = FirebaseFirestore.getInstance()

        selectMediaButton.setOnClickListener {
            pickMedia()
        }

        publishButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()


            if (title.isEmpty() || description.isEmpty() || selectedMediaUri == null) {
                Toast.makeText(this, "Veuillez remplir tous les champs et choisir un média", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadToCloudinary(selectedMediaUri!!)
        }
    }

    private fun pickMedia() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        startActivityForResult(intent, PICK_MEDIA_REQUEST)
    }

    private fun uploadToCloudinary(uri: Uri) {
        val file = File(getRealPathFromURI(uri))
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val preset = "ml_default"
        val presetBody = preset.toRequestBody("text/plain".toMediaTypeOrNull())

        RetrofitClient.instance.uploadImage("dmiizt4qx", body, presetBody)
            .enqueue(object : Callback<CloudinaryResponse> {
                override fun onResponse(call: Call<CloudinaryResponse>, response: Response<CloudinaryResponse>) {
                    if (response.isSuccessful) {
                        val url = response.body()?.secure_url
                        Toast.makeText(this@AddPostActivity, "Upload réussi : $url", Toast.LENGTH_LONG).show()

                        if (url != null) {
                            savePostToFirestore(
                                titleInput.text.toString().trim(),
                                descriptionInput.text.toString().trim(),
                                url
                            )
                        }
                    } else {
                        Toast.makeText(this@AddPostActivity, "Échec de l'upload", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<CloudinaryResponse>, t: Throwable) {
                    Toast.makeText(this@AddPostActivity, "Erreur : ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun savePostToFirestore(title: String, description: String, imageUrl: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return


        val post = hashMapOf(
            "title" to title,
            "description" to description,
            "imageUrl" to imageUrl,
            "userId" to userId,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(this, "Post publié avec succès !", Toast.LENGTH_SHORT).show()
                finish() // Revenir à la page précédente (ex: profil)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur lors de la publication", Toast.LENGTH_SHORT).show()
            }
    }


    private fun getRealPathFromURI(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val path = cursor?.getString(columnIndex!!)
        cursor?.close()
        return path ?: throw IllegalArgumentException("Chemin non trouvé")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_MEDIA_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedMediaUri = data.data

            val mimeType = contentResolver.getType(selectedMediaUri!!)
            selectedMediaType = when {
                mimeType?.startsWith("image") == true -> "image"
                mimeType?.startsWith("video") == true -> "video"
                else -> null
            }

            if (selectedMediaType == "image") {
                previewImage.setImageURI(selectedMediaUri)
            } else {
                previewImage.setImageResource(R.drawable.ic_video_placeholder)
            }
        }
    }
}

