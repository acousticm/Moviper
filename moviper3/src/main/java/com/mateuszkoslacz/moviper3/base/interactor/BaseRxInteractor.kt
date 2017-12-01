package com.mateuszkoslacz.moviper3.base.interactor


import com.mateuszkoslacz.moviper3.iface.interactor.ViperRxInteractor
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by lucas.urbas on 29/08/15.
 *
 *
 * Adapted and modified by mateuszkoslacz on 21.10.2016.
 *
 *
 * Base Interactor class. (see ViperInteractor)
 *
 *
 * It's responsible for manipulating data on behalf of presenter, ie. saving data to db, getting
 * data from remotes etc.
 *
 *
 * It has no Presenter reference as it should return Observables to asynchronously pass data to
 * Presenter. If you are looking for solution adopted to regular, non-Rx approach, see [ ].
 */
abstract class BaseRxInteractor : ViperRxInteractor {

    protected val disposables = CompositeDisposable()

    override fun attach() {

    }

    override fun detach(retainInstance: Boolean) {

    }
}
