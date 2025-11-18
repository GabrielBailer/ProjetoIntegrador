package com.example.app_pi2

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.app_pi2.databinding.FragmentInteracaoDialogBinding
import java.util.*

class InteracaoDialogFragment : DialogFragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentInteracaoDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var tts: TextToSpeech
    private var titulo: String? = null
    private var imagem: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        titulo = arguments?.getString("titulo")
        imagem = arguments?.getString("imagem")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInteracaoDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tts = TextToSpeech(requireContext(), this)

        binding.tvTituloDialog.text = titulo ?: "Interação"

        if (!imagem.isNullOrEmpty()) {
            val resId = resources.getIdentifier(imagem, "drawable", requireContext().packageName)
            if (resId != 0) {
                binding.imgInteracao.setImageResource(resId)
            } else {
                binding.imgInteracao.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }

        binding.root.setOnClickListener { dismiss() }

        binding.btnVoltar.setOnClickListener { dismiss() }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("pt", "BR")
            titulo?.let {
                tts.speak(it, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        tts.stop()
        tts.shutdown()
    }

    companion object {
        fun newInstance(titulo: String, imagem: String?): InteracaoDialogFragment {
            val fragment = InteracaoDialogFragment()
            val args = Bundle().apply {
                putString("titulo", titulo)
                putString("imagem", imagem)
            }
            fragment.arguments = args
            return fragment
        }
    }
}