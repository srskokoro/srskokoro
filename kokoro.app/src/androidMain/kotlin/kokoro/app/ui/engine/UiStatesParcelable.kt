package kokoro.app.ui.engine

import android.os.Parcel
import android.os.Parcelable
import androidx.collection.MutableScatterMap

internal class UiStatesParcelable : Parcelable {
	@JvmField val map: MutableScatterMap<String, String?>

	constructor() {
		map = MutableScatterMap()
	}

	constructor(parcel: Parcel) {
		var n = parcel.readInt()
		val map = MutableScatterMap<String, String?>(n)
		while (--n >= 0) {
			val key = parcel.readString()
			val value = parcel.readString()
			if (key != null) map[key] = value
		}
		this.map = map
	}

	override fun describeContents(): Int = 0

	override fun writeToParcel(dest: Parcel, flags: Int) {
		val map = map
		dest.writeInt(map.size)
		map.forEach { key, value ->
			dest.writeString(key)
			dest.writeString(value)
		}
	}

	companion object CREATOR : Parcelable.Creator<UiStatesParcelable> {

		override fun createFromParcel(parcel: Parcel) = UiStatesParcelable(parcel)

		override fun newArray(size: Int) = arrayOfNulls<UiStatesParcelable>(size)
	}
}
