package com.crzsc.plugin.listener

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.psi.PsiManager

class MyProjectManagerListenerV2 : ProjectActivity, Disposable {

    private val eventsMap = mutableMapOf<Project, PsiTreeListener>()

    override fun dispose() {
        for ((project, listener) in eventsMap) {
            PsiManager.getInstance(project).removePsiTreeChangeListener(listener)
        }
        eventsMap.clear()
    }

    override suspend fun execute(project: Project) {
        val treeListener = PsiTreeListener(project)
        eventsMap[project] = treeListener
        PsiManager.getInstance(project).addPsiTreeChangeListener(treeListener, this)
    }
}
