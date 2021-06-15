/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.mcp.actions

import com.demonwav.mcdev.facet.MinecraftFacet
import com.demonwav.mcdev.platform.mcp.McpModuleType
import com.demonwav.mcdev.platform.mcp.srg.McpSrgMap
import com.demonwav.mcdev.util.ActionData
import com.demonwav.mcdev.util.gotoTargetElement
import com.demonwav.mcdev.util.invokeLater
import com.demonwav.mcdev.util.qualifiedMemberReference
import com.demonwav.mcdev.util.simpleQualifiedMemberReference
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.intellij.ui.LightColors
import com.intellij.ui.awt.RelativePoint

class GotoAtEntryAction : SrgActionBase() {
    override fun withSrgTarget(parent: PsiElement, srgMap: McpSrgMap?, e: AnActionEvent, data: ActionData) {
        when (parent) {
            is PsiField -> {
                val reference = getSrgField(srgMap, parent) ?: parent.simpleQualifiedMemberReference
                searchForText(e, data, reference.name)
            }
            is PsiMethod -> {
                val reference = getSrgMethod(srgMap, parent) ?: parent.qualifiedMemberReference
                searchForText(e, data, reference.name + reference.descriptor)
            }
            else ->
                showBalloon(e)
        }
    }

    private fun searchForText(e: AnActionEvent, data: ActionData, text: String) {
        val manager = ModuleManager.getInstance(data.project)
        val toList = manager.modules.asSequence()
            .mapNotNull { MinecraftFacet.getInstance(it, McpModuleType) }
            .toList()

        manager.modules.asSequence()
            .mapNotNull { MinecraftFacet.getInstance(it, McpModuleType) }
            .flatMap { it.accessTransformers.asSequence() }
            .forEach { virtualFile ->
                val file = PsiManager.getInstance(data.project).findFile(virtualFile) ?: return@forEach

                var found = false
                PsiSearchHelper.getInstance(data.project)
                    .processElementsWithWord(
                        { element, _ ->
                            gotoTargetElement(element, data.editor, data.file)
                            found = true
                            false
                        },
                        LocalSearchScope(file),
                        text,
                        UsageSearchContext.ANY,
                        true
                    )

                if (found) {
                    return
                }
            }

        showBalloon(e)
    }

    private fun showBalloon(e: AnActionEvent) {
        val balloon = JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder("No access transformer entry found", null, LightColors.YELLOW, null)
            .setHideOnAction(true)
            .setHideOnClickOutside(true)
            .setHideOnKeyOutside(true)
            .createBalloon()

        val project = e.project ?: return
        val statusBar = WindowManager.getInstance().getStatusBar(project)

        invokeLater { balloon.show(RelativePoint.getCenterOf(statusBar.component), Balloon.Position.atRight) }
    }
}
