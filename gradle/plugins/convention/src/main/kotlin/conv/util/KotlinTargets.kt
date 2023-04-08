package conv.util

import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.newInstance
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import javax.inject.Inject

abstract class KotlinTargets<out T : KotlinTarget> @Inject constructor(val targets: Iterable<T>) : ExtensionAware, Iterable<T> {

	companion object {
		inline operator fun <reified T : KotlinTarget> invoke(objects: ObjectFactory, vararg targets: T) =
			objects.newInstance<KotlinTargets<T>>(targets.asList())
	}

	override fun iterator() = targets.iterator()

	inline operator fun times(crossinline configure: T.() -> Unit) = timesAssign(configure)

	inline operator fun timesAssign(crossinline configure: T.() -> Unit) = targets.forEach(configure)
}
