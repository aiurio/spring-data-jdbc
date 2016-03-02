package io.aiur.oss.db.jdbc.jdbc.nurkiewicz;

import java.util.Map;

public interface RowUnmapper<T> {
	Map<String, Object> mapColumns(T t);
}

