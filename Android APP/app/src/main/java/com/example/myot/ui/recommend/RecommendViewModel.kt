package com.example.myot.ui.recommend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myot.model.RecommendItem
import com.example.myot.data.repository.RecommendRepository
import kotlinx.coroutines.launch

class RecommendViewModel : ViewModel() {

    private val _recommendations = MutableLiveData<List<RecommendItem>>()
    val recommendations: LiveData<List<RecommendItem>> = _recommendations

    fun loadRecommendations() {
        viewModelScope.launch {
            val result = RecommendRepository.getRecommendHistory()
            _recommendations.value = result
        }
    }
}
