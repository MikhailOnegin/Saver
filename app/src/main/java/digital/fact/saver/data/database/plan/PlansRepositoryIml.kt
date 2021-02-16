package digital.fact.saver.data.database.plan

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.data.database.operation.OperationsDao
import digital.fact.saver.data.database.operation.OperationsDb
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.domain.repository.PlansRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlansRepositoryIml(context: Context) : PlansRepository {
    private var plansDao: PlansDao
    private val _plans: MutableLiveData<List<Plan>> = MutableLiveData()
    private val plans: LiveData<List<Plan>> = _plans

    init {
        val db = PlansDb.getInstance(context)
        plansDao = db.plansDao()
        CoroutineScope(Dispatchers.IO).launch {
            val sources = plansDao.getAll()
            this@PlansRepositoryIml._plans.postValue(sources.value)
        }
    }

    override fun insert(item: Plan) {
        CoroutineScope(Dispatchers.IO).launch {
            plansDao.insert(item)
        }
    }

    override fun update() {
        CoroutineScope(Dispatchers.IO).launch {
            val sources = plansDao.getAll()
            this@PlansRepositoryIml._plans.postValue(sources.value)
        }
    }

    override fun delete(item: Plan) {
        CoroutineScope(Dispatchers.IO).launch {
            plansDao.delete(item)
        }
    }

    override fun deleteAll() {
        CoroutineScope(Dispatchers.IO).launch {
            plansDao.deleteAll()
        }
    }

    override fun getAll(): LiveData<List<Plan>> {
        return plans
    }
}