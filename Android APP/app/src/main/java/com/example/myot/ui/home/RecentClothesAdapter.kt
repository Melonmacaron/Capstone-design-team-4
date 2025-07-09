package com.example.myot.ui.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.model.ClothingItem

class RecentClothesAdapter(
    private val items: List<ClothingItem>,
    private val onItemClick: (ClothingItem) -> Unit
) : RecyclerView.Adapter<RecentClothesAdapter.ClothesViewHolder>() {

    class ClothesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.clothesImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClothesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_clothes_square, parent, false)
        return ClothesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClothesViewHolder, position: Int) {
        val item = items[position]

        if (item.imageUri != null) {
            Glide.with(holder.itemView.context)
                .load(item.imageUri)
                .placeholder(R.drawable.default_clothes)
                .into(holder.imageView)
        } else if (item.imageResId != -1) {
            holder.imageView.setImageResource(item.imageResId)
        } else {
            holder.imageView.setImageResource(R.drawable.default_clothes)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
