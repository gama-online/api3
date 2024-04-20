WITH D(ob_date) AS (
    SELECT COALESCE((
        SELECT doc.date
        FROM debt_opening_balances ob
        JOIN documents doc ON doc.id = ob.id
        WHERE doc.company_id = :companyId AND doc.archive IS NOT true AND ob.finished_debt
        AND doc.date >= :startAccounting AND doc.date < :dateFrom
        ORDER BY doc.date DESC
        LIMIT 1), :startAccounting)
), R(counterparty_id, b_ob, b_debit, b_credit, b_total,
        b_ob_total, b_debit_total, b_credit_total, b_debt_total) AS (
    SELECT counterparty_id,
		SUM(CASE WHEN doc_date < :dateFrom THEN base_debt_amount END) b_ob,
		SUM(CASE WHEN doc_date >= :dateFrom AND base_debt_amount > 0 THEN base_debt_amount END) b_debit,
		SUM(CASE WHEN doc_date >= :dateFrom AND base_debt_amount < 0 THEN -base_debt_amount END) b_credit,
		COUNT(counterparty_id) OVER(),

		SUM(SUM(CASE WHEN doc_date < :dateFrom THEN base_debt_amount END)) OVER(),
        SUM(SUM(CASE WHEN doc_date >= :dateFrom AND base_debt_amount > 0 THEN base_debt_amount END)) OVER(),
        SUM(SUM(CASE WHEN doc_date >= :dateFrom AND base_debt_amount < 0 THEN -base_debt_amount END)) OVER(),
        SUM(SUM(CASE WHEN doc_date < :dateFrom THEN base_debt_amount END)) OVER()
	FROM debt_history
 	CROSS JOIN D
	WHERE company_id = :companyId
		AND ((doc_date = D.ob_date AND doc_type = 'DebtOpeningBalance') OR
		     (doc_date > D.ob_date AND doc_date < :dateTo AND (doc_type IS NULL OR doc_type <> 'DebtOpeningBalance')))
		AND type = 'V'
	GROUP BY counterparty_id
)
SELECT R.counterparty_id,
	R.b_ob, R.b_debit, R.b_credit, R.b_total,
	R.b_ob_total, R.b_debit_total, R.b_credit_total, R.b_debt_total,
	CP.name, CP.short_name, CP.com_code, CP.vat_code,
	CP.registration_address_zip, CP.registration_address_city, CP.registration_address_country,
	CP.business_address_zip, CP.business_address_city, CP.business_address_country,
	CP.post_address_zip, CP.post_address_city, CP.post_address_country,
	CP.accounts->'V'->>'number' AS account_number,
    G.name AS account
FROM R
LEFT JOIN counterparties CP ON CP.id = R.counterparty_id
LEFT JOIN gl_accounts G ON G.company_id = :companyId
	AND G.number = CP.accounts->'V'->>'number'
	AND (G.archive IS NOT true)
	AND (G.hidden IS NOT true)
WHERE (b_ob <> 0 OR b_debit <> 0 OR b_credit <> 0)
ORDER BY CP.name, CP.com_code, R.counterparty_id, account
