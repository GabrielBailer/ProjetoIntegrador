package com.example.app_pi2.utils

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreManager {

    val db by lazy {
        FirebaseFirestore.getInstance()
    }
}