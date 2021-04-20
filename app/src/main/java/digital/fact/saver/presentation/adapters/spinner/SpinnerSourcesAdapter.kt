package digital.fact.saver.presentation.adapters.spinner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import digital.fact.saver.R
import digital.fact.saver.domain.models.Source
import digital.fact.saver.utils.formatToMoney

class SpinnerSourcesAdapter(
    context: Context,
    resource: Int
) : ArrayAdapter<Source>(context, resource) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCorrectView(position, convertView, parent, R.layout.spinner_source)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCorrectView(position, convertView, parent, R.layout.spinner_source_dropdown)
    }

    private fun getCorrectView(
            position: Int,
            convertView: View?,
            parent: ViewGroup,
            resource: Int
    ): View {
        val view = convertView
                ?: LayoutInflater.from(context).inflate(resource, parent, false)
        view.run {
            val source = getItem(position)
            source?.let {
                findViewById<TextView>(R.id.title).text = it.name
                findViewById<TextView>(R.id.subtitle).text = it.currentSum.formatToMoney()
            }
        }
        return view
    }

}