WITH D(ob_date) AS (
  SELECT COALESCE(
  	(SELECT doc.date
	  FROM gl_opening_balances ob
	  JOIN documents doc ON doc.id = ob.id
	  WHERE doc.company_id = :companyId AND doc.archive IS NOT true AND doc.finished_gl
	  AND doc.date >= :startAccounting AND doc.date < :dateTo
	  ORDER BY doc.date DESC
	  LIMIT 1), :startAccounting)
)
SELECT operation_type, rc_id, rc_name, number, name, currency,
	CASE WHEN ob_deb > ob_cred THEN ob_deb-ob_cred ELSE NULL END AS ob_deb,
	CASE WHEN ob_deb < ob_cred THEN ob_cred-ob_deb ELSE NULL END AS ob_cred
FROM (
    SELECT 'all' AS operation_type, CAST(0 AS BIGINT) AS rc_id, NULL AS rc_name, A.number, COALESCE(gla.name, 'x') AS name, currency,
        SUM(COALESCE(debit00, 0) + COALESCE(debit0, 0)) AS ob_deb,
        SUM(COALESCE(credit00, 0) + COALESCE(credit0, 0)) AS ob_cred
    FROM (
        SELECT account_number AS number, COALESCE(debit_currency, credit_currency) AS currency,
            SUM(debit_amount) AS debit00,
            SUM(credit_amount) AS credit00,
            NULL AS debit0, NULL AS credit0, NULL AS debit, NULL AS credit
        FROM gl_ob_operations op
        JOIN documents doc ON doc.id = op.parent_id
        CROSS JOIN D
        WHERE doc.company_id = :companyId AND doc.date = D.ob_date AND doc.archive IS NOT true AND doc.finished_gl
        GROUP BY account_number, currency

        UNION
        SELECT debit_number, amount_currency, 0, 0, SUM(amount_amount), 0, 0, 0
        FROM gl_operations op
        JOIN double_entries doc ON doc.id = op.parent_id
        CROSS JOIN D
        WHERE doc.company_id = :companyId AND doc.date > D.ob_date AND doc.date <= :dateTo AND doc.archive IS NOT true AND doc.finished_gl
        GROUP BY debit_number, amount_currency

        UNION
        SELECT credit_number, amount_currency, 0, 0, 0, SUM(amount_amount), 0, 0
        FROM gl_operations op
        JOIN double_entries doc ON doc.id = op.parent_id
        CROSS JOIN D
        WHERE doc.company_id = :companyId AND doc.date > D.ob_date AND doc.date <= :dateTo AND doc.archive IS NOT true AND doc.finished_gl
        GROUP BY credit_number, amount_currency

    ) AS A
    LEFT JOIN gl_accounts gla ON gla.company_id = :companyId AND gla.number = A.number AND gla.archive IS NOT true
    WHERE gla.type = 'X' OR gla.type = 'I'
    GROUP BY A.number, gla.name, currency

    UNION

    SELECT 'rc' AS operation_type, A.rc_id, rc.name AS rc_name, A.number, COALESCE(gla.name, 'x') AS name, currency,
        SUM(COALESCE(debit00, 0) + COALESCE(debit0, 0)) AS ob_deb,
        SUM(COALESCE(credit00, 0) + COALESCE(credit0, 0)) AS ob_cred
    FROM (
        SELECT CAST(r.data->>'id' AS BIGINT) AS rc_id, account_number AS number, COALESCE(debit_currency, credit_currency) AS currency,
            SUM(debit_amount) AS debit00,
            SUM(credit_amount) AS credit00,
            NULL AS debit0, NULL AS credit0, NULL AS debit, NULL AS credit
        FROM gl_ob_operations op
        CROSS JOIN jsonb_array_elements(op.rc) AS r(data)
        JOIN documents doc ON doc.id = op.parent_id
        CROSS JOIN D
        WHERE doc.company_id = :companyId AND doc.date = D.ob_date AND doc.archive IS NOT true AND doc.finished_gl = true
        GROUP BY rc_id, account_number, currency

        UNION
        SELECT CAST(r.data->>'id' AS BIGINT) AS rc_id, debit_number, amount_currency, 0, 0, SUM(amount_amount), 0, 0, 0
        FROM gl_operations op
        CROSS JOIN jsonb_array_elements(op.debit_rc) AS r(data)
        JOIN double_entries doc ON doc.id = op.parent_id
        CROSS JOIN D
        WHERE doc.company_id = :companyId AND doc.date > D.ob_date AND doc.date <= :dateTo AND doc.archive IS NOT true AND doc.finished_gl
        GROUP BY rc_id, debit_number, amount_currency

        UNION
        SELECT CAST(r.data->>'id' AS BIGINT) AS rc_id, credit_number, amount_currency, 0, 0, 0, SUM(amount_amount), 0, 0
        FROM gl_operations op
        CROSS JOIN jsonb_array_elements(op.credit_rc) AS r(data)
        JOIN double_entries doc ON doc.id = op.parent_id
        CROSS JOIN D
        WHERE doc.company_id = :companyId AND doc.date > D.ob_date AND doc.date <= :dateTo AND doc.archive IS NOT true AND doc.finished_gl
        GROUP BY rc_id, credit_number, amount_currency

    ) AS A
    LEFT JOIN gl_accounts gla ON gla.company_id = :companyId AND gla.number = A.number AND gla.archive IS NOT true
    LEFT JOIN resp_centers rc ON rc.id = A.rc_id AND rc.archive IS NOT true AND rc.company_id = :companyId
    WHERE gla.type = 'X' OR gla.type = 'I'
    GROUP BY A.rc_id, rc_name, A.number, gla.name, currency
) AS AA
ORDER BY operation_type, rc_name, rc_id, number, name, currency
