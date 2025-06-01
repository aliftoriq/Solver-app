package com.example.solvr.utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class EditTextRupiah @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private var current = ""

    init {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    removeTextChangedListener(this)

                    val cleanString = s.toString().replace("[Rp,.\\s]".toRegex(), "")
                    if (cleanString.isEmpty()) {
                        current = "Rp 0,00"
                        setText(current)
                        setSelection(current.length)
                        addTextChangedListener(this)
                        return
                    }

                    try {
                        val formatted = formatRupiah(cleanString)
                        current = formatted
                        setText(formatted)
                        setSelection(formatted.length)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    addTextChangedListener(this)
                }
            }
        })
    }

    private fun formatRupiah(value: String): String {
        val parsed = value.toDouble()
        val formatter = DecimalFormat.getCurrencyInstance(Locale("in", "ID")) as DecimalFormat
        val symbols = DecimalFormatSymbols(Locale("in", "ID")).apply {
            currencySymbol = "Rp "
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        formatter.decimalFormatSymbols = symbols


        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0

        return formatter.format(parsed)
    }

    fun getCleanValue(): Long {
        val clean = this.text.toString()
            .replace("Rp", "")
            .replace(".", "")
            .trim()
        return clean.toLongOrNull()?:0L
        }
}