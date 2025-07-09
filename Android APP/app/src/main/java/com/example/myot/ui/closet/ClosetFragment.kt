package com.example.myot.ui.closet

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.R
import com.example.myot.data.repository.ClosetRepository
import com.example.myot.model.ClothingItem
import com.example.myot.ui.home.RecentClothesAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class ClosetFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var clothesAdapter: RecentClothesAdapter
    private var isGrid = true

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            refreshClosetData()
        }
    }

    private val addLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            refreshClosetData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_closet, container, false)
        recyclerView = view.findViewById(R.id.recycler_closet)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addButton = view.findViewById<View>(R.id.addClothingButton)
        addButton.setOnClickListener {
            showAddOptionsPopup()
        }

        refreshClosetData()
    }

    private fun refreshClosetData() {
        lifecycleScope.launch {
            val clothes: List<ClothingItem> = ClosetRepository.getClothes()

            clothesAdapter = RecentClothesAdapter(clothes) { item ->
                val bottomSheet = ClothingDetailBottomSheetFragment.newInstanceWithItem(
                    item,
                    editLauncher,
                    onDeleted = { refreshClosetData() } // ✅ 삭제 후 새로고침
                )
                bottomSheet.show(parentFragmentManager, "ClothingDetail")
            }

            recyclerView.apply {
                layoutManager = GridLayoutManager(context, 3)
                adapter = clothesAdapter
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_closet, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.buttonToggleView -> {
                toggleViewType()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleViewType() {
        recyclerView.layoutManager =
            if (isGrid) LinearLayoutManager(context)
            else GridLayoutManager(context, 3)
        isGrid = !isGrid
    }

    override fun onResume() {
        super.onResume()
        if (::clothesAdapter.isInitialized) {
            clothesAdapter.notifyDataSetChanged()
        }
    }

    private val selectGalleryImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val intent = Intent(requireContext(), AddClothingActivity::class.java)
            intent.putExtra("galleryImageUri", it)
            addLauncher.launch(intent)
        }
    }

    private fun showAddOptionsPopup() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(R.layout.dialog_add_clothing_options, null)
        dialog.setContentView(sheetView)

        val takePhoto = sheetView.findViewById<TextView>(R.id.optionTakePhoto)
        val choosePhoto = sheetView.findViewById<TextView>(R.id.optionChooseFromGallery)

        takePhoto.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(requireContext(), AddClothingActivity::class.java)
            intent.putExtra("startCamera", true)
            addLauncher.launch(intent)
        }

        choosePhoto.setOnClickListener {
            dialog.dismiss()
            selectGalleryImageLauncher.launch("image/*")
        }

        dialog.show()
    }
}
