package io.aiur.oss.db.jdbc.jdbc.nurkiewicz.sql;

import org.springframework.data.domain.Pageable;

/**
 * @author Tomasz Nurkiewicz
 * @since 1/15/13, 11:03 PM
 */
public class PostgreSqlGenerator extends SqlGenerator {
	@Override
	public String limitClause(Pageable page) {
		final int offset = page.getPageNumber() * page.getPageSize();
		return " LIMIT " + page.getPageSize() + " OFFSET " + offset;
	}
}
