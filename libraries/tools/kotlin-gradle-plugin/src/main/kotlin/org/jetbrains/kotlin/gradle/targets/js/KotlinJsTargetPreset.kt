/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PackageDirectoryMismatch")

// Old package for compatibility
package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.JsCompilerType
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.removeCapitalizedJsCompilerSuffix
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTargetConfigurator
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTargetPreset
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName

open class KotlinJsTargetPreset(
    project: Project,
    kotlinPluginVersion: String
) : KotlinOnlyTargetPreset<KotlinJsTarget, KotlinJsCompilation>(
    project,
    kotlinPluginVersion
) {
    var irPreset: KotlinJsIrTargetPreset? = null
        internal set

    override val platformType: KotlinPlatformType
        get() = KotlinPlatformType.js

    override fun instantiateTarget(name: String): KotlinJsTarget {
        return project.objects.newInstance(
            KotlinJsTarget::class.java,
            project,
            platformType
        ).apply {
            this.irTarget = irPreset?.createTarget(
                lowerCamelCaseName(name.removeCapitalizedJsCompilerSuffix(JsCompilerType.legacy), JsCompilerType.ir.name)
            )
        }
    }

    override fun createKotlinTargetConfigurator() = KotlinJsTargetConfigurator(
        kotlinPluginVersion
    )

    override fun getName(): String {
        return lowerCamelCaseName(
            PRESET_NAME,
            irPreset?.let { JsCompilerType.both.name }
        )
    }

    override fun createCompilationFactory(forTarget: KotlinOnlyTarget<KotlinJsCompilation>): KotlinJsCompilationFactory {
        return KotlinJsCompilationFactory(project, forTarget, irPreset?.let { (forTarget as KotlinJsTarget).irTarget })
    }

    companion object {
        const val PRESET_NAME = "js"
    }
}

class KotlinJsSingleTargetPreset(
    project: Project,
    kotlinPluginVersion: String
) : KotlinJsTargetPreset(
    project,
    kotlinPluginVersion
) {
    // In a Kotlin/JS single-platform project, we don't need any disambiguation suffixes or prefixes in the names:
    override fun provideTargetDisambiguationClassifier(target: KotlinOnlyTarget<KotlinJsCompilation>): String? =
        irPreset?.let {
            super.provideTargetDisambiguationClassifier(target)
                ?.removePrefix(target.name.removeCapitalizedJsCompilerSuffix(JsCompilerType.legacy))
                ?.decapitalize()
        }

    override fun createKotlinTargetConfigurator() = KotlinJsTargetConfigurator(
        kotlinPluginVersion
    )
}