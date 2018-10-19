package coedit.dialog

import coedit.CoeditPlugin
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * Created by Alex Plate on 19.10.2018.
 */
class StartServerDialog(val project: Project) : DialogWrapper(project) {

    private val portInput = JTextField()

    override fun doOKAction() {
        val coeditPlugin = CoeditPlugin.getInstance(project)
        val conn = coeditPlugin.myConn
        if (portInput.text != null && portInput.text.isNotEmpty()) {
            conn.myPort = portInput.text.toIntOrNull() ?: 8089
        }

        conn.startServer(project)

        coeditPlugin.subscribeToMessageBus()

        this.close(0)
    }

    init {
        this.init()
    }

    override fun createCenterPanel(): JComponent? {
        val panel = JPanel()
        panel.layout = GridBagLayout()

        val constraints = GridBagConstraints()
        constraints.fill = GridBagConstraints.HORIZONTAL

        constraints.gridx = 0
        constraints.gridy = 0
        panel.add(JLabel("Port (8089) "), constraints)
        constraints.gridx = 1
        panel.add(portInput, constraints)

        return panel
    }
}