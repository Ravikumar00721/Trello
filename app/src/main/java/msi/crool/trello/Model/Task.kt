package msi.crool.trello.Model

import android.os.Parcel
import android.os.Parcelable

data class Task(
    var title: String? ="",
    var createdby: String? ="",
    var cards:ArrayList<Card> =ArrayList()
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(Card.CREATOR)!!
    ) {
    }

    override fun writeToParcel(dest: Parcel, flags: Int)= with(dest) {
        writeString(title)
        writeString(createdby)
        writeTypedList(cards)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(parcel: Parcel): Task {
            return Task(parcel)
        }

        override fun newArray(size: Int): Array<Task?> {
            return arrayOfNulls(size)
        }
    }
}
