package com.mateuszkoslacz.moviper3.base.view

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hannesdorfmann.mosby3.mvp.MvpPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import com.hannesdorfmann.mosby3.mvp.delegate.ActivityMvpDelegate
import com.hannesdorfmann.mosby3.mvp.delegate.ActivityMvpDelegateImpl
import com.hannesdorfmann.mosby3.mvp.delegate.MvpDelegateCallback
import com.mateuszkoslacz.moviper3.iface.presenter.ViperRxPresenter
import com.mateuszkoslacz.moviper3.iface.view.ViperView

/**
 * An Activity that uses a [MvpPresenter] to implement a Model-View-Presenter
 * architecture.
 *
 * @author Hannes Dorfmann
 * @since 1.0.0
 */
abstract class ViperAiPassiveActivity<V : MvpView, P : MvpPresenter<V>> : AppCompatActivity(), MvpView, ViperView, MvpDelegateCallback<V, P> {

    protected val mvpDelegate: ActivityMvpDelegate<*, *> by lazy {
        ActivityMvpDelegateImpl(this, this, true)
    }
    private lateinit var presenter: P
    protected var retainInstance: Boolean = false
    protected abstract val layoutId: Int
    override fun getContext(): Context = this

    override val args: Bundle
        get() = intent.extras

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
        injectViews()
        mvpDelegate.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mvpDelegate.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mvpDelegate.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        mvpDelegate.onPause()
    }

    override fun onResume() {
        super.onResume()
        mvpDelegate.onResume()
    }

    override fun onStart() {
        super.onStart()
        mvpDelegate.onStart()
    }

    override fun onStop() {
        super.onStop()
        mvpDelegate.onStop()
    }

    override fun onRestart() {
        super.onRestart()
        mvpDelegate.onRestart()
    }

    override fun onContentChanged() {
        super.onContentChanged()
        mvpDelegate.onContentChanged()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mvpDelegate.onPostCreate(savedInstanceState)
    }

    override fun setPresenter(presenter: P) {
        this.presenter = presenter
    }

    /**
     * **DO NOT** use this method because of a fact that this view should be completely passive
     * (independent from the presenter type)! <br></br>
     * Instead you should use getters to provide
     * event sources that will notify presenter after presenter's registration to them. To use
     * getPresenter() method you shall use non-passive Moviper view.
     *
     * @return raw [ViperRxPresenter], so you can't call any custom methods on it anyway
     */
    @Deprecated("Do not use this method if you want your view to me passive!")
    override fun getPresenter() = presenter

    /**
     * Instantiate a presenter instance
     *
     * @return The [MvpPresenter] for this view
     */
    abstract override fun createPresenter(): P

    override fun getMvpView(): V {
        return this as V
    }

    protected fun injectViews() {
        // stub
    }
}
