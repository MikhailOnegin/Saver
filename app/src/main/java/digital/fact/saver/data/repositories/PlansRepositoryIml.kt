package digital.fact.saver.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.App
import digital.fact.saver.data.database.dao.PlansDao
import digital.fact.saver.data.database.dto.PlanTable
import digital.fact.saver.domain.repository.PlansRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlansRepositoryIml : PlansRepository {
    private var plansDao: PlansDao
    private val _plans: MutableLiveData<List<PlanTable>> = MutableLiveData()
    private val plans: LiveData<List<PlanTable>> = _plans

    init {
        val db = App.db
        plansDao = db.plansDao()
        CoroutineScope(Dispatchers.IO).launch {
            val plans = plansDao.getAll()
            this@PlansRepositoryIml._plans.postValue(plans)
        }
    }

    override fun insert(item: PlanTable): LiveData<Long> {
        val result: MutableLiveData<Long> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(plansDao.insert(item))
        }
        return result
    }

    override fun update(item: PlanTable): LiveData<Int> {
        val result: MutableLiveData<Int> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(plansDao.update(item))
        }
        return result
    }

    override fun delete(item: PlanTable): LiveData<Int> {
        val result: MutableLiveData<Int> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(plansDao.delete(item))
        }
        return result
    }

    override fun deleteAll(): LiveData<Int> {
        val result: MutableLiveData<Int> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(plansDao.deleteAll())
        }
        return result
    }

    override fun getAll(): LiveData<List<PlanTable>> {
        return plans
    }

    override fun updateAll(): LiveData<List<PlanTable>> {
        CoroutineScope(Dispatchers.IO).launch {
            val plans = plansDao.getAll()
            this@PlansRepositoryIml._plans.postValue(plans)
        }
        return plans
    }

    override fun getPlansByPeriod(periodStart: Long, periodEnd: Long): LiveData<List<PlanTable>> {
        val result: MutableLiveData<List<PlanTable>> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            val plans = plansDao.getPlansForAPeriod(periodStart = periodStart, periodEnd = periodEnd)
            result.postValue(plans)
        }
        return result
    }
}