package org.smartregister.fp.common.util

import android.content.Context
import org.smartregister.configurableviews.model.Field
import org.smartregister.configurableviews.model.RegisterConfiguration
import org.smartregister.fp.R
import java.util.*

object ConfigHelperUtils {

    fun defaultRegisterConfiguration(context: Context?) : RegisterConfiguration? {
        if (context == null) {
            return null
        }

        val config = RegisterConfiguration()
        config.isEnableAdvancedSearch = true
        config.isEnableFilterList = true
        config.isEnableSortList = true
        config.searchBarText = context.getString(R.string.search_hint)
        config.isEnableJsonViews = false

        val filers: MutableList<Field> = ArrayList()
        /*filers.add(Field(context.getString(R.string.has_tasks_due), "has_tasks_due"))
        filers.add(Field(context.getString(R.string.risky_pregnancy), "risky_pregnancy"))
        filers.add(Field(context.getString(R.string.syphilis_positive), "syphilis_positive"))
        filers.add(Field(context.getString(R.string.hiv_positive), "hiv_positive"))
        filers.add(Field(context.getString(R.string.hypertensive), "hypertensive"))*/
        config.filterFields = filers

        val sortFields: MutableList<Field> = ArrayList()
        /*sortFields.add(Field(context.getString(R.string.updated_recent_first), "updated_at desc"))
        sortFields.add(Field(context.getString(R.string.ga_older_first), "ga asc"))
        sortFields.add(Field(context.getString(R.string.ga_younger_first), "ga desc"))
        sortFields.add(Field(context.getString(R.string.id), "id"))
        sortFields.add(Field(context.getString(R.string.first_name_a_to_z), "first_name asc"))
        sortFields.add(Field(context.getString(R.string.first_name_z_to_a), "first_name desc"))
        sortFields.add(Field(context.getString(R.string.last_name_a_to_z), "last_name asc"))
        sortFields.add(Field(context.getString(R.string.last_name_z_to_a), "last_name desc"))*/
        config.sortFields = sortFields

        return config
    }
}