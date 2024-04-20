WITH RECURSIVE rec AS
(
	SELECT a.number, a.name, a.parent, a.parent AS rec_parent, a.saft_number, 1 AS debt
	FROM gl_accounts a
	WHERE a.company_id = :companyId
		AND a.inner = false
		AND (a.archive IS null OR a.archive = false)
		AND (a.hidden IS null OR a.hidden = false)

	UNION ALL

	SELECT a.number, a.name, a.parent, p.parent AS rec_parent, p.saft_number, debt+1 AS debt
	FROM rec a
	INNER JOIN gl_accounts p ON p.company_id = :companyId
		AND (a.saft_number IS NULL OR a.saft_number = '')
		AND p.number = a.rec_parent
	WHERE debt < 5
),
gla AS
(
	SELECT b.number, b.name, b.parent, b.rec_parent, b.saft_number
	FROM (SELECT a.number, a.name, a.parent, a.saft_number, a,rec_parent, a.debt,
			RANK() OVER(PARTITION BY a.number ORDER BY a.debt DESC) as r
			FROM rec a) b
	WHERE b.r = 1
),
D(ob_date) AS (
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
	saft.number AS saft_number, saft.name AS saft_name, saft.type AS saft_type,
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
LEFT JOIN gla ON gla.number = A.number
lEFT JOIN gl_saft_accounts saft ON saft.number = gla.saft_number
GROUP BY A.number, gla.name, currency, saft.number, saft.name, saft.type
HAVING currency IS NOT NULL
ORDER BY A.number, gla.name, currency
