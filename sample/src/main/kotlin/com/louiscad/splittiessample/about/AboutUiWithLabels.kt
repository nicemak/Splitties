package com.louiscad.splittiessample.about

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.text.Layout
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.Barrier
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.louiscad.splittiessample.R
import com.louiscad.splittiessample.extensions.ui.addDefaultAppBar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import splitties.collections.forEachByIndex
import splitties.dimensions.dip
import splitties.dimensions.dp
import splitties.resources.txt
import splitties.views.dsl.constraintlayout.*
import splitties.views.dsl.coordinatorlayout.coordinatorLayout
import splitties.views.dsl.core.*
import splitties.views.dsl.material.contentScrollingWithAppBarLParams
import splitties.views.onClick
import splitties.views.textAppearance
import splitties.views.textResource
import java.text.BreakIterator

class AboutUiWithLabels(override val ctx: Context) : Ui {
    companion object {
        /** Used as a workaround for jank. See https://stackoverflow.com/questions/44481035/android-first-time-animation-is-not-smooth*/
        var firstAnimatorUsage = true
    }

    @SuppressLint("SetTextI18n")
    private val mainContent = constraintLayout {
        val libNameLabel = label(R.string.library_name)
        val libNameTv = tv {
            textResource = R.string.lib_name
        }
        val authorLabel = label(R.string.author)
        val authorTv = tv {
            text = "Louis CAD"
        }
        val licenseLabel = label(R.string.license)
        val licenseTv = tv {
            text = "Apache v2.0"
        }
        val labelsBarrier = endBarrier(libNameLabel, authorLabel, licenseLabel)
        addLabelAndTv(labelsBarrier, libNameLabel, libNameTv) { topOfParent() }
        addLabelAndTv(labelsBarrier, authorLabel, authorTv) { topToBottomOf(libNameTv) }
        addLabelAndTv(labelsBarrier, licenseLabel, licenseTv) { topToBottomOf(authorTv) }
        val hello = "Hello folks! 👋🏽👋🏽"
        val helloTv = add(textView {
            if (SDK_INT >= 23) hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
            text = hello
        }, lParams(wrapContent, wrapContent) { topToBottomOf(licenseTv) })
        val helloTvList: List<TextView> = hello.splitCharacters().map {
            textView {
                if (SDK_INT >= 23) hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
                text = it
            }
        }
        horizontalChain(
            views = helloTvList,
            defaultHeight = wrapContent,
            defaultWidth = wrapContent,
            style = packed,
            initLastViewParams = {}
        ) { topToBottomOf(helloTv) }
        var isBig = false
        add(button {
            text = "Animate"
            onClick {
                MainScope().launch {
                    if (firstAnimatorUsage) {
                        firstAnimatorUsage = false
                        val millis = 1500L
                        ValueAnimator.ofFloat(0f, 144f).apply {
                            duration = millis
                            addUpdateListener {
                                requestLayout()
                                invalidate()
                            }
                        }.start()
                        delay(millis)
                    }
                    val initialSize = if (isBig) 48f else 14f
                    val targetSize = if (isBig) 14f else 48f
                    helloTvList.last().animate().translationX(if (isBig) 0f else dp(40))
                    isBig = !isBig
                    ValueAnimator.ofFloat(initialSize, targetSize).apply {
                        duration = 500
                        addUpdateListener {
                            helloTvList.forEachByIndex { textView ->
                                textView.textSize = animatedValue as Float
                            }
                        }
                    }.start()
                }
            }
        }, lParams(wrapContent, wrapContent) {
            topToBottomOf(helloTvList.firstOrNull() ?: licenseTv)
        })
    }

    override val root = coordinatorLayout {
        fitsSystemWindows = true
        addDefaultAppBar(ctx)
        add(mainContent, contentScrollingWithAppBarLParams {
            margin = dip(16)
        })
    }

    private fun label(@StringRes txtResId: Int) = textView {
        textAppearance = R.style.TextAppearance_MaterialComponents_Body2
        text = buildSpannedString { bold { append(txt(txtResId)) } }
    }

    private inline fun tv(initView: TextView.() -> Unit = {}) = textView {
        textAppearance = R.style.TextAppearance_MaterialComponents_Body2
        initView()
    }

    private inline fun ConstraintLayout.addLabelAndTv(
        labelBarrier: Barrier,
        label: View,
        tv: View,
        addLabelConstraints: ConstraintLayout.LayoutParams.() -> Unit
    ) {
        add(label, lParams(wrapContent, wrapContent) {
            startOfParent(); addLabelConstraints()
        })
        add(tv, lParams(wrapContent, wrapContent) {
            startToEndOf(labelBarrier); alignVerticallyOn(label)
            startMargin = dip(8)
        })
    }

    private fun String.splitCharacters(): List<String> = mutableListOf<String>().also { list ->
        val breakIterator = BreakIterator.getCharacterInstance()
        breakIterator.setText(this)
        var start = breakIterator.first()
        var end = breakIterator.next()
        while (end != BreakIterator.DONE) {
            list.add(this@splitCharacters.substring(start, end))
            start = end
            end = breakIterator.next()
        }
    }
}
