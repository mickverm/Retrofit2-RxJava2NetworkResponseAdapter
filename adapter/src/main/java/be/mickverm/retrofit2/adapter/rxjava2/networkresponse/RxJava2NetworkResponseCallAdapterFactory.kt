package be.mickverm.retrofit2.adapter.rxjava2.networkresponse

import com.squareup.moshi.Types
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


/**
 * A [CallAdapter.Factory] which allows [NetworkResponse] objects to be returned from RxJava streams.
 *
 * Adding this class to [Retrofit] allows you to return [Observable], [Flowable], [Single] or
 * [Maybe] types parameterized with [NetworkResponse] from service methods.
 *
 * Note: This adapter must be registered before an adapter that is capable of adapting RxJava2 streams.
 */
class RxJava2NetworkResponseCallAdapterFactory private constructor() : CallAdapter.Factory() {

    companion object {
        @JvmStatic
        fun create() = RxJava2NetworkResponseCallAdapterFactory()
    }

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        val rawType = getRawType(returnType)

        val isFlowable = rawType === Flowable::class.java
        val isSingle = rawType === Single::class.java
        val isMaybe = rawType == Maybe::class.java
        if (rawType !== Observable::class.java && !isFlowable && !isSingle && !isMaybe) {
            return null
        }

        @Suppress("ReplaceGuardClauseWithFunctionCall")
        if (returnType !is ParameterizedType) {
            throw IllegalStateException(
                "${rawType.simpleName} return type must be parameterized as " +
                        "${rawType.simpleName}<Foo> or ${rawType.simpleName}<? extends Foo>"
            )
        }

        val observableEmissionType = getParameterUpperBound(0, returnType)
        if (getRawType(observableEmissionType) != NetworkResponse::class.java) {
            return null
        }

        @Suppress("ReplaceGuardClauseWithFunctionCall")
        if (observableEmissionType !is ParameterizedType) {
            throw IllegalStateException(
                "NetworkResponse must be parameterized as NetworkResponse<SuccessBody, ErrorBody>"
            )
        }

        val successBodyType = getParameterUpperBound(0, observableEmissionType)
        val delegateType = Types.newParameterizedType(
            Observable::class.java,
            successBodyType
        )
        val delegateAdapter = retrofit.nextCallAdapter(
            this,
            delegateType,
            annotations
        )

        val errorBodyType = getParameterUpperBound(1, observableEmissionType)
        val errorBodyConverter = retrofit.nextResponseBodyConverter<Any>(
            null,
            errorBodyType,
            annotations
        )

        @Suppress("UNCHECKED_CAST")
        return RxJava2NetworkResponseCallAdapter(
            successBodyType,
            delegateAdapter as CallAdapter<Any, Observable<Any>>,
            errorBodyConverter,
            isFlowable,
            isSingle,
            isMaybe
        )
    }
}
