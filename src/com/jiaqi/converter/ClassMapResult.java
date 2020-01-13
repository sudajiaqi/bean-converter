package com.jiaqi.converter;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.jiaqi.converter.utils.ProjectUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class ClassMapResult {
    private PsiClass from;

    private PsiClass to;

    private Set<String> mappedFields = new LinkedHashSet<>();

    private List<String> notMappedToFields = new LinkedList<>();

    private List<String> notMappedFromFields = new LinkedList<>();

    public Set<String> getMappedFields() {
        return this.mappedFields;
    }

    public List<String> getNotMappedToFields() {
        return this.notMappedToFields;
    }

    public List<String> getNotMappedFromFields() {
        return this.notMappedFromFields;
    }

    private void addMappedField(String field) {
        mappedFields.add(field);
    }

    private void addNotMappedToField(String toField) {
        notMappedToFields.add(toField);
    }

    private void addNotMappedFromField(String fromField) {
        notMappedFromFields.add(fromField);
    }

    public static ClassMapResult from(PsiClass to, PsiClass from, boolean inherited) {
        ClassMapResult result = new ClassMapResult();
        result.from = from;
        result.to = to;
        processToFields(to, from, result, inherited);
        processFromFields(from, result, inherited);

        return result;
    }

    private static void processToFields(PsiClass to, PsiClass from, ClassMapResult mappingResult, boolean useInherited) {
        for (PsiField toField : getFields(to, useInherited)) {
            String fieldName = toField.getName();
            if (fieldName != null && !toField.hasModifier(JvmModifier.STATIC)) {
                PsiMethod toSetter = findSetter(to, fieldName, useInherited);
                PsiMethod fromGetter = findGetter(from, fieldName, useInherited);
                if (toSetter != null && fromGetter != null && isMatchingFieldType(toField, fromGetter)) {
                    mappingResult.addMappedField(fieldName);
                } else {
                    mappingResult.addNotMappedToField(fieldName);
                }
            }
        }
    }

    private static void processFromFields(PsiClass from, ClassMapResult mappingResult, boolean useInherited) {
        for (PsiField fromField : getFields(from, useInherited)) {
            String fromFieldName = fromField.getName();
            if (fromFieldName != null
                    && !fromField.hasModifier(JvmModifier.STATIC)
                    && !mappingResult.getMappedFields().contains(fromFieldName)) {
                mappingResult.addNotMappedFromField(fromFieldName);
            }
        }
    }

    @NotNull
    private static PsiField[] getFields(PsiClass clazz, boolean useInherited) {
        PsiField[] fields;
        if (useInherited) {
            fields = clazz.getAllFields();
        } else {
            fields = clazz.getFields();
        }
        return fields;
    }

    private static PsiMethod findSetter(PsiClass psiClass, String fieldName, boolean useInherited) {
        PsiMethod[] setters = psiClass.findMethodsByName("set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), useInherited);
        if (setters.length == 1) {
            return setters[0];
        }
        return null;
    }

    private static PsiMethod findGetter(PsiClass psiClass, String fieldName, boolean useInherited) {
        String methodSuffix = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        PsiMethod[] getters = psiClass.findMethodsByName("get" + methodSuffix, useInherited);
        if (getters.length > 0) {
            return getters[0];
        }
        getters = psiClass.findMethodsByName("is" + methodSuffix, false);
        if (getters.length > 0) {
            return getters[0];
        }
        return null;
    }

    private static boolean isMatchingFieldType(PsiField toField, PsiMethod fromGetter) {
        PsiType fromGetterReturnType = fromGetter.getReturnType();
        PsiType toFieldType = toField.getType();
        return fromGetterReturnType != null && toFieldType.isAssignableFrom(fromGetterReturnType);
    }


    public String getGetter(String field) {
        String methodSuffix = field.substring(0, 1).toUpperCase() + field.substring(1);
        PsiMethod[] getters = from.findMethodsByName("get" + methodSuffix, true);
        if (getters.length > 0) {
            return getters[0].getName();
        }
        getters = from.findMethodsByName("is" + methodSuffix, true);
        if (getters.length > 0) {
            return getters[0].getName();
        }
        return null;
    }

    public String getSetter(String field) {
        String methodSuffix = field.substring(0, 1).toUpperCase() + field.substring(1);
        PsiMethod[] getters = from.findMethodsByName("set" + methodSuffix, true);
        if (getters.length > 0) {
            return getters[0].getName();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClassMapResult that = (ClassMapResult) o;

        return new EqualsBuilder()
                .append(mappedFields, that.mappedFields)
                .append(notMappedToFields, that.notMappedToFields)
                .append(notMappedFromFields, that.notMappedFromFields)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(mappedFields)
                .append(notMappedToFields)
                .append(notMappedFromFields)
                .toHashCode();
    }

    public PsiClass getFrom() {
        return from;
    }

    public PsiClass getTo() {
        return to;
    }

    private String writeNotMappedFields(List<String> notMappedFields, PsiClass psiClass) {
        String indentation = ProjectUtil.getProjectIndentation(psiClass);
        StringBuilder builder = new StringBuilder();
        if (!notMappedFields.isEmpty()) {
            builder.append("\n")
                    .append(indentation)
                    .append("// Not mapped ")
                    .append(psiClass.getName())
                    .append(" fields: \n");
        }
        for (String notMappedField : notMappedFields) {
            builder.append(indentation)
                    .append("// ")
                    .append(notMappedField)
                    .append("\n");
        }
        return builder.toString();
    }

    public String writeNotMappedFields() {
        return writeNotMappedFields(this.notMappedFromFields, this.from) + writeNotMappedFields(this.notMappedToFields, this.to);
    }

}
