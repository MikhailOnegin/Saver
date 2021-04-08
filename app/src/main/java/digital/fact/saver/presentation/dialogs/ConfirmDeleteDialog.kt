package digital.fact.saver.presentation.dialogs

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import digital.fact.saver.R
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.databinding.LayoutDialogDeleteBinding
import digital.fact.saver.domain.models.Sources
import digital.fact.saver.presentation.viewmodels.SourcesViewModel

class ConfirmDeleteDialog(
    private val title: String,
    private val description: String,
    private val warning: String? = null,
    private val popBack: Boolean = true,
    private val onSliderFinishedListener: (() -> Unit)
) : BottomSheetDialogFragment() {

    private lateinit var binding: LayoutDialogDeleteBinding
    private lateinit var sourcesVM: SourcesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutDialogDeleteBinding.inflate(inflater, container, false)
        setDataToDialog()
        setListeners()
        return binding.root
    }

    private fun setDataToDialog() {
        binding.apply {
            dialogTitle.text = title
            dialogDescription.text = description
            if (!warning.isNullOrEmpty()) {
                dialogWarning.visibility = View.VISIBLE
                dialogWarning.text = warning
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sourcesVM = ViewModelProvider(requireActivity())[SourcesViewModel::class.java]
    }

    private fun setListeners() {
        binding.slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress == 100) {
                    onSliderFinishedListener.invoke()
                    this@ConfirmDeleteDialog.dismiss()
                    if (popBack) findNavController().popBackStack()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    val animator = ValueAnimator.ofInt(seekBar.progress, 0)
                    animator.duration = 300L
                    animator.addUpdateListener {
                        seekBar.progress = it.animatedValue as Int
                    }
                    animator.start()
                }
            }

        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

}