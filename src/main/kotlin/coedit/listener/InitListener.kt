package coedit.listener

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener

/**
 * Created by Alex Plate on 17.10.2018.
 */
class InitListener : DocumentListener, CoListener {

    override fun getName(): String {
        return "InitListener"
    }

    override fun documentChanged(event: DocumentEvent?) {
        // Request auth
        println("U")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InitListener

        if (getName() != other.getName()) return false

        return true
    }

    override fun hashCode(): Int {
        return getName().hashCode()
    }
}