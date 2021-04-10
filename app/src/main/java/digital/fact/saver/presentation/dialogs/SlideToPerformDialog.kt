package digital.fact.saver.presentation.dialogs

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import digital.fact.saver.R
import digital.fact.saver.databinding.DialogSlideToPerformBinding

class SlideToPerformDialog(
    private val title: String,
    private val description: String,
    private val warning: String? = null,
    private val onSliderFinishedListener: (() -> Unit)
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogSlideToPerformBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogSlideToPerformBinding.inflate(inflater, container, false)
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

    private fun setListeners() {
        binding.slider.apply {
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    when (progress) {
                        11 -> binding.deleteText.alpha = 0.9f
                        12 -> binding.deleteText.alpha = 0.8f
                        13 -> binding.deleteText.alpha = 0.7f
                        14 -> binding.deleteText.alpha = 0.6f
                        15 -> binding.deleteText.alpha = 0.5f
                        16 -> binding.deleteText.alpha = 0.4f
                        17 -> binding.deleteText.alpha = 0.3f
                        18 -> binding.deleteText.alpha = 0.2f
                        19 -> binding.deleteText.alpha = 0.1f
                        20 -> binding.deleteText.alpha = 0.0f
                    }
                    if (progress < 10) setProgress(10)
                    if (progress > 20) binding.deleteText.alpha = 0.0f
                    if (progress >= 91) {
                        onSliderFinishedListener.invoke()
                        this@SlideToPerformDialog.dismiss()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    if (seekBar != null) {
                        val animator = ValueAnimator.ofInt(seekBar.progress, 10)
                        animator.duration = 300L
                        animator.addUpdateListener {
                            seekBar.progress = it.animatedValue as Int
                        }
                        animator.start()
                    }
                }

            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

}