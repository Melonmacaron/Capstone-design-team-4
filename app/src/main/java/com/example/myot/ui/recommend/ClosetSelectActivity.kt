package com.example.myot.ui.recommend

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.R
import com.example.myot.data.repository.ClosetRepository
import com.example.myot.model.ClothingItem
import com.example.myot.ui.home.RecentClothesAdapter
import kotlinx.coroutines.launch

class ClosetSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_closet_select)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerClosetSelect)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        lifecycleScope.launch {
            val clothesList = ClosetRepository.getClothes()

            val adapter = RecentClothesAdapter(clothesList) { selectedItem: ClothingItem ->
                val resultIntent = Intent().apply {
                    putExtra("selected_item", selectedItem)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }

            recyclerView.adapter = adapter
        }
    }
}
