package com.crzsc.plugin.git

import com.crzsc.plugin.setting.PluginSetting
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory
import com.intellij.openapi.vfs.VirtualFile

class LineBreakCheckinHandlerFactory : CheckinHandlerFactory() {
    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {

        return object : CheckinHandler() {

            override fun beforeCheckin(): ReturnResult {
                if (!PluginSetting.instance.gitPreCommit) {
                    return ReturnResult.COMMIT
                }
                val files = panel.virtualFiles
                files.let { it.toString() }
                files.also { toString() }
                val checker = LineFeedChecker(panel.project)
                val results = checker.checkLineFeeds(files)

                val dialog = LineFeedDialog(panel.project, results, object : IApplyChanges {
                    override fun applyChanges(changes: MutableMap<VirtualFile, String>): Map<VirtualFile, String> {
                        for ((file, format) in changes) {
                            checker.convertLineFeeds(file, format)
                        }
                        return checker.checkLineFeeds(files)
                    }
                })
                if (dialog.showAndGet()) {
                    val changes = dialog.changes
                    for ((file, format) in changes) {
                        checker.convertLineFeeds(file, format)
                    }
                    return ReturnResult.COMMIT
                }
                return ReturnResult.CANCEL
            }
        }
    }
}
