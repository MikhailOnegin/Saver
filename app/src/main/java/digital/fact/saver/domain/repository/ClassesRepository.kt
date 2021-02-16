package digital.fact.saver.domain.repository

import androidx.lifecycle.LiveData
import digital.fact.saver.domain.models.Class
import digital.fact.saver.domain.models.Operation

interface ClassesRepository {
    fun insert(item: Class)

    fun update()

    fun delete(item: Class)

    fun deleteAll()

    fun getAll(): LiveData<List<Class>>
}