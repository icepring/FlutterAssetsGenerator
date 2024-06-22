package com.crzsc.plugin.andrasferenczi.ext.psi

import com.jetbrains.lang.dart.psi.DartMethodDeclaration
import com.jetbrains.lang.dart.psi.DartNormalFormalParameter

fun DartMethodDeclaration.findNormalFormalParameters(): Sequence<DartNormalFormalParameter> = findChildrenByType()