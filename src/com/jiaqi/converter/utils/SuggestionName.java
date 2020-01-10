package com.jiaqi.converter.utils;

import com.intellij.psi.PsiClass;

public class SuggestionName {

    public static String get(PsiClass psiClass) {
        String className = psiClass.getName();
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

}
