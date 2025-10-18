package com.example.app_pi2

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.util.*

class DetalheInteracaoActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var titulo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_interacao)

        tts = TextToSpeech(this, this)

        val imgDetalheInteracao: ImageView = findViewById(R.id.imgDetalheInteracao)
        val tvDetalheDescricao: TextView = findViewById(R.id.tvDetalheDescricao)

        titulo = intent.getStringExtra("TITULO")
        val descricao = intent.getStringExtra("DESCRICAO")
        val imagemUrl = intent.getStringExtra("IMAGEM_URL")

        tvDetalheDescricao.text = descricao

        if (!imagemUrl.isNullOrEmpty()) {
            if (imagemUrl.startsWith("http")) {
                Glide.with(this)
                    .load(imagemUrl)
                    .into(imgDetalheInteracao)
            } else {
                val resId = resources.getIdentifier(imagemUrl, "drawable", packageName)
                if (resId != 0) {
                    imgDetalheInteracao.setImageResource(resId)
                } else {
                    imgDetalheInteracao.setImageResource(R.drawable.ic_launcher_foreground) // Imagem padrão
                }
            }
        } else {
            imgDetalheInteracao.setImageResource(R.drawable.ic_launcher_foreground) // Imagem padrão
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("pt", "BR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "A linguagem especificada não é suportada.")
            } else {
                // Falar o título assim que o TTS estiver pronto
                titulo?.let { speakOut(it) }
            }
        } else {
            Log.e("TTS", "Falha na inicialização do TextToSpeech.")
        }
    }

    private fun speakOut(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onDestroy() {
        if (this::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
