package com.jkjk.doodlewatch.act

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.jkjk.doodlewatch.R
import com.jkjk.doodlewatch.core.act.BaseAct
import kotlinx.android.synthetic.main.act_input.*

class InputAct : BaseAct() {

    override val layoutResId: Int
        get() = R.layout.act_input

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBody()
    }

    override fun setupBody() {
        text.setText(intent?.getStringExtra(EXTRA_INPUT))

        text.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val input = text.text.toString()
                setResult(RESULT_OK, Intent().putExtra(EXTRA_INPUT, input))
                finish()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    override fun onResume() {
        super.onResume()

        text.requestFocus()
        text.post {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(text, InputMethodManager.SHOW_FORCED)
        }
    }

    companion object {
        const val EXTRA_INPUT = "EXTRA_INPUT"
    }
}