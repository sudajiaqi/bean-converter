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


/**
 * @author jiaqi
 */
public class ConverterFromAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiClass psiClass = ProjectUtil.getPsiClassFromContext(e);
        if (psiClass == null) {
            return;
        }
        ConverterDialog generateConverterDialog = new ConverterDialog(psiClass, true, false);
        generateConverterDialog.show();
        if (generateConverterDialog.isOK()) {
            PsiClass classFrom = generateConverterDialog.getConvertFromClass();
            generateConvertAs(psiClass, classFrom, generateConverterDialog.isInheritFields());
        }
    }

    @Override
    public void update(AnActionEvent e) {
        PsiClass psiClass = ProjectUtil.getPsiClassFromContext(e);
        e.getPresentation().setEnabled(psiClass != null);
    }

    private void generateConvertAs(PsiClass to, PsiClass from, boolean inherited) {
        WriteCommandAction.runWriteCommandAction(
                to.getProject(),
                "Convert from " + from.getQualifiedName(),
                null,
                getExecute(to, from, inherited),
                to.getContainingFile()
        );
    }

    Runnable getExecute(PsiClass to, PsiClass from, boolean inherited) {
        return () -> {
            ClassMapResult result = ClassMapResult.from(to, from, inherited);
            GenerateMethod action = new GenerateFromMethod(result);
            String method = action.generate();
            PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(to.getProject());
            PsiMethod convertAs = elementFactory.createMethodFromText(method, to);
            PsiElement psiElement = to.add(convertAs);
            JavaCodeStyleManager.getInstance(to.getProject()).shortenClassReferences(psiElement);
        };
    }


}
