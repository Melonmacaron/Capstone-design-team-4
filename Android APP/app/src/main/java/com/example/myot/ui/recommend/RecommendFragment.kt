package com.example.myot.ui.recommend

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.R
import com.example.myot.model.RecommendItem
import kotlinx.coroutines.launch

class RecommendFragment : Fragment() {

    private val viewModel: RecommendViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecommendAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_recommend, container, false)
        recyclerView = view.findViewById(R.id.recyclerRecommend)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = RecommendAdapter(emptyList()) { selectedItem ->
            val intent = Intent(requireContext(), RecommendResultActivity::class.java).apply {
                putExtra(RecommendResultActivity.EXTRA_RECOMMEND_ITEM, selectedItem)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        viewModel.recommendations.observe(viewLifecycleOwner) { items ->
            adapter.updateData(items)
        }

        view.findViewById<ImageView>(R.id.buttonAddRecommend).setOnClickListener {
            startActivity(Intent(requireContext(), RecommendCreateActivity::class.java))
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.loadRecommendations()
        }
    }
}
