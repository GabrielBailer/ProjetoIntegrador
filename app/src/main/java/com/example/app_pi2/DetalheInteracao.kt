package com.example.app_pi2

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.app_pi2.databinding.ItemInteracaoBinding
import java.util.*

class DetalheInteracao : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ItemInteracaoBinding
    private lateinit var tts: TextToSpeech
    private var titulo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa View Binding
        binding = ItemInteracaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa TextToSpeech
        tts = TextToSpeech(this, this)

        // Recebe dados da Intent
        titulo = intent.getStringExtra("TITULO")
        val nomeImagem = intent.getStringExtra("IMAGEM_URL") // apenas o nome do drawable

        // Preenche imagem
        if (!nomeImagem.isNullOrEmpty()) {
            val resId = resources.getIdentifier(nomeImagem, "drawable", packageName)
            binding.imgMiniatura.setImageResource(
                if (resId != 0) resId else R.drawable.ic_launcher_foreground
            )
        } else {
            binding.imgMiniatura.setImageResource(R.drawable.ic_launcher_foreground)
        }

        // Preenche título
        titulo?.let {
            binding.tvTitulo.setText(it)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.forLanguageTag("pt-BR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "A linguagem especificada não é suportada.")
            } else {
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
