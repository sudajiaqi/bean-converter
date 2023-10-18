package com.jiaqi.converter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.jiaqi.converter.utils.ProjectUtil;

/**
 * @author jiaqi
 */
public class ConverterToAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiClass psiClass = ProjectUtil.getPsiClassFromContext(e);
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
        PsiClass psiClass = ProjectUtil.getPsiClassFromContext(e);
        e.getPresentation().setEnabled(psiClass != null);
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
