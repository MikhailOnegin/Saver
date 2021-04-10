package digital.fact.saver.presentation.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import digital.fact.saver.R
import digital.fact.saver.databinding.DialogConfirmationBinding

class ConfirmationDialog(
        private val title: String,
        private val message: String,
        private val positiveButtonText: String? = null,
        private val negativeButtonText: String? = null,
        private val onPositiveButtonClicked: () -> Unit,
        private val onNegativeButtonClicked: (() -> Unit)? = null
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogConfirmationBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DialogConfirmationBinding.inflate(inflater, container, false)
        binding.run {
            title.text = this@ConfirmationDialog.title
            message.text = this@ConfirmationDialog.message
            if (positiveButtonText != null) buttonPositive.text = positiveButtonText
            if (negativeButtonText != null) buttonNegative.text = negativeButtonText
            buttonPositive.setOnClickListener {
                onPositiveButtonClicked.invoke()
                dismiss()
            }
            buttonNegative.setOnClickListener {
                onNegativeButtonClicked?.invoke()
                dismiss()
            }
        }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

}