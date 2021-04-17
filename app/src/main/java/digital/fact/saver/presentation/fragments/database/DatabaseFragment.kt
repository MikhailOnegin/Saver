package digital.fact.saver.presentation.fragments.database

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentDatabaseBinding
import digital.fact.saver.presentation.activity.MainActivity
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.utils.createSnackBar
import digital.fact.saver.utils.events.EventObserver

class DatabaseFragment : Fragment() {

    //sergeev: Настроить работу с разрешениями.
    private lateinit var binding: FragmentDatabaseBinding
    private lateinit var databaseVM: DatabaseViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentDatabaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainVM = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val factory = DatabaseViewModel.DatabaseVMFactory(mainVM)
        databaseVM = ViewModelProvider(this, factory)[DatabaseViewModel::class.java]
        setObservers()
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).hideBottomNavigationView()
        setListeners()
    }

    private fun setObservers() {
        databaseVM.importStateEvent.observe(viewLifecycleOwner, EventObserver(onImportStateEvent))
    }

    private val onImportStateEvent: (DatabaseViewModel.State) -> Unit = {
        when (it) {
            DatabaseViewModel.State.WORKING -> {
                binding.progress.visibility = View.VISIBLE
            }
            DatabaseViewModel.State.ERROR -> {
                binding.progress.visibility = View.GONE
                createSnackBar(
                        anchorView = binding.root,
                        text = getString(R.string.databaseImportError),
                        buttonText = getString(R.string.buttonOk)
                ).show()
            }
            DatabaseViewModel.State.DONE -> {
                binding.progress.visibility = View.GONE
                createSnackBar(
                        anchorView = binding.root,
                        text = getString(R.string.databaseImportSuccessful),
                        buttonText = getString(R.string.buttonOk),
                        onButtonClicked = { findNavController().popBackStack() }
                ).show()
            }
        }
    }

    private fun setListeners() {
        binding.run {
            toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
            buttonImportLegacyDb.setOnClickListener { onImportLegacyBdButtonClicked() }
        }
    }

    private fun onImportLegacyBdButtonClicked() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(intent, REQUEST_LEGACY_DATABASE_PATH)
        } catch (exc: Exception) {
            createSnackBar(
                    binding.root,
                    getString(R.string.snackbarNoFileManager)
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_LEGACY_DATABASE_PATH -> {
                data?.data?.run {
                    databaseVM.copyLegacyDb(requireActivity(), this)
                }
            }
        }
    }

    companion object {
        const val REQUEST_LEGACY_DATABASE_PATH = 321
    }

}