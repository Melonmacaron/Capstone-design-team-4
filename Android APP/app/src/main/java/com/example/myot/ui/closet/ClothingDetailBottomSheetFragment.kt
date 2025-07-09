package com.example.myot.ui.closet

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.data.repository.ClosetRepository
import com.example.myot.model.ClothingItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class ClothingDetailBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_ITEM = "clothing_item"

        fun newInstanceWithItem(
            item: ClothingItem,
            launcher: ActivityResultLauncher<Intent>,
            onDeleted: () -> Unit  // ✅ 콜백 추가
        ): ClothingDetailBottomSheetFragment {
            val fragment = ClothingDetailBottomSheetFragment()
            val args = Bundle()
            args.putParcelable(ARG_ITEM, item)
            fragment.arguments = args
            fragment.editLauncher = launcher
            fragment.onDeleted = onDeleted
            Log.d("ClothingDetail", "newInstanceWithItem: item 넘김 = $item")
            return fragment
        }
    }

    private lateinit var editLauncher: ActivityResultLauncher<Intent>
    private var onDeleted: (() -> Unit)? = null  // ✅ 삭제 콜백

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_clothing_detail_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val item = arguments?.getParcelable<ClothingItem>(ARG_ITEM)

        if (item == null) {
            Log.e("ClothingDetail", "❌ item == null! arguments = ${arguments}")
            dismiss()
            return
        }

        Log.d("ClothingDetail", "✅ 받은 item: $item")

        val image = view.findViewById<ImageView>(R.id.clothingImage)
        val textType = view.findViewById<TextView>(R.id.clothingType)
        val textColor = view.findViewById<TextView>(R.id.clothingColor)
        val textFit = view.findViewById<TextView>(R.id.clothingFit)
        val textLength = view.findViewById<TextView>(R.id.clothingLength)
        val textMaterial = view.findViewById<TextView>(R.id.clothingMaterial)
        val textSleeve = view.findViewById<TextView>(R.id.clothingSleeveLength)
        val textCollar = view.findViewById<TextView>(R.id.clothingCollar)

        val sleeveLayout = view.findViewById<LinearLayout>(R.id.sleeveLengthLayout)
        val collarLayout = view.findViewById<LinearLayout>(R.id.collarLayout)

        val buttonEdit = view.findViewById<ImageButton>(R.id.editInfoButton)
        val buttonDelete = view.findViewById<ImageButton>(R.id.deleteInfoButton)

        val imageUrl = item.imageUri?.replace("localhost", "10.0.2.2")

        if (imageUrl != null) {
            Glide.with(requireContext()).load(imageUrl).into(image)
        } else {
            image.setImageResource(item.imageResId)
        }

        textType.text = "${item.mainCategory} / ${item.category}"
        textColor.text = item.color
        textFit.text = item.fit
        textLength.text = item.length ?: "-"
        textMaterial.text = item.material
        textSleeve.text = item.sleeveLength ?: "-"
        textCollar.text = item.collar ?: "-"

        if (item.mainCategory == "상의" || item.mainCategory == "아우터") {
            sleeveLayout.visibility = View.VISIBLE
        } else {
            sleeveLayout.visibility = View.GONE
        }
        collarLayout.visibility = View.GONE
        buttonEdit.setOnClickListener {
            val intent = Intent(requireContext(), EditClothingActivity::class.java)
            intent.putExtra("clothingItem", item)
            editLauncher.launch(intent)
            dismiss()
        }

        buttonDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("삭제하시겠습니까?")
                .setMessage("이 옷을 삭제하면 되돌릴 수 없습니다.")
                .setPositiveButton("확인") { _, _ ->
                    lifecycleScope.launch {
                        val result = ClosetRepository.deleteClothing(item.id.toInt())
                        if (result) {
                            Toast.makeText(requireContext(), "삭제되었습니다", Toast.LENGTH_SHORT).show()
                            onDeleted?.invoke()  // ✅ 삭제 후 콜백 실행
                            dismiss()
                        } else {
                            Toast.makeText(requireContext(), "삭제에 실패했습니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        val behavior = dialog?.behavior
        behavior?.peekHeight = requireContext().resources.displayMetrics.heightPixels
        behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }
}
