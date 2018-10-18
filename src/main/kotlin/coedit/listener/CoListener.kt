package coedit.listener

/**
 * Created by Alex Plate on 17.10.2018.
 */
abstract class CoListener(private val name: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoListener

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}