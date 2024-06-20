package com.crzsc.plugin.git

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import org.jetbrains.annotations.Nullable
import java.awt.*
import java.awt.event.ActionEvent
import javax.swing.*

class LineFeedDialog(
    @Nullable project: Project?,
    private var fileResults: Map<VirtualFile, String>,
    private val applyListener: IApplyChanges
) :
    DialogWrapper(project) {

    val changes = mutableMapOf<VirtualFile, String>()
    private val mainPanel = JPanel(GridBagLayout())
    private val mainPanelContainer = JPanel(BorderLayout())
    private val scrollPane = JScrollPane(mainPanel)

    init {
        init()
        title = "Line Feed Checker"
        buildDialog()
    }

    override fun createCenterPanel(): JComponent {
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        mainPanelContainer.add(scrollPane, BorderLayout.CENTER)
        return mainPanelContainer
    }

    override fun getPreferredSize(): Dimension {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val maxHeight = screenSize.height * 0.5 // 设置对话框的最大高度为屏幕高度的50%
        val preferredSize = super.getPreferredSize()
        return Dimension(preferredSize.width, maxHeight.toInt())
    }

    override fun createActions(): Array<Action> {
        val applyAction = object : DialogWrapperAction("Apply") {
            override fun doAction(e: ActionEvent) {
                updateFileResults(applyListener.applyChanges(changes))
            }
        }

        return arrayOf(applyAction, okAction, cancelAction)
    }

    fun updateFileResults(newFileResults: Map<VirtualFile, String>) {
        fileResults = newFileResults
        buildDialog()
    }

    private fun buildDialog() {
        mainPanel.removeAll()
        mainPanel.revalidate()
        mainPanel.repaint()

        val constraints = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            gridx = 0
            gridy = 0
            weightx = 1.0
            insets = JBUI.insets(5, 20)
        }

        val topPanel = JPanel(GridBagLayout())
        topPanel.add(JLabel("File"), constraints)
        constraints.gridx = 1
        topPanel.add(JLabel("Current"), constraints)
        constraints.gridx = 2
        topPanel.add(JLabel("Change to"), constraints)

        mainPanelContainer.add(topPanel, BorderLayout.NORTH)

        constraints.gridy++

        val crButtons = mutableListOf<JRadioButton>()
        val lfButtons = mutableListOf<JRadioButton>()
        val crlfButtons = mutableListOf<JRadioButton>()
        val keepButtons = mutableListOf<JRadioButton>()

        val selectAllPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        val allCrButton = JRadioButton("All CR")
        val allLfButton = JRadioButton("All LF")
        val allCrlfButton = JRadioButton("All CRLF")
        val allKeepButton = JRadioButton("All KEEP").apply { isSelected = true }

        val allButtonGroup = ButtonGroup().apply {
            add(allCrButton)
            add(allLfButton)
            add(allCrlfButton)
            add(allKeepButton)
        }

        for ((file, result) in fileResults) {
            constraints.gridx = 0
            mainPanel.add(JLabel(file.name), constraints)

            constraints.gridx = 1
            mainPanel.add(JLabel(result), constraints)

            val optionsPanel = JPanel(FlowLayout(FlowLayout.LEFT))
            val crButton = JRadioButton("CR")
            val lfButton = JRadioButton("LF")
            val crlfButton = JRadioButton("CRLF")
            val keepButton = JRadioButton("Keep").apply { isSelected = true }

            optionsPanel.add(crButton)
            optionsPanel.add(lfButton)
            optionsPanel.add(crlfButton)
            optionsPanel.add(keepButton)

            val buttonGroup = ButtonGroup().apply {
                add(crButton)
                add(lfButton)
                add(crlfButton)
                add(keepButton)
            }

            crButton.addActionListener {
                changes[file] = "CR"
            }

            lfButton.addActionListener {
                changes[file] = "LF"
            }

            crlfButton.addActionListener {
                changes[file] = "CRLF"
            }

            keepButton.addActionListener {
                if (allCrButton.isSelected) {
                    changes.keys.forEach { changes.remove(it) }
                } else {
                    changes.remove(file)
                }
            }

            crButtons.add(crButton)
            lfButtons.add(lfButton)
            crlfButtons.add(crlfButton)
            keepButtons.add(keepButton)

            constraints.gridx = 2
            mainPanel.add(optionsPanel, constraints)

            constraints.gridy++
        }

        allCrButton.addActionListener {
            changes.clear()
            for (file in fileResults.keys) {
                changes[file] = "CR"
            }
            crButtons.forEach { it.isSelected = true }
        }

        allLfButton.addActionListener {
            changes.clear()
            for (file in fileResults.keys) {
                changes[file] = "LF"
            }
            lfButtons.forEach { it.isSelected = true }
        }

        allCrlfButton.addActionListener {
            changes.clear()
            for (file in fileResults.keys) {
                changes[file] = "CRLF"
            }
            crlfButtons.forEach { it.isSelected = true }
        }

        allKeepButton.addActionListener {
            changes.clear()
            for (file in fileResults.keys) {
                changes.remove(file)
            }
            keepButtons.forEach { it.isSelected = true }
        }

        selectAllPanel.add(allCrButton)
        selectAllPanel.add(allLfButton)
        selectAllPanel.add(allCrlfButton)
        selectAllPanel.add(allKeepButton)

        constraints.gridx = 0
        constraints.gridwidth = 4

        mainPanelContainer.add(selectAllPanel, BorderLayout.SOUTH)

        scrollPane.setViewportView(mainPanel)
        mainPanel.revalidate()
        mainPanel.repaint()

        setSize(preferredSize.width, preferredSize.height)
    }

}

interface IApplyChanges {
    fun applyChanges(changes: MutableMap<VirtualFile, String>):Map<VirtualFile, String>
}
