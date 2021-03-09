package digital.fact.saver.domain.repository

import androidx.lifecycle.LiveData
import digital.fact.saver.domain.models.Template

interface TemplatesRepository {
    fun insert(item: Template): LiveData<Long>

    fun update(item: Template): LiveData<Int>

    fun updateAll(): LiveData<List<Template>>

    fun delete(item: Template): LiveData<Int>

    fun deleteAll(): LiveData<Int>

    fun getAll(): LiveData<List<Template>>
}