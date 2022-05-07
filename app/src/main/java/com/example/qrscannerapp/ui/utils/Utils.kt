package com.example.newsapplication.ui.utils

import java.text.SimpleDateFormat
import java.util.*

object Utils {

    fun standardDateTime(inputDate: String): String? {
        var date = ""
        try {
            val strCurrentDate = inputDate.substring(0, inputDate.lastIndexOf("T"))
            var format = SimpleDateFormat("yyyy-MM-dd")
            val newDate: Date = format.parse(strCurrentDate)
            format = SimpleDateFormat("d MMM, yyyy")
            date = format.format(newDate)
            val cal = Calendar.getInstance()
            cal.time = newDate
            val day = cal[Calendar.DATE]
            return if (day in 11..13) {
                SimpleDateFormat("d'th' MMM, yyyy").format(newDate)
            } else when (day % 10) {
                1 -> SimpleDateFormat("d'st' MMM, yyyy").format(newDate)
                2 -> SimpleDateFormat("d'nd' MMM, yyyy").format(newDate)
                3 -> SimpleDateFormat("d'rd' MMM, yyyy").format(newDate)
                else -> SimpleDateFormat("d'th' MMM, yyyy").format(newDate)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return date
    }
}