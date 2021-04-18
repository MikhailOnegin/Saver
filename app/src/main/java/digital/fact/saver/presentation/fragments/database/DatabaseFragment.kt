package digital.fact.saver.presentation.fragments.database

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import digital.fact.saver.App
import digital.fact.saver.R
import digital.fact.saver.data.database.classes.MainDb
import digital.fact.saver.databinding.FragmentDatabaseBinding
import digital.fact.saver.presentation.activity.MainActivity
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.presentation.dialogs.ConfirmationDialog
import digital.fact.saver.utils.createSnackBar
import digital.fact.saver.utils.events.EventObserver
import java.text.SimpleDateFormat
import java.util.*

class DatabaseFragment : Fragment() {

    private lateinit var binding: FragmentDatabaseBinding
    private lateinit var databaseVM: DatabaseViewModel
    private lateinit var launcherForExportDB: ActivityResultLauncher<String>
    private lateinit var launcherForImportDB: ActivityResultLauncher<String>
    private lateinit var launcherForImportLegacyDB: ActivityResultLauncher<String>

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
        registerCallbacks()
    }

    private fun registerCallbacks() {
        launcherForExportDB = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            DatabaseResultCallback(binding) { requestPathForExportDatabase() }
        )
        launcherForImportDB = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            DatabaseResultCallback(binding) { requestPathForImportDatabase(REQUEST_IMPORT) }
        )
        launcherForImportLegacyDB = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            DatabaseResultCallback(binding) { requestPathForImportDatabase(REQUEST_IMPORT_LEGACY) }
        )
    }

    private fun requestPathForExportDatabase() {
        val fileName = "DATABASE (${getDateString()}).db"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        try {
            startActivityForResult(intent, REQUEST_EXPORT)
        } catch (exc: java.lang.Exception) {
            showNoFileManagerSnackbar()
        }
    }

    private fun requestPathForImportDatabase(requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(intent, requestCode)
        } catch (exc: java.lang.Exception) {
            showNoFileManagerSnackbar()
        }
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).hideBottomNavigationView()
        setListeners()
    }

    private fun checkPermissions(
        launcher: ActivityResultLauncher<String>,
        onPermissionGranted: () -> Unit
    ) {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> onPermissionGranted.invoke()
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                ConfirmationDialog(
                    title = getString(R.string.storagePermissionsDialogTitle),
                    message = getString(R.string.storagePermissionsDialogMessage),
                    positiveButtonText = getString(R.string.buttonGrant),
                    negativeButtonText = getString(R.string.buttonDeny),
                    onPositiveButtonClicked = {
                        launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                ).show(childFragmentManager, "rationale_dialog")
            }
            else -> launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    class DatabaseResultCallback(
        private val binding: FragmentDatabaseBinding,
        private val onPermissionGranted: () -> Unit
    ) : ActivityResultCallback<Boolean> {
        override fun onActivityResult(permissionGranted: Boolean) {
            if (permissionGranted) onPermissionGranted.invoke()
            else {

                createSnackBar(
                    anchorView = binding.root,
                    text = App.getInstance().getString(R.string.errorNoStoragePermission),
                    buttonText = App.getInstance().getString(R.string.buttonOk)
                ).show()
            }
        }
    }

    private fun getDateString(): String{
        val sdf = SimpleDateFormat("dd.MM.yyyy HH-mm-ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun setObservers() {
        databaseVM.importStateEvent.observe(viewLifecycleOwner, EventObserver(onImportStateEvent))
        databaseVM.exportStateEvent.observe(viewLifecycleOwner, EventObserver(onExportStateEvent))
    }

    private val onExportStateEvent: (DatabaseViewModel.State) -> Unit = {
        when (it) {
            DatabaseViewModel.State.WORKING -> {
                binding.progress.visibility = View.VISIBLE
            }
            DatabaseViewModel.State.ERROR -> {
                binding.progress.visibility = View.GONE
                createSnackBar(
                    anchorView = binding.root,
                    text = getString(R.string.databaseExportError),
                    buttonText = getString(R.string.buttonOk)
                ).show()
            }
            DatabaseViewModel.State.SUCCESS -> {
                binding.progress.visibility = View.GONE
                createSnackBar(
                    anchorView = binding.root,
                    text = getString(R.string.databaseExportSuccessful),
                    buttonText = getString(R.string.buttonOk)
                ).show()
            }
        }
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
            DatabaseViewModel.State.SUCCESS -> {
                binding.progress.visibility = View.GONE
                createSnackBar(
                    anchorView = binding.root,
                    text = getString(R.string.databaseImportSuccessful),
                    buttonText = getString(R.string.buttonOk)
                ).show()
            }
        }
    }

    private fun setListeners() {
        binding.run {
            toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
            buttonExport.setOnClickListener {
                checkPermissions(launcherForExportDB) { requestPathForExportDatabase() }
            }
            buttonImport.setOnClickListener {
                checkPermissions(launcherForImportDB) {
                    requestPathForImportDatabase(REQUEST_IMPORT)
                }
            }
            buttonImportLegacy.setOnClickListener {
                checkPermissions(launcherForImportLegacyDB) {
                    requestPathForImportDatabase(REQUEST_IMPORT_LEGACY)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_EXPORT -> {
                data?.data?.run {
                    val currentDb = requireActivity().getDatabasePath(MainDb.dbName)
                    databaseVM.exportDatabase(currentDb, this)
                }
            }
            REQUEST_IMPORT -> {
                data?.data?.run {
                    val currentDb = requireActivity().getDatabasePath(MainDb.dbName)
                    databaseVM.importDatabase(currentDb, this)
                }
            }
            REQUEST_IMPORT_LEGACY -> {
                data?.data?.run {
                    databaseVM.importLegacyDb(requireActivity(), this)
                }
            }
        }
    }

    private fun showNoFileManagerSnackbar() {
        createSnackBar(
            binding.root,
            getString(R.string.snackbarNoFileManager)
        ).show()
    }

    companion object {
        const val REQUEST_EXPORT = 123
        const val REQUEST_IMPORT = 234
        const val REQUEST_IMPORT_LEGACY = 356
    }

}