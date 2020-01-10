package com.jiaqi.converter.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;

public class ProjectUtil {
    public static String getProjectIndentation(PsiClass psiClass) {
        CommonCodeStyleSettings.IndentOptions indentOptions = CodeStyleSettings.IndentOptions.retrieveFromAssociatedDocument(psiClass.getContainingFile());
        String indentation = "        ";
        if (indentOptions != null) {
            if (indentOptions.USE_TAB_CHARACTER) {
                indentation = "\t\t";
            } else {
                indentation = new String(new char[2 * indentOptions.INDENT_SIZE]).replace("\0", " ");
            }
        }
        return indentation;
    }
}
