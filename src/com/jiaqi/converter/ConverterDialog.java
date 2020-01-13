package com.jiaqi.converter;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


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
            LabeledComponent<TextFieldWithAutoCompletion> convertFromComponent = LabeledComponent.create(fromField, "Convert From class");
            dialog.add(convertFromComponent);
        }

        if (to) {
            this.toField = createTextField(classNamesForAutocompletion);
            LabeledComponent<TextFieldWithAutoCompletion> convertToComponent = LabeledComponent.create(toField, "Convert To class");
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
        List<String> history = Stream.of(EditorHistoryManager.getInstance(psiClass.getProject()).getFiles())
                .map(VirtualFile::getNameWithoutExtension)
                .distinct()
                .collect(toList());

        List<String> projectFiles = FileBasedIndex.getInstance()
                .getContainingFiles(
                        ID.create("filetypes"),
                        JavaFileType.INSTANCE,
                        GlobalSearchScope.allScope(psiClass.getProject())
                ).stream()
                .map(VirtualFile::getNameWithoutExtension)
                .collect(toList());

        history.addAll(projectFiles);
        return history;
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
        PsiClass[] resolvedClasses = PsiShortNamesCache.getInstance(psiClass.getProject()).getClassesByName(className, GlobalSearchScope.projectScope(psiClass.getProject()));
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
        PsiClass[] resolvedClasses = PsiShortNamesCache.getInstance(psiClass.getProject()).getClassesByName(className, GlobalSearchScope.projectScope(psiClass.getProject()));
        if (resolvedClasses.length == 0) {
            return new ValidationInfo(String.format("Failed to find a class %s in the current project", className), textField);
        }
        return null;
    }
}
