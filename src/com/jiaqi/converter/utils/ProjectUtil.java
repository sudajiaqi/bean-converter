package com.jiaqi.converter.utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.util.PsiTreeUtil;

public final class ProjectUtil {
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

    public static PsiClass getPsiClassFromContext(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        return PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
    }
}
