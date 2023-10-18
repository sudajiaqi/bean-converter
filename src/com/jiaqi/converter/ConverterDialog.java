package com.jiaqi.converter;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.TextFieldWithAutoCompletion;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jiaqi
 */
public class ConverterDialog extends DialogWrapper {

    private static final int WIDTH = 400;

    private static final int BASE_LINE = 36;

    private JPanel dialog;

    private PsiClass psiClass;

    private TextFieldWithAutoCompletion<String> toField;

    private TextFieldWithAutoCompletion<String> fromField;

    private JCheckBox inheritFields;

    public ConverterDialog(PsiClass psiClass, boolean from, boolean to) {
        super(psiClass.getProject());
        this.psiClass = psiClass;
        this.dialog = createConverterDialog();
        List<String> classNamesForAutocompletion = getClassNamesForAutocompletion();

        this.inheritFields = new JCheckBox("Use inherited fields");
        if (from) {
            this.fromField = createTextField(classNamesForAutocompletion);
            LabeledComponent<TextFieldWithAutoCompletion<String>> convertFromComponent =
                    LabeledComponent.create(fromField, "Convert From class");
            dialog.add(convertFromComponent);
        }

        if (to) {
            this.toField = createTextField(classNamesForAutocompletion);
            LabeledComponent<TextFieldWithAutoCompletion<String>> convertToComponent =
                    LabeledComponent.create(toField, "Convert To class");
            dialog.add(convertToComponent);
        }

        dialog.add(this.inheritFields);

        init();
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        ValidationInfo toFieldValidation = validateTextField(toField, "Target");
        if (toFieldValidation == null) {
            return validateTextField(fromField, "From");
        }
        return toFieldValidation;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return dialog;
    }

    private JPanel createConverterDialog() {
        setTitle("Select Classes for Conversion");
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridLayout(0, 1));
        return jPanel;
    }

    private List<String> getClassNamesForAutocompletion() {
        Project project = this.psiClass.getProject();
        EditorHistoryManager editorHistoryManager = EditorHistoryManager.getInstance(project);
        PsiManager psiManager = PsiManager.getInstance(project);
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        List<String> classNames = new LinkedList<>();

        // history
        Collection<VirtualFile> historyJavaVirtualFiles = editorHistoryManager.getFileList()
                .stream()
                .filter(vf -> JavaFileType.INSTANCE.equals(vf.getFileType()))
                .distinct()
                .collect(Collectors.toList());
        PsiUtilCore.toPsiFiles(psiManager, historyJavaVirtualFiles)
                .stream()
                .map(psiFile -> (PsiJavaFile) psiFile)
                .flatMap(psiJavaFile -> Arrays.stream(psiJavaFile.getClasses())
                        .map(PsiClass::getQualifiedName)
                        .distinct()
                )
                .forEach(classNames::add);

        // project file
        Collection<VirtualFile> indexedJavaVirtualFiles = FileTypeIndex.getFiles(
                JavaFileType.INSTANCE,
                GlobalSearchScope.allScope(project));
        PsiUtilCore.toPsiFiles(psiManager, indexedJavaVirtualFiles)
                .stream()
                .map(psiFile -> (PsiJavaFile) psiFile)
                .flatMap(psiJavaFile -> Arrays.stream(psiJavaFile.getClasses())
                        .map(PsiClass::getQualifiedName)
                        .distinct()
                )
                .filter(qn -> !classNames.contains(qn))
                .distinct()
                .forEach(classNames::add);

        return classNames;
    }

    private TextFieldWithAutoCompletion<String> createTextField(List<String> classNames) {
        TextFieldWithAutoCompletion<String> textField = TextFieldWithAutoCompletion.create(psiClass.getProject(), classNames, true, null);
        textField.setPreferredSize(new Dimension(WIDTH, BASE_LINE));
        textField.setOneLineMode(true);
        return textField;
    }

    public PsiClass getConvertToClass() {
        return extractPsiClass(this.toField);
    }

    public PsiClass getConvertFromClass() {
        return extractPsiClass(this.fromField);
    }

    public boolean isInheritFields() {
        return this.inheritFields.isSelected();
    }

    private PsiClass extractPsiClass(TextFieldWithAutoCompletion<String> textField) {
        String className = textField.getText();
        if (className.isEmpty()) {
            throw new IllegalArgumentException("Should select smth");
        }
        Project project = this.psiClass.getProject();
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        PsiClass[] resolvedClasses = javaPsiFacade.findClasses(className, GlobalSearchScope.projectScope(project));
        if (resolvedClasses.length == 0) {
            throw new IllegalArgumentException("No such class found: " + className);
        }
        return resolvedClasses[0];
    }

    private ValidationInfo validateTextField(TextFieldWithAutoCompletion<String> textField, String fieldName) {
        if (textField == null) {
            return null;
        }
        String className = textField.getText();
        if (className.isEmpty()) {
            return new ValidationInfo(String.format("%s class should be selected", fieldName), textField);
        }
        Project project = this.psiClass.getProject();
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        PsiClass[] resolvedClasses = javaPsiFacade.findClasses(className, GlobalSearchScope.projectScope(project));
        if (resolvedClasses.length == 0) {
            return new ValidationInfo(String.format("Failed to find a class %s in the current project", className), textField);
        }
        return null;
    }
}
