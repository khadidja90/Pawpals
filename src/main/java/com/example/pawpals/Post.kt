package com.example.pawpals

import java.util.Date

data class Post(
    val title: String = "",
    val description: String = "",
    val timestamp: Long? = null,
    val imageUrl: String = ""

)
