package com.example.myot.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.data.remote.ApiClient
import com.example.myot.data.repository.ClosetRepository
import com.example.myot.model.ClothingItem
import com.example.myot.model.WeatherResponse
import com.example.myot.ui.closet.ClothingDetailBottomSheetFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recentClothesAdapter: RecentClothesAdapter

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            refreshRecentClothes()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 날씨 정보 View 참조
        val imageWeatherIcon = view.findViewById<ImageView>(R.id.image_weather_icon)
        val textWeather = view.findViewById<TextView>(R.id.text_weather)
        val textTemperature = view.findViewById<TextView>(R.id.text_temperature)
        val textHumidity = view.findViewById<TextView>(R.id.text_humidity)

        // 날씨 정보 요청 및 표시
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getWeather()
                if (response.isSuccessful) {
                    val weather = response.body()
                    weather?.let {
                        val iconUrl = "https://openweathermap.org/img/wn/${it.icon}@2x.png"
                        Glide.with(requireContext()).load(iconUrl).into(imageWeatherIcon)
                        textWeather.text = it.weather
                        textTemperature.text = "${it.temperature}℃"
                        textHumidity.text = "${it.humidity}%"
                    }
                } else {
                    Toast.makeText(requireContext(), "날씨 정보를 불러오지 못했습니다", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "날씨 로딩 오류", Toast.LENGTH_SHORT).show()
            }
        }

        // RecyclerView 설정
        recyclerView = view.findViewById(R.id.recent_clothes_recycler)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        refreshRecentClothes()
    }

    private fun refreshRecentClothes() {
        lifecycleScope.launch {
            val recentClothes: List<ClothingItem> = withContext(Dispatchers.IO) {
                ClosetRepository.getFrequentClothes()
            }

            recentClothesAdapter = RecentClothesAdapter(recentClothes) { item ->
                val detailSheet = ClothingDetailBottomSheetFragment.newInstanceWithItem(
                    item,
                    editLauncher,
                    onDeleted = { refreshRecentClothes() }
                )
                detailSheet.show(parentFragmentManager, "ClothingDetail")
            }

            recyclerView.adapter = recentClothesAdapter
        }
    }
}
