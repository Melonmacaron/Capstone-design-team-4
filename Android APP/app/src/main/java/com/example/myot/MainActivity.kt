package com.example.myot

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myot.data.remote.ApiClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.findNavController()
            ?: throw IllegalStateException("NavController not found")

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        // ✅ 서버 연결 상태 확인
        checkServerConnection()
    }

    private fun checkServerConnection() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.ping()
                if (response.isSuccessful && response.body()?.status == "ok") {
                    Toast.makeText(this@MainActivity, "서버 연결 성공", Toast.LENGTH_SHORT).show()
                } else {
                    showServerError()
                }
            } catch (e: Exception) {
                showServerError()
            }
        }
    }

    private fun showServerError() {
        AlertDialog.Builder(this)
            .setTitle("서버 연결 실패")
            .setMessage("서버에 연결할 수 없습니다. 네트워크나 서버 상태를 확인해주세요.")
            .setPositiveButton("확인") { _, _ ->
                finish() // 앱 종료
            }
            .setCancelable(false) // 백버튼 방지 (선택 사항)
            .show()
    }


    // ✅ RecommendResultActivity에서 돌아올 때 선택 탭 처리
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val navId = intent.getIntExtra("navigateTo", -1)
        if (navId != -1) {
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.selectedItemId = navId
        }
    }
}
