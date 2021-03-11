package digital.fact.saver.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.data.database.classes.MainDb
import digital.fact.saver.data.database.dao.PlansDao
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
            MainDb.getInstance(
                context
            )
        plansDao = db.plansDao()
        CoroutineScope(Dispatchers.IO).launch {
            val plans = plansDao.getAll()
            this@PlansRepositoryIml._plans.postValue(plans)
        }
    }

    override fun insert(item: Plan): LiveData<Long> {
        val result: MutableLiveData<Long> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(plansDao.insert(item))
        }
        return result
    }

    override fun update(item: Plan): LiveData<Int> {
        val result: MutableLiveData<Int> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(  plansDao.update(item))
        }
        return result
    }

    override fun delete(item: Plan): LiveData<Int> {
        val result: MutableLiveData<Int> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue( plansDao.delete(item))
        }
        return result
    }

    override fun deleteAll(): LiveData<Int> {
        val result: MutableLiveData<Int> = MutableLiveData()
        CoroutineScope(Dispatchers.IO).launch {
            result.postValue(  plansDao.deleteAll())
        }
        return result
    }

    override fun getAll(): LiveData<List<Plan>> {
        return plans
    }

    override fun updateAll(): LiveData<List<Plan>> {
        CoroutineScope(Dispatchers.IO).launch {
            val plans = plansDao.getAll()
            this@PlansRepositoryIml._plans.postValue(plans)
        }
        return plans
    }
}