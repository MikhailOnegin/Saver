package digital.fact.saver.presentation.fragments.bank

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import digital.fact.saver.domain.models.Class
import digital.fact.saver.presentation.viewmodels.TestDatabaseViewModel

class BanksFragment : Fragment() {
    var f = 0
    private lateinit var vmClass: TestDatabaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vmClass = ViewModelProvider(this)[TestDatabaseViewModel::class.java]
        setObservers(this)

    }
    private fun setObservers(owner: LifecycleOwner) {
        vmClass.getAllClass().observe(owner, Observer {
            f++
            vmClass.insertClass(Class(Class.ClassCategory.COSTS, "asdA"))
            vmClass.updateClasses()
            if(it == null){
                Log.i("eeee","null")
            }
            else{
                Log.i("eeee",it.toString())
            }
        })
    }
}