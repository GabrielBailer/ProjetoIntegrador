package com.example.app_pi2.utils

import android.content.Context

object FalaAutomaticaManager {

    private const val PREFS_NAME = "configuracoes_app"
    private const val KEY_FALA_AUTOMATICA = "fala_automatica"

    fun isAtivo(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FALA_AUTOMATICA, false)
    }

    fun setAtivo(context: Context, ativo: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        prefs.edit()
            .putBoolean(KEY_FALA_AUTOMATICA, ativo)
            .apply()
    }
}