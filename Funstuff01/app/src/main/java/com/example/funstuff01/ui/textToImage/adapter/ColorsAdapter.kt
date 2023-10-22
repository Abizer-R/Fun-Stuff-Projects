package com.example.funstuff01.ui.textToImage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.funstuff01.databinding.TextColorListItemBinding

class ColorsAdapter(
    private val listener: ColorsClickListener
) : ListAdapter<Int, ColorsAdapter.ColorViewHolder>(
    object : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
        override fun areContentsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem

    })
{

    inner class ColorViewHolder(private val binding: TextColorListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(@ColorRes colorRes: Int) {
            with(binding) {
                bgView.setBackgroundColor(
                    ContextCompat.getColor(bgView.context, colorRes)
                )

                root.setOnClickListener {
                    listener.onItemClick(colorRes)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return ColorViewHolder(
            TextColorListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    interface ColorsClickListener {
        fun onItemClick(@ColorRes colorRes: Int)
    }
}