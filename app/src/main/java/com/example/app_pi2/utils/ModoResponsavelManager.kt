package com.example.app_pi2.utils

import android.content.Context

object ModoResponsavelManager {

    private const val PREF = "config"
    private const val KEY = "modo_responsavel"

    fun isAtivo(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF, 0)
        return prefs.getBoolean(KEY, false)
    }

    fun setAtivo(context: Context, ativo: Boolean) {
        val prefs = context.getSharedPreferences(PREF, 0)
        prefs.edit().putBoolean(KEY, ativo).apply()
    }
}