package coedit.listener

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener

/**
 * Created by Alex Plate on 17.10.2018.
 */
class ChangeListener : DocumentListener, CoListener("ChangeListener") {

    override fun beforeDocumentChange(event: DocumentEvent?) {
        println("x")
    }
}