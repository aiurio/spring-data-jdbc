package io.aiur.oss.db.jdbc.jdbc.nurkiewicz.sql;

import io.aiur.oss.db.jdbc.jdbc.nurkiewicz.TableDescription;
import org.springframework.data.domain.Pageable;

/**
 * @author Tomasz Nurkiewicz
 * @since 1/16/13, 10:25 PM
 */
public class DerbySqlGenerator extends SqlGenerator {

	public static final String ROW_NUM_COLUMN = "ROW_NUM";
	public static final String ROW_NUM_COLUMN_CLAUSE = "SELECT * FROM (SELECT ROW_NUMBER() OVER () AS " + ROW_NUM_COLUMN + ", t.* FROM (";

	public DerbySqlGenerator(String allColumnsClause) {
		super(allColumnsClause);
	}

	public DerbySqlGenerator() {
	}

	@Override
	public String selectAll(TableDescription table, Pageable page) {
		final int offset = page.getPageNumber() * page.getPageSize();
		return super.selectAll(table, page) + " OFFSET " + offset + " ROWS FETCH NEXT " + page.getPageSize() + " ROWS ONLY";
	}

	@Override
	public String limitClause(Pageable page) {
		return "";
	}
}
