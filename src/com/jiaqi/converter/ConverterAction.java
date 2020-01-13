package com.jiaqi.converter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.jiaqi.converter.utils.ProjectUtil;


public class ConverterAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiClass psiClass = ProjectUtil.getPsiClassFromContext(e);
        if (psiClass == null) {
            return;
        }
        ConverterDialog generateConverterDialog = new ConverterDialog(psiClass, true, true);
        generateConverterDialog.show();
        if (generateConverterDialog.isOK()) {
            PsiClass classTo = generateConverterDialog.getConvertToClass();
            PsiClass classFrom = generateConverterDialog.getConvertFromClass();
            generateConvertAs(classTo, classFrom, generateConverterDialog.isInheritFields(), psiClass);
        }
    }


    @Override
    public void update(AnActionEvent e) {
        PsiClass psiClass = ProjectUtil.getPsiClassFromContext(e);
        e.getPresentation().setEnabled(psiClass != null);
    }

    private void generateConvertAs(PsiClass to, PsiClass from, boolean inherited, PsiClass contentClass) {
        WriteCommandAction.runWriteCommandAction(
                to.getProject(),
                "Convert from " + from.getQualifiedName() + " to " + to.getQualifiedName(),
                null,
                getExecute(to, from, inherited, contentClass),
                to.getContainingFile()
        );
    }

    Runnable getExecute(PsiClass to, PsiClass from, boolean inherited, PsiClass contentClass) {
        return () -> {
            ClassMapResult result = ClassMapResult.from(to, from, inherited);
            GenerateMethod action = new GenerateConverterMethod(result);
            String method = action.generate();
            PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(contentClass.getProject());
            PsiMethod convertAs = elementFactory.createMethodFromText(method, contentClass);
            PsiElement psiElement = contentClass.add(convertAs);
            JavaCodeStyleManager.getInstance(contentClass.getProject()).shortenClassReferences(psiElement);
        };
    }

}
