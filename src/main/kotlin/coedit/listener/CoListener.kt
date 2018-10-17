package coedit.listener

/**
 * Created by Alex Plate on 17.10.2018.
 */
interface CoListener {
    fun getName(): String {
        return javaClass.name
    }
}