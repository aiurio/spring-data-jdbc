package io.aiur.oss.db.jdbc.jdbc.nurkiewicz.sql;

import io.aiur.oss.db.jdbc.jdbc.nurkiewicz.TableDescription;
import org.springframework.data.domain.Pageable;

/**
 * Author: tom
 */
public class OracleSqlGenerator extends SqlGenerator {
	public OracleSqlGenerator() {
	}

	public OracleSqlGenerator(String allColumnsClause) {
		super(allColumnsClause);
	}

	@Override
	public String limitClause(Pageable page) {
		return "";
	}

	@Override
	public String selectAll(TableDescription table, Pageable page) {
		return SQL99Helper.generateSelectAllWithPagination(table, page, this);
	}
}
