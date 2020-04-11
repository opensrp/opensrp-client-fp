package org.smartregister.fp.common.cursor

import net.sqlcipher.MatrixCursor
import java.util.*

open class AdvancedMatrixCursor(columnNames: Array<String?>?) : MatrixCursor(columnNames) {

    override fun getLong(column: Int): Long {
        return try {
            super.getLong(column)
        } catch (e: NumberFormatException) {
            Date().time
        }
    }
}