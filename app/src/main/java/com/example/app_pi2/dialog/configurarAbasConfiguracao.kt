package com.example.app_pi2.dialog

import android.graphics.Typeface
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.app_pi2.R

fun configurarAbasConfiguracao(view: View) {

    val tabAparencia = view.findViewById<TextView>(R.id.tabAparencia)
    val tabSeguranca = view.findViewById<TextView>(R.id.tabSeguranca)
    val indicadorTab = view.findViewById<View>(R.id.indicadorTab)

    val cardModoEscuro = view.findViewById<LinearLayout>(R.id.cardModoEscuro)
    val cardModoResponsavel = view.findViewById<LinearLayout>(R.id.cardModoResponsavel)
    val cardFala = view.findViewById<LinearLayout>(R.id.cardFalaDireta)

    val tvDescricaoConfiguracao = view.findViewById<TextView>(R.id.tvDescricaoConfiguracao)

    val corAtiva = ContextCompat.getColor(view.context, R.color.text_primary)
    val corInativa = ContextCompat.getColor(view.context, R.color.text_secondary)

    fun selecionarAbaAparencia() {
        tabAparencia.setTextColor(corAtiva)
        tabAparencia.setTypeface(null, Typeface.BOLD)
        tabSeguranca.setTextColor(corInativa)
        tabSeguranca.setTypeface(null, Typeface.NORMAL)

        cardModoEscuro.visibility = View.VISIBLE
        cardFala.visibility = View.VISIBLE
        cardModoResponsavel.visibility = View.GONE

        tvDescricaoConfiguracao.text = "O modo escuro reduz o brilho da tela e pode ajudar a diminuir o cansaço visual, especialmente em ambientes com pouca luz."

        indicadorTab.post {
            indicadorTab.animate().x(tabAparencia.x).setDuration(200).start()
            val params = indicadorTab.layoutParams
            params.width = tabAparencia.width
            indicadorTab.layoutParams = params
        }
    }

    fun selecionarAbaSeguranca() {
        tabSeguranca.setTextColor(corAtiva)
        tabSeguranca.setTypeface(null, Typeface.BOLD)
        tabAparencia.setTextColor(corInativa)
        tabAparencia.setTypeface(null, Typeface.NORMAL)

        cardModoEscuro.visibility = View.GONE
        cardFala.visibility = View.GONE
        cardModoResponsavel.visibility = View.VISIBLE

        tvDescricaoConfiguracao.text = "O modo responsável exige a senha do responsável para autorizar a inclusão de novas interações e o modo edição."

        indicadorTab.post {
            indicadorTab.animate().x(tabSeguranca.x).setDuration(200).start()
            val params = indicadorTab.layoutParams
            params.width = tabSeguranca.width
            indicadorTab.layoutParams = params
        }
    }

    tabAparencia.setOnClickListener { selecionarAbaAparencia() }
    tabSeguranca.setOnClickListener { selecionarAbaSeguranca() }

    selecionarAbaAparencia()
}