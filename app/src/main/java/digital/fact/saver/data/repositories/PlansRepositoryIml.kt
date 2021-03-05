package digital.fact.saver.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.data.database.plan.PlansDao
import digital.fact.saver.data.database.plan.PlansDb
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
        val db =
            PlansDb.getInstance(
                context
            )
        plansDao = db.plansDao()
        CoroutineScope(Dispatchers.IO).launch {
            val plans = plansDao.getAll()
            this@PlansRepositoryIml._plans.postValue(plans)
        }
    }

    override fun insert(item: Plan) {
        CoroutineScope(Dispatchers.IO).launch {
            plansDao.insert(item)
        }
    }

    override fun update(item: Plan) {
        CoroutineScope(Dispatchers.IO).launch {
            plansDao.update(item)
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

    override fun updateAll() {
        CoroutineScope(Dispatchers.IO).launch {
            val plans = plansDao.getAll()
            this@PlansRepositoryIml._plans.postValue(plans)
        }
    }
}