package digital.fact.saver.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.App
import digital.fact.saver.data.database.dao.PlansDao
import digital.fact.saver.data.database.dto.DbPlan
import digital.fact.saver.domain.repository.PlansRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlansRepositoryIml : PlansRepository {
    private var plansDao: PlansDao
    private val _plans: MutableLiveData<List<DbPlan>> = MutableLiveData()
    private val plans: LiveData<List<DbPlan>> = _plans

    init {
        val db = App.db
        plansDao = db.plansDao()
        CoroutineScope(Dispatchers.IO).launch {
            val plans = plansDao.getAll()
            this@PlansRepositoryIml._plans.postValue(plans)
        }
    }

    override fun insert(item: DbPlan): LiveData<Long> {
        val result: MutableLiveData<Long> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(plansDao.insert(item))
        }
        return result
    }

    override fun update(item: DbPlan): LiveData<Int> {
        val result: MutableLiveData<Int> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(plansDao.update(item))
        }
        return result
    }

    override fun delete(item: DbPlan): LiveData<Int> {
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

    override fun getAll(): LiveData<List<DbPlan>> {
        return plans
    }

    override fun updateAll(): LiveData<List<DbPlan>> {
        CoroutineScope(Dispatchers.IO).launch {
            val plans = plansDao.getAll()
            this@PlansRepositoryIml._plans.postValue(plans)
        }
        return plans
    }

    override fun getPlansByPeriod(periodStart: Long, periodEnd: Long): LiveData<List<DbPlan>> {
        val result: MutableLiveData<List<DbPlan>> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            val plans = plansDao.getPlansForAPeriod(periodStart = periodStart, periodEnd = periodEnd)
            result.postValue(plans)
        }
        return result
    }
}