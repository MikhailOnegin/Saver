package digital.fact.saver.domain.repository

import androidx.lifecycle.LiveData
import digital.fact.saver.domain.models.Class1

interface ClassesRepository {
    fun insert(item: Class1)

    fun update(item: Class1)

    fun delete(item: Class1)

    fun deleteAll()

    fun getAll(): LiveData<List<Class1>>
}