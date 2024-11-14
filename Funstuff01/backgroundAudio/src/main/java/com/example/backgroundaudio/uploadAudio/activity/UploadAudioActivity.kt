package com.example.backgroundaudio.uploadAudio.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.commitNow
import com.example.backgroundaudio.R
import com.example.backgroundaudio.audioList.fragment.parent.AudioListParentFragment
import com.example.backgroundaudio.databinding.ActivityUploadAudioBinding
import com.example.backgroundaudio.uploadAudio.fragment.UploadAudioFragment
import com.example.backgroundaudio.uploadAudio.fragment.UploadAudioFragment.Companion.AUDIO_FILE_NAME
import com.example.backgroundaudio.uploadAudio.fragment.UploadAudioFragment.Companion.AUDIO_FILE_PATH
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadAudioActivity : AppCompatActivity() {

    companion object {

        fun startActivity(
            context: Context,
            audioFilePath: String,
            audioFileName: String? = null
        ) {
            val intent = Intent(context, UploadAudioActivity::class.java).apply {
                putExtra(AUDIO_FILE_PATH, audioFilePath)
                putExtra(AUDIO_FILE_NAME, audioFileName)
            }
            context.startActivity(intent)
        }
    }

    private var _binding: ActivityUploadAudioBinding? = null
    private val binding: ActivityUploadAudioBinding get() = requireNotNull(_binding)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityUploadAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        attachFragment()
    }

    private fun setupToolbar() {
        with(binding.toolbar) {
            navigationIcon = ContextCompat.getDrawable(this@UploadAudioActivity, R.drawable.ic_arrow_back)
            navigationIcon?.setTint(ContextCompat.getColor(this@UploadAudioActivity, R.color.colorOnBackground))
            setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun attachFragment() {
        supportFragmentManager.commitNow {
            replace(
                binding.fragmentContainer.id,
                UploadAudioFragment.newInstance(
                    intent.getStringExtra(AUDIO_FILE_PATH) ?: "",
                    intent.getStringExtra(AUDIO_FILE_NAME)
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}