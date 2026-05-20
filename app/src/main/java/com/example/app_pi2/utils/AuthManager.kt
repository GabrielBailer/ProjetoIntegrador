package com.example.app_pi2.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object AuthManager {

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    fun isLogged(): Boolean {
        return auth.currentUser != null
    }

    fun logout() {
        auth.signOut()
    }
}