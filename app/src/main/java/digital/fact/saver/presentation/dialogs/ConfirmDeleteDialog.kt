package digital.fact.saver.presentation.dialogs

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

class ConfirmDeleteDialog(private val wallet: Sources) : BottomSheetDialogFragment() {

    private lateinit var binding: LayoutDialogDeleteBinding
    private lateinit var sourcesVM: SourcesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutDialogDeleteBinding.inflate(inflater, container, false)
        setListeners()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sourcesVM = ViewModelProvider(requireActivity())[SourcesViewModel::class.java]
    }

    private fun setListeners() {
        binding.slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress == 100) deleteSource()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress = 0
            }

        })
    }

    private fun deleteSource() {
        sourcesVM.deleteSource(
            Source(
                _id = wallet.id,
                name = wallet.name,
                type = wallet.type,
                start_sum = wallet.startSum,
                adding_date = wallet.addingDate,
                sort_order = wallet.sortOrder,
                visibility = wallet.visibility,
            )
        )
        findNavController().popBackStack()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

}