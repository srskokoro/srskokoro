package kokoro.app.ui.wv

inline fun <T1> CallbackRouter(
	s1: T1,
	crossinline route: (
		data: String,
		s1: T1,
	) -> Unit
) = object : CallbackRouter.WithS1<T1>(
	s1,
) {
	override fun route(data: String) = route(
		data,
		this.s1,
	)
}

inline fun <T1, T2> CallbackRouter(
	s1: T1,
	s2: T2,
	crossinline route: (
		data: String,
		s1: T1,
		s2: T2,
	) -> Unit
) = object : CallbackRouter.WithS2<T1, T2>(
	s1,
	s2,
) {
	override fun route(data: String) = route(
		data,
		this.s1,
		this.s2,
	)
}

inline fun <T1, T2, T3> CallbackRouter(
	s1: T1,
	s2: T2,
	s3: T3,
	crossinline route: (
		data: String,
		s1: T1,
		s2: T2,
		s3: T3,
	) -> Unit
) = object : CallbackRouter.WithS3<T1, T2, T3>(
	s1,
	s2,
	s3,
) {
	override fun route(data: String) = route(
		data,
		this.s1,
		this.s2,
		this.s3,
	)
}

interface CallbackRouter {

	fun route(data: String)

	abstract class WithS1<T1>(
		val s1: T1,
	) : CallbackRouter {

		override fun equals(other: Any?): Boolean {
			if (other is WithS1<*>) {
				@Suppress("USELESS_CAST")
				val o = other as WithS1<*>
				if (
					o.s1 == s1
				) return true
			}
			return false
		}

		override fun hashCode(): Int {
			return 31 + (s1?.hashCode() ?: 0)
		}
	}

	abstract class WithS2<T1, T2>(
		val s1: T1,
		val s2: T2,
	) : CallbackRouter {

		override fun equals(other: Any?): Boolean {
			if (other is WithS2<*, *>) {
				@Suppress("USELESS_CAST")
				val o = other as WithS2<*, *>
				if (
					o.s1 == s1 &&
					o.s2 == s2
				) return true
			}
			return false
		}

		override fun hashCode(): Int {
			var r = 31 + (s1?.hashCode() ?: 0)
			r = r * 31 + (s2?.hashCode() ?: 0)
			return r
		}
	}

	abstract class WithS3<T1, T2, T3>(
		val s1: T1,
		val s2: T2,
		val s3: T3,
	) : CallbackRouter {

		override fun equals(other: Any?): Boolean {
			if (other is WithS3<*, *, *>) {
				@Suppress("USELESS_CAST")
				val o = other as WithS3<*, *, *>
				if (
					o.s1 == s1 &&
					o.s2 == s2 &&
					o.s3 == s3
				) return true
			}
			return false
		}

		override fun hashCode(): Int {
			var r = 31 + (s1?.hashCode() ?: 0)
			r = r * 31 + (s2?.hashCode() ?: 0)
			r = r * 31 + (s3?.hashCode() ?: 0)
			return r
		}
	}
}
