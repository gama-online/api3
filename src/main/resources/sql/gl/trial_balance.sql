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
SELECT A.number, COALESCE(gla.name, 'x') AS name, currency,
    SUM(COALESCE(debit00, 0) - COALESCE(credit00, 0) + COALESCE(debit0, 0) - COALESCE(credit0, 0)) AS ob,
    SUM(COALESCE(debit, 0)) AS debit, SUM(COALESCE(credit, 0)) AS credit
FROM (
	SELECT account_number AS number, COALESCE(debit_currency, credit_currency) AS currency,
		SUM(debit_amount) AS debit00,
		SUM(credit_amount) AS credit00,
		NULL AS debit0, NULL AS credit0, NULL AS debit, NULL AS credit
	FROM gl_ob_operations op
	JOIN documents doc ON doc.id = op.parent_id
 	CROSS JOIN D
	WHERE doc.company_id = :companyId AND doc.date = D.ob_date AND doc.archive IS NOT true AND doc.finished_gl = true
	GROUP BY account_number, currency

	UNION
	SELECT debit_number, amount_currency, 0, 0, SUM(amount_amount), 0, 0, 0
	FROM gl_operations op
	JOIN double_entries doc ON doc.id = op.parent_id
 	CROSS JOIN D
	WHERE doc.company_id = :companyId AND doc.date > D.ob_date AND doc.date < :dateFrom AND doc.archive IS NOT true AND doc.finished_gl
	GROUP BY debit_number, amount_currency

	UNION
	SELECT credit_number, amount_currency, 0, 0, 0, SUM(amount_amount), 0, 0
	FROM gl_operations op
	JOIN double_entries doc ON doc.id = op.parent_id
 	CROSS JOIN D
	WHERE doc.company_id = :companyId AND doc.date > D.ob_date AND doc.date < :dateFrom AND doc.archive IS NOT true AND doc.finished_gl
	GROUP BY credit_number, amount_currency

	UNION
	SELECT debit_number, amount_currency, 0, 0, 0, 0, SUM(amount_amount), 0
	FROM gl_operations op
	JOIN double_entries doc ON doc.id = op.parent_id
	WHERE doc.company_id = :companyId AND doc.date BETWEEN :dateFrom AND :dateTo AND doc.archive IS NOT true AND doc.finished_gl
	GROUP BY debit_number, amount_currency

	UNION
	SELECT credit_number, amount_currency, 0, 0, 0, 0, 0, SUM(amount_amount)
	FROM gl_operations op
	JOIN double_entries doc ON doc.id = op.parent_id
	WHERE doc.company_id = :companyId AND doc.date BETWEEN :dateFrom AND :dateTo AND doc.archive IS NOT true AND doc.finished_gl
	GROUP BY credit_number, amount_currency
) AS A
LEFT JOIN gl_accounts gla ON gla.company_id = :companyId AND gla.number = A.number AND gla.archive IS NOT true
GROUP BY A.number, gla.name, currency
HAVING currency IS NOT NULL
ORDER BY A.number, gla.name, currency
