package digital.fact.saver.domain.repository

import androidx.lifecycle.LiveData
import digital.fact.saver.domain.models.Class
import kotlinx.coroutines.Deferred

interface ClassesRepository {

    fun insert(item: Class): LiveData<Long>

    fun update(item: Class): LiveData<Int>

    fun updateAll(): LiveData<List<Class>>

    fun delete(item: Class): LiveData<Int>

    fun deleteAll(): LiveData<Int>

    fun getAll(): LiveData<List<Class>>
}