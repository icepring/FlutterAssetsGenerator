package com.crzsc.plugin.andrasferenczi.ext.swing

import javax.swing.JButton

fun JButton.removeAllListeners() {
    actionListeners.forEach { this.removeActionListener(it) }
}
