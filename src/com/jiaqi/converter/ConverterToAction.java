package com.jiaqi.converter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;


/**
 * @author jiaqi
 */
public class ConverterToAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromContext(e);
        if (psiClass == null) {
            return;
        }
        ConverterDialog generateConverterDialog = new ConverterDialog(psiClass, false, true);
        generateConverterDialog.show();
        if (generateConverterDialog.isOK()) {
            PsiClass classTo = generateConverterDialog.getConvertToClass();
            generateConvertAs(classTo, psiClass, generateConverterDialog.isInheritFields());
        }
    }

    @Override
    public void update(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromContext(e);
        e.getPresentation().setEnabled(psiClass != null);
    }

    private PsiClass getPsiClassFromContext(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        return PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
    }

    private void generateConvertAs(PsiClass to, PsiClass from, boolean inherited) {
        WriteCommandAction.runWriteCommandAction(
                from.getProject(),
                "Convert to " + to.getQualifiedName(),
                null,
                getExecute(to, from, inherited),
                from.getContainingFile()
        );
    }

    Runnable getExecute(PsiClass to, PsiClass from, boolean inherited) {
        return () -> {
            ClassMapResult result = ClassMapResult.from(to, from, inherited);
            GenerateToMethod action = new GenerateToMethod(result);
            String method = action.generate();
            PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(from.getProject());
            PsiMethod convertAs = elementFactory.createMethodFromText(method, from);
            PsiElement psiElement = from.add(convertAs);
            JavaCodeStyleManager.getInstance(from.getProject()).shortenClassReferences(psiElement);
        };
    }


}
