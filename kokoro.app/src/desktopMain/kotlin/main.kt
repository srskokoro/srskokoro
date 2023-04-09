import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing

fun main(args: Array<out String>): Unit = runBlocking {
	launch(Dispatchers.Swing) {
		run(System.getProperty("user.dir"), args)
	}
}
