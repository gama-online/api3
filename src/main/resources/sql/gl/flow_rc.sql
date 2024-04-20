WITH D(ob_date) AS (
  SELECT COALESCE(
  	(SELECT doc.date
	  FROM gl_opening_balances ob
	  JOIN documents doc ON doc.id = ob.id
	  WHERE doc.company_id = :companyId AND doc.archive IS NOT true AND doc.finished_gl
	  AND doc.date >= :startAccounting AND doc.date < :dateFrom
	  ORDER BY doc.date DESC
	  LIMIT 1), :startAccounting)
)
SELECT rc.id AS rc_id, rc.name AS rc_name, A.number, COALESCE(gla.name, 'x') AS name, currency,
    SUM(COALESCE(debit00, 0)) - SUM(COALESCE(credit00, 0)) + SUM(COALESCE(debit0, 0)) - SUM(COALESCE(credit0, 0)) AS ob,
    SUM(COALESCE(debit, 0)) AS debit, SUM(COALESCE(credit, 0)) AS credit
FROM (
    SELECT account_number AS number, COALESCE(debit_currency, credit_currency) AS currency,
		SUM(debit_amount) AS debit00,
	    SUM(credit_amount) AS credit00,
	    null AS debit0, null AS credit0, null AS debit, null AS credit
	FROM gl_ob_operations op
    JOIN documents doc ON doc.id = op.parent_id
 	CROSS JOIN D
	WHERE doc.company_id = :companyId AND doc.date = D.ob_date AND doc.archive IS NOT true AND doc.finished_gl
		AND account_number = :accountNumber AND jsonb_path_query_array(rc, '$[*].id') @> CAST(CAST(:rcId AS text) AS jsonb)
	GROUP BY account_number, currency

    UNION
	SELECT debit_number, amount_currency, 0, 0, SUM(amount_amount), 0, 0, 0
	FROM gl_operations op
	JOIN double_entries doc ON doc.id = op.parent_id
 	CROSS JOIN D
	WHERE doc.company_id = :companyId AND doc.date > D.ob_date AND doc.date < :dateFrom AND doc.archive IS NOT true AND doc.finished_gl
		AND debit_number = :accountNumber AND jsonb_path_query_array(debit_rc, '$[*].id') @> CAST(CAST(:rcId AS text) AS jsonb)
	GROUP BY debit_number, amount_currency

    UNION
	SELECT credit_number, amount_currency, 0, 0, 0, SUM(amount_amount), 0, 0
	FROM gl_operations op
    JOIN double_entries doc ON doc.id = op.parent_id
 	CROSS JOIN D
	WHERE doc.company_id = :companyId AND doc.date > D.ob_date AND doc.date < :dateFrom AND doc.archive IS NOT true AND doc.finished_gl
		AND credit_number = :accountNumber AND jsonb_path_query_array(credit_rc, '$[*].id') @> CAST(CAST(:rcId AS text) AS jsonb)
	GROUP BY credit_number, amount_currency

	UNION
	SELECT debit_number, amount_currency, 0, 0, 0, 0, SUM(amount_amount), 0
	FROM gl_operations op
    JOIN double_entries doc ON doc.id = op.parent_id
	WHERE doc.company_id = :companyId AND doc.date BETWEEN :dateFrom AND :dateTo AND doc.archive IS NOT true AND doc.finished_gl
	    AND debit_number = :accountNumber AND jsonb_path_query_array(debit_rc, '$[*].id') @> CAST(CAST(:rcId AS text) AS jsonb)
	GROUP BY debit_number, amount_currency

	UNION
	SELECT credit_number, amount_currency, 0, 0, 0, 0, 0, SUM(amount_amount)
	FROM gl_operations op
    JOIN double_entries doc ON doc.id = op.parent_id
	WHERE doc.company_id = :companyId AND doc.date BETWEEN :dateFrom AND :dateTo AND doc.archive IS NOT true AND doc.finished_gl
	    AND credit_number = :accountNumber AND jsonb_path_query_array(credit_rc, '$[*].id') @> CAST(CAST(:rcId AS text) AS jsonb)
	GROUP BY credit_number, amount_currency
) AS A
LEFT JOIN gl_accounts gla ON gla.company_id = :companyId AND gla.number = A.number AND gla.archive IS NOT true
LEFT JOIN resp_centers rc ON rc.id = :rcId AND rc.archive IS NOT true AND rc.company_id = :companyId
GROUP BY rc.id, rc_name, A.number, gla.name, currency
HAVING currency IS NOT NULL
ORDER BY rc_id, rc_name, number, name, currency



