package com.bignerdranch.android.criminalintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.sql.Time
import java.util.*

private const val ARG_TIME = "time"

class TimePickerFragment: DialogFragment() {

    interface Callbacks {
        fun onTimeSelected(date: Date)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val timeListener = TimePickerDialog.OnTimeSetListener { _: TimePicker, hours: Int, minutes: Int ->

            val date = arguments?.getSerializable(ARG_TIME) as Date
            val calendar = Calendar.getInstance()
            calendar.time = date
            val initialYear = calendar.get(Calendar.YEAR)
            val initialMonth = calendar.get(Calendar.MONTH)
            val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

            val resultDate : Date = GregorianCalendar(initialYear, initialMonth, initialDay, hours, minutes).time
            targetFragment?.let { fragment ->
                (fragment as Callbacks).onTimeSelected(resultDate)
            }
        }


        val date = arguments?.getSerializable(ARG_TIME) as Date
        val calendar = Calendar.getInstance()
        calendar.time = date
        val initialHours = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinutes = calendar.get(Calendar.MINUTE)

        return TimePickerDialog(
            requireContext(),
            timeListener,
            initialHours,
            initialMinutes,
            false

        )

    }

    companion object {
        fun newInstance(date: Date): TimePickerFragment {
            val args = Bundle().apply{
                putSerializable(ARG_TIME, date)
            }

            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }
}