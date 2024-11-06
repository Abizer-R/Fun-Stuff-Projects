package com.example.backgroundaudio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.RenderMode
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.backgroundaudio.SelectAudioBottomSheet.Companion.UPDATE_LOADING_STATE
import com.example.backgroundaudio.SelectAudioBottomSheet.Companion.UPDATE_PLAY_PAUSE
import com.example.backgroundaudio.SelectAudioBottomSheet.Companion.UPDATE_SELECTED_STATE
import com.example.backgroundaudio.databinding.AudioListItemBinding
import com.example.backgroundaudio.databinding.UploadFromStorageListItemBinding

class AudioAdapter(
    private val listener: AudioClickListener
) : ListAdapter<AudioItemModel, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<AudioItemModel>() {
        override fun areItemsTheSame(oldItem: AudioItemModel, newItem: AudioItemModel): Boolean {
            return oldItem.index == newItem.index
        }

        override fun areContentsTheSame(oldItem: AudioItemModel, newItem: AudioItemModel): Boolean {
            return oldItem.index == newItem.index && oldItem.name == newItem.name &&
                    oldItem.url == newItem.url && oldItem.icon == newItem.icon
        }


    }
) {

    final val AUDIO_TYPE = 1
    final val UPLOAD_STORAGE_TYPE = 2

    inner class AudioViewHolder(private val binding: AudioListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(audioItem: AudioItemModel) {
            with(binding) {
                // Define initial State of all components to avoid inconsistencies
                audioTitle.text = audioItem.name
                Glide.with(audioIcon.context)
                    .load(audioItem.icon)
                    .placeholder(R.drawable.ic_music_card)
                    .error(R.drawable.ic_music_card)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(audioIcon)

                lottiePlayingAnim.apply {
                    setRenderMode(RenderMode.HARDWARE)
                    setAnimation(R.raw.sound_wave_one)
                    repeatCount = LottieDrawable.INFINITE
                    speed = 2F
                }

                updatePlayPause(audioItem)
                updateLoadingState(audioItem)
                updateSelectedState(audioItem)

                clRoot.setOnClickListener {
                    listener.onAudioItemClicked(absoluteAdapterPosition, currentList[absoluteAdapterPosition])
                }
                imgPlayPause.setOnClickListener {
                    listener.onPlayPauseClicked(
                        absoluteAdapterPosition, currentList[absoluteAdapterPosition]
                    )
                }
            }
        }

        private fun updatePlayPause(audioItem: AudioItemModel) {
            if(audioItem.isPlayingCurrently) {
                binding.imgPlayPause.setImageResource(R.drawable.ic_pause_white_36dp)
                binding.audioIcon.isVisible = false
                binding.lottiePlayingAnim.isVisible = true
                binding.lottiePlayingAnim.playAnimation()
            } else {
                binding.imgPlayPause.setImageResource(R.drawable.ic_play_arrow_white_36dp)
                binding.audioIcon.isVisible = true
                binding.lottiePlayingAnim.isVisible = false
                binding.lottiePlayingAnim.pauseAnimation()
            }
        }

        private fun updateLoadingState(audioItem: AudioItemModel) {
            binding.progressBar.isVisible = audioItem.isLoading
            binding.imgPlayPause.isVisible = audioItem.isLoading.not()
        }

        private fun updateSelectedState(audioItem: AudioItemModel) {
            with (binding.cvRoot) {
                setBackgroundColor(
                    context.getColor(
                        if (audioItem.isSelected) R.color.color_selected else R.color.dark_panel
                    )
                )
                strokeWidth = if (audioItem.isSelected) 2 else 0
                strokeColor = context.getColor(R.color.colorAccent)
            }
        }

        fun handlePayloads(
            payloads: MutableList<Any>,
            audioItem: AudioItemModel
        ) {
            for(payload in payloads) {
                when(payload) {
                    UPDATE_PLAY_PAUSE -> {
                        updatePlayPause(audioItem)
                    }

                    UPDATE_LOADING_STATE -> {
                        updateLoadingState(audioItem)
                    }

                    UPDATE_SELECTED_STATE -> {
                        updateSelectedState(audioItem)
                    }
                }
            }
        }
    }

    inner class UploadFromStorageViewHolder(private val binding: UploadFromStorageListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(audioItem: AudioItemModel) {
            with(binding) {

                clRoot.setOnClickListener {
                    if (audioItem.isSelected) {
                        listener.onAudioItemClicked(absoluteAdapterPosition, currentList[absoluteAdapterPosition])
                    } else {
                        listener.onUploadFromStorage()
                    }
                }


                updateSelectedState(audioItem)
            }
        }

        private fun updateSelectedState(audioItem: AudioItemModel) {
            if (audioItem.isSelected) {
                handleAudioSelected(audioItem)
            } else {
                handleAudioNotSelected()
            }
            with (binding.cvRoot) {
                setBackgroundColor(
                    context.getColor(
                        if (audioItem.isSelected) R.color.color_selected else R.color.dark_panel
                    )
                )
                strokeWidth = if (audioItem.isSelected) 2 else 0
                strokeColor = context.getColor(R.color.colorAccent)
            }
        }

        private fun handleAudioNotSelected() {
            binding.groupUploadFromStorage.isVisible = true
            binding.groupAudioDetails.isVisible = false
        }

        private fun handleAudioSelected(audioItem: AudioItemModel) {
            with(binding) {
                groupUploadFromStorage.isVisible = false
                groupAudioDetails.isVisible = true

                // Define initial State of all components to avoid inconsistencies
                audioTitle.text = audioItem.name

                lottiePlayingAnim.apply {
                    setRenderMode(RenderMode.HARDWARE)
                    setAnimation(R.raw.sound_wave_one)
                    repeatCount = LottieDrawable.INFINITE
                    speed = 2F
                }

                updatePlayPause(audioItem)
                updateLoadingState(audioItem)

                imgPlayPause.setOnClickListener {
                    listener.onPlayPauseClicked(
                        absoluteAdapterPosition, currentList[absoluteAdapterPosition]
                    )
                }
            }
        }

        private fun updatePlayPause(audioItem: AudioItemModel) {
            if(audioItem.isPlayingCurrently) {
                binding.imgPlayPause.setImageResource(R.drawable.ic_pause_white_36dp)
                binding.audioIcon.isVisible = false
                binding.lottiePlayingAnim.isVisible = true
                binding.lottiePlayingAnim.playAnimation()
            } else {
                binding.imgPlayPause.setImageResource(R.drawable.ic_play_arrow_white_36dp)
                binding.audioIcon.isVisible = true
                binding.lottiePlayingAnim.isVisible = false
                binding.lottiePlayingAnim.pauseAnimation()
            }
        }

        private fun updateLoadingState(audioItem: AudioItemModel) {
            binding.progressBar.isVisible = audioItem.isLoading
            binding.imgPlayPause.isVisible = audioItem.isLoading.not()
        }

        fun handlePayloads(
            payloads: MutableList<Any>,
            audioItem: AudioItemModel
        ) {
            for(payload in payloads) {
                when(payload) {
                    UPDATE_PLAY_PAUSE -> {
                        updatePlayPause(audioItem)
                    }

                    UPDATE_LOADING_STATE -> {
                        updateLoadingState(audioItem)
                    }

                    UPDATE_SELECTED_STATE -> {
                        updateSelectedState(audioItem)
                    }
                }
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position].itemType) {
            AudioItemType.AUDIO_FILE -> AUDIO_TYPE
            AudioItemType.UPLOAD_FROM_DEVICE -> UPLOAD_STORAGE_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            AUDIO_TYPE -> AudioViewHolder(
                AudioListItemBinding.inflate(layoutInflater, parent, false)
            )
            else -> UploadFromStorageViewHolder(
                UploadFromStorageListItemBinding.inflate(layoutInflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            when (holder) {
                is AudioViewHolder -> {
                    holder.handlePayloads(payloads, currentList[position])
                }
                is UploadFromStorageViewHolder -> {
                    holder.handlePayloads(payloads, currentList[position])
                }
            }
        }

    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is AudioViewHolder ->{
                holder.bind(currentList[position])
            }
            is UploadFromStorageViewHolder -> {
                holder.bind(currentList[position])
            }
        }
    }

    interface AudioClickListener {
        fun onAudioItemClicked(position: Int, audioItem: AudioItemModel)
        fun onPlayPauseClicked(position: Int, audioItem: AudioItemModel)
        fun onUploadFromStorage()
    }
}