package com.example.myot.ui.image

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.data.repository.ImageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageFragment : Fragment() {

    private lateinit var imagePreview: ImageView
    private lateinit var addImageButton: ImageView
    private var selectedImageUri: Uri? = null
    private var defaultImageUrl: String? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            imagePreview.setImageURI(it)  // UI에 먼저 반영

            CoroutineScope(Dispatchers.IO).launch {
                val response = ImageRepository.uploadUserImage(requireContext(), it)
                withContext(Dispatchers.Main) {
                    response?.let { res ->
                        defaultImageUrl = res.image_url
                        Glide.with(this@ImageFragment)
                            .load(res.image_url)
                            .placeholder(R.drawable.image_placeholder)
                            .error(R.drawable.image_placeholder)
                            .into(imagePreview)
                    } ?: Log.w("ImageFragment", "서버 업로드 실패")
                }
            }
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imagePreview = view.findViewById(R.id.imagePreview)
        addImageButton = view.findViewById(R.id.buttonAddImage)

        imagePreview.setOnClickListener {
            openGallery()
        }

        addImageButton.setOnClickListener {
            val intent = Intent(requireContext(), SynthesisActivity::class.java)

            defaultImageUrl?.let {
                intent.putExtra("defaultImageUrl", it)
            }

            startActivity(intent)
        }

        loadDefaultImage()
    }

    private fun openGallery() {
        imagePickerLauncher.launch("image/*")
    }

    private fun loadDefaultImage() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = ImageRepository.fetchRecentImage()
            withContext(Dispatchers.Main) {
                response?.let {
                    defaultImageUrl = it.image_url
                    Log.d("ImageFragment", "서버에서 받은 기본 이미지 URL: ${it.image_url}")

                    Glide.with(this@ImageFragment)
                        .load(it.image_url)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .into(imagePreview)
                } ?: Log.w("ImageFragment", "기본 이미지 불러오기 실패: 서버 응답이 null")
            }
        }
    }
}
