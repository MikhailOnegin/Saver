package digital.fact.saver.presentation.fragments.bank

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import digital.fact.saver.domain.models.*
import digital.fact.saver.presentation.viewmodels.TestDatabaseViewModel

class BanksFragment : Fragment() {

    private lateinit var testVM: TestDatabaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        testVM = ViewModelProvider(this)[TestDatabaseViewModel::class.java]
        val operation = Template(Template.TemplateCategory.CONSUMPTION, "asd", 4, 5, 6,5)
        testVM.insertTemplate(operation)
        setObservers(this)
    }
    private fun setObservers(owner: LifecycleOwner) {
        testVM.templates.observe(owner, {
            val g = 6
        })
    }
}