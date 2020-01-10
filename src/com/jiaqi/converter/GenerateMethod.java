package com.jiaqi.converter;

import java.util.List;

/**
 * @author jiaqi
 */
public interface GenerateMethod {

    String generate();

    default String writeNotMappedFields(List<String> notMappedFields, String indentation, String sourceType) {
        StringBuilder builder = new StringBuilder();
        if (!notMappedFields.isEmpty()) {
            builder.append("\n")
                    .append(indentation)
                    .append("// Not mapped ")
                    .append(sourceType)
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
}
