package io.aiur.oss.db.jdbc.jdbc.convert.impl.joda.editor;

import org.joda.time.LocalDateTime;

import java.beans.PropertyEditorSupport;

public class JodaLocalDateTimeEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(final String text) throws IllegalArgumentException {
        setValue(new LocalDateTime(text));
    }
    @Override
    public void setValue(final Object value) {
        super.setValue(value == null || value instanceof LocalDateTime ? value
                : new LocalDateTime(value));
    }
    @Override
    public LocalDateTime getValue() {
        return (LocalDateTime) super.getValue();
    }
    @Override
    public String getAsText() {
        return getValue().toString();
    }
}