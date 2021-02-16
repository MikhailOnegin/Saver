package digital.fact.saver.domain.repository

import androidx.lifecycle.LiveData
import digital.fact.saver.domain.models.Template

interface TemplatesRepository {

    fun insert(item: Template)

    fun update(item: Template)

    fun delete(item: Template)

    fun deleteAll()

    fun getAll(): LiveData<List<Template>>
}