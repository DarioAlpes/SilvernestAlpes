package co.smartobjects.silvernestandroid.utilidades

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers

internal fun <T> Flowable<T>.suscribirseEnUI(): Flowable<T> = subscribeOn(AndroidSchedulers.mainThread())
internal fun <T> Observable<T>.suscribirseEnUI(): Observable<T> = subscribeOn(AndroidSchedulers.mainThread())
internal fun <T> Single<T>.suscribirseEnUI(): Single<T> = subscribeOn(AndroidSchedulers.mainThread())
internal fun <T> Maybe<T>.suscribirseEnUI(): Maybe<T> = subscribeOn(AndroidSchedulers.mainThread())

internal fun <T> Flowable<T>.observarEnUI(): Flowable<T> = observeOn(AndroidSchedulers.mainThread())
internal fun <T> Observable<T>.observarEnUI(): Observable<T> = observeOn(AndroidSchedulers.mainThread())
internal fun <T> Single<T>.observarEnUI(): Single<T> = observeOn(AndroidSchedulers.mainThread())
internal fun <T> Maybe<T>.observarEnUI(): Maybe<T> = observeOn(AndroidSchedulers.mainThread())

internal fun <T> Flowable<T>.usarSchedulersEnUI(scheduler: Scheduler): Flowable<T> = subscribeOn(scheduler).observarEnUI()
internal fun <T> Observable<T>.usarSchedulersEnUI(scheduler: Scheduler): Observable<T> = subscribeOn(scheduler).observarEnUI()
internal fun <T> Single<T>.usarSchedulersEnUI(scheduler: Scheduler): Single<T> = subscribeOn(scheduler).observarEnUI()
internal fun <T> Maybe<T>.usarSchedulersEnUI(scheduler: Scheduler): Maybe<T> = subscribeOn(scheduler).observarEnUI()