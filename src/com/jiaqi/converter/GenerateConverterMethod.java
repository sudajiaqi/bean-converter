package com.jiaqi.converter;

import com.intellij.psi.PsiClass;
import com.jiaqi.converter.ClassMapResult;
import com.jiaqi.converter.GenerateMethod;
import com.jiaqi.converter.utils.ProjectUtil;
import com.jiaqi.converter.utils.SuggestionName;
import org.jetbrains.annotations.NotNull;

/**
 * @author jiaqi
 */
public class GenerateConverterMethod implements GenerateMethod {

    private final ClassMapResult mapResult;

    private final String toClassName;
    private final String toName;
    private final String fromName;

    private final String fromClassName;

    public GenerateConverterMethod(ClassMapResult mapResult) {
        this.mapResult = mapResult;
        this.toClassName = mapResult.getTo().getQualifiedName();
        this.fromClassName = mapResult.getFrom().getQualifiedName();
        this.fromName = SuggestionName.get(mapResult.getFrom());
        this.toName = SuggestionName.get(mapResult.getTo());
    }

    @NotNull
    private StringBuilder buildMethodSignature() {

        StringBuilder builder = new StringBuilder("public static ");
        builder.append(this.toClassName);
        builder.append(" to").append(mapResult.getTo().getName());
        builder.append("(").append(this.fromClassName).append(" ").append(this.fromName).append(") {\n");
        builder.append("if (").append(this.fromName).append(" == null) {\nreturn null;\n}\n");
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
        builder.append(writeNotMappedFields(mapResult.getNotMappedToFields(), indentation, mapResult.getTo().getQualifiedName()));
        builder.append(writeNotMappedFields(mapResult.getNotMappedFromFields(), indentation, mapResult.getFrom().getQualifiedName()));

        builder.append("return ").append(this.toName).append(";\n}");

        return builder.toString();
    }


    @NotNull
    private String writeMappedFields() {
        StringBuilder builder = new StringBuilder();
        for (String fieldName : this.mapResult.getMappedFields()) {
            builder.append(this.toName).append(".")
                    .append(mapResult.getSetter(fieldName))
                    .append("(").append(this.fromName).append(".")
                    .append(mapResult.getGetter(fieldName))
                    .append("());\n");
        }
        return builder.toString();
    }

}
