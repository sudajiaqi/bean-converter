package com.jiaqi.converter;

import com.intellij.psi.PsiClass;
import com.jiaqi.converter.utils.ProjectUtil;
import com.jiaqi.converter.utils.SuggestionName;
import org.jetbrains.annotations.NotNull;

/**
 * @author jiaqi
 */
public class GenerateToMethod implements GenerateMethod {

    private final ClassMapResult mapResult;

    private final String toClassName;
    private final String toName;

    public GenerateToMethod(ClassMapResult mapResult) {
        this.mapResult = mapResult;
        this.toClassName = mapResult.getTo().getName();
        this.toName = SuggestionName.get(mapResult.getTo());
    }

    @NotNull
    private StringBuilder buildMethodSignature() {
        StringBuilder builder = new StringBuilder("public ");
        builder.append(this.toClassName);
        builder.append(" to");
        builder.append(this.toClassName);
        builder.append("() {\n");
        builder.append(this.toClassName)
                .append(" ")
                .append(this.toName)
                .append(" = new ")
                .append(this.toClassName).append("();\n");
        return builder;
    }


    @Override
    public String generate() {

        StringBuilder builder = buildMethodSignature();
        String indentation = ProjectUtil.getProjectIndentation(mapResult.getFrom());

        builder.append(writeMappedFields());
        builder.append(writeNotMappedFields(mapResult.getNotMappedToFields(), indentation, this.toClassName));
        builder.append(writeNotMappedFields(mapResult.getNotMappedFromFields(), indentation, mapResult.getFrom().getName()));

        builder.append("return ").append(this.toName).append(";\n}");
        return builder.toString();
    }


    @NotNull
    private String writeMappedFields() {
        StringBuilder builder = new StringBuilder();
        for (String fieldName : this.mapResult.getMappedFields()) {
            builder.append(this.toName).append(".")
                    .append(mapResult.getSetter(fieldName))
                    .append("(this.")
                    .append(fieldName)
                    .append(");\n");
        }
        return builder.toString();
    }

}
