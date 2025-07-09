package com.example.myot.ui.recommend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.model.RecommendItem

class RecommendAdapter(
    private var items: List<RecommendItem>,
    private val onItemClick: (RecommendItem) -> Unit
) : RecyclerView.Adapter<RecommendAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.recommendImageContainer)
        val textDescription: TextView = view.findViewById(R.id.recommendText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommend, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.textDescription.text = item.description
        Glide.with(context)
            .load(item.imageUrl)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    // ✅ 리스트 갱신용 함수 추가
    fun updateData(newItems: List<RecommendItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}
