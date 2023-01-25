import org.gradle.kotlin.dsl.create
import srs.kokoro.jcef.JcefExtension

extensions.create<JcefExtension>("jcef", project)
