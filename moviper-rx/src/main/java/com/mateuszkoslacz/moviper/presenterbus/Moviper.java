package com.mateuszkoslacz.moviper.presenterbus;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.mateuszkoslacz.moviper.base.exception.PresenterAlreadyRegisteredException;
import com.mateuszkoslacz.moviper.base.exception.PresenterInstancesAccessNotEnabled;
import com.mateuszkoslacz.moviper.base.exception.PresentersAccessUtilNotEnabled;
import com.mateuszkoslacz.moviper.iface.presenter.ViperPresenter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;


/**
 * Created by mateuszkoslacz on 24.10.2016.
 */
public class Moviper {

    private static final String TAG = "Moviper";

    private static final Moviper instance = new Moviper();
    private MoviperErrorHandler errorHandler = e -> Log.e(TAG, "IPC default error handler: ", e);

    private Config mConfig = new Config();

    // for every presenter complete lifecycle we do two writes (save and remove), and
    // n reads where n is the size of the presenters list.
    // for every external call we do n reads where n is the size of the presenters list.
    // that makes
    // TODO: 28.10.2016 reconsider no-checking if presenter exists
    private List<ViperPresenter> mPresenters = new CopyOnWriteArrayList<>();

    private PublishSubject<MoviperBundle> registerSynchronizer = PublishSubject.create();

    private Moviper() {
        registerSynchronizer
                .subscribeOn(Schedulers.computation())
                .doOnNext(this::routeMoviperBundle)
                .doOnError(throwable -> errorHandler.onError(throwable))
                .retry()
                .subscribe();
    }

    public static Moviper getInstance() {
        return instance;
    }

    public void setErrorHandler(@NonNull MoviperErrorHandler handler) {
        this.errorHandler = handler;
    }

    /**
     * Use it in {@link Application#onCreate()} to setup IPC and IPC Instance Presenter Access to
     * let you make use of {@link #getPresenters(Class)} and {@link #getPresenterInstance(Class,
     * String)}.
     */
    public void setConfig(Config config) {
        mConfig = config;
    }

    public void register(ViperPresenter presenter) {
        if (mConfig.isPresenterAccessUtilEnabled()) {
            registerSynchronizer.onNext(new MoviperBundle(presenter, true));
        }
    }

    public void unregister(ViperPresenter presenter) {
        if (mConfig.isPresenterAccessUtilEnabled()) {
            registerSynchronizer.onNext(new MoviperBundle(presenter, false));
        }
    }

    private void routeMoviperBundle(MoviperBundle bundle) {
        if (bundle.isRegister()) {
            if (mConfig.isInstancePresentersEnabled() && mPresenters.contains(bundle.getPresenter()))
                throw new PresenterAlreadyRegisteredException(bundle.getPresenter());
            registerSync(bundle.getPresenter());
        } else {
            unregisterSync(bundle.getPresenter());
        }
    }

    /**
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code fromIterable} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * Make sure that you have enabled IPC Instance Presenter Access by using {@link
     * #setConfig(Config)} with {@link Config.Builder#withPresenterAccessUtilEnabled(boolean)} set
     * to true to avoid {@link PresenterInstancesAccessNotEnabled} exception being thrown.
     *
     * @param presenterTypeClass class of presenters you want to get
     * @return {@link Observable} that emits all (from zero to n) registered presenters of given
     * class.
     */
    public <PresenterType extends ViperPresenter> Observable<PresenterType> getPresenters(
            final Class<PresenterType> presenterTypeClass) {
        if (!mConfig.isPresenterAccessUtilEnabled()) throw new PresentersAccessUtilNotEnabled();
        return Observable.fromIterable(mPresenters)
                .filter(viperPresenter -> viperPresenter.getClass() == presenterTypeClass)
                .cast(presenterTypeClass);
    }


    private <PresenterType extends ViperPresenter> Observable<PresenterType> getPresenterInstanceObservable(
            final Class<PresenterType> presenterTypeClass, String name) {
        if (!mConfig.isInstancePresentersEnabled()) throw new PresenterInstancesAccessNotEnabled();
        return getPresenters(presenterTypeClass)
                .filter(moviperPresenter -> moviperPresenter.getName().equals(name));
    }

    /**
     * It returns a given Presenter instance wrapped in the {@link Maybe}.
     *
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code fromIterable} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * Make sure that you have fulfilled the requirements of ue the general IPC ({@link
     * #getPresenters(Class)}.
     * <p>
     * Make sure that you have enabled IPC Instance Presenter Access by using {@link
     * #setConfig(Config)} with {@link Config.Builder#withInstancePresentersEnabled(boolean)} set to
     * true to avoid {@link PresenterInstancesAccessNotEnabled} exception being thrown.
     * <p>
     * If you create two or more presenters with the same name (making {@link ViperPresenter#getName()}
     * method returns the the same name for them) with the IPC Instance Presenters Access
     * enabled, a {@link PresenterAlreadyRegisteredException} is thrown. By default {@link ViperPresenter#getName()}
     * method returns unique names.
     *
     * @param presenterTypeClass class of presenter you want to get
     * @param name               name of a presenter you want to get here. You shall set it up by
     *                           returning proper name in {@link ViperPresenter#getName()}.
     * @return {@link Maybe} that emits (or not) Presenter instance of given name and class.
     */
    public <PresenterType extends ViperPresenter> Maybe<PresenterType> getPresenterInstance(
            final Class<PresenterType> presenterTypeClass, String name) {
        return getPresenterInstanceObservable(presenterTypeClass, name)
                .firstElement();
    }

    /**
     * It returns a given Presenter instance wrapped in the {@link Single} or error if not found.
     *
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code fromIterable} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * Make sure that you have fulfilled the requirements of ue the general IPC ({@link
     * #getPresenters(Class)}.
     * <p>
     * Make sure that you have enabled IPC Instance Presenter Access by using {@link
     * #setConfig(Config)} with {@link Config.Builder#withInstancePresentersEnabled(boolean)} set to
     * true to avoid {@link PresenterInstancesAccessNotEnabled} exception being thrown.
     * <p>
     * If you create two or more presenters with the same name (making {@link ViperPresenter#getName()}
     * method returns the the same name for them) with the IPC Instance Presenters Access
     * enabled, a {@link PresenterAlreadyRegisteredException} is thrown. By default {@link ViperPresenter#getName()}
     * method returns unique names.
     *
     * @param presenterTypeClass class of presenter you want to get
     * @param name               name of a presenter you want to get here. You shall set it up by
     *                           returning proper name in {@link ViperPresenter#getName()}.
     * @return {@link Single} that emits Presenter instance of given name and class or throws a
     * {@link java.util.NoSuchElementException}.
     */
    public <PresenterType extends ViperPresenter> Single<PresenterType> getPresenterInstanceOrError(
            final Class<PresenterType> presenterTypeClass, String name) {
        return getPresenterInstanceObservable(presenterTypeClass, name)
                .singleOrError();
    }

    private void registerSync(ViperPresenter presenter) {
        mPresenters.add(presenter);
    }

    private void unregisterSync(ViperPresenter presenter) {
        mPresenters.remove(presenter);
    }

    @VisibleForTesting
    public void unregisterAll() {
        mPresenters.clear();
    }

    public interface MoviperErrorHandler {

        void onError(Throwable e);
    }

    private class MoviperBundle {

        private ViperPresenter mPresenter;

        private boolean mRegister;

        public MoviperBundle(ViperPresenter presenter, boolean register) {
            mPresenter = presenter;
            mRegister = register;
        }

        public ViperPresenter getPresenter() {
            return mPresenter;
        }

        public boolean isRegister() {
            return mRegister;
        }
    }

}
