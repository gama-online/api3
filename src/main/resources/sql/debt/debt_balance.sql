WITH D(ob_date) AS (
    SELECT COALESCE((
        SELECT doc.date
        FROM debt_opening_balances ob
        JOIN documents doc ON doc.id = ob.id
        WHERE doc.company_id = :companyId AND doc.archive IS NOT true AND ob.finished_debt
        AND doc.date >= :startAccounting AND doc.date < :dateFrom
        ORDER BY doc.date DESC
        LIMIT 1), :startAccounting)
), R(counterparty_id, type, currency, ob, debit, credit, b_currency, b_ob, b_debit, b_credit,
        b_ob_total, b_debit_total, b_credit_total, b_debt_total) AS (
    SELECT counterparty_id, type,
		debt_currency AS currency,
		SUM(CASE WHEN doc_date < :dateFrom THEN debt_amount END) ob,
		SUM(CASE WHEN doc_date >= :dateFrom AND debt_amount > 0 THEN debt_amount END) debit,
		SUM(CASE WHEN doc_date >= :dateFrom AND debt_amount < 0 THEN -debt_amount END) credit,
		base_debt_currency AS b_currency,
		SUM(CASE WHEN doc_date < :dateFrom THEN base_debt_amount END) b_ob,
		SUM(CASE WHEN doc_date >= :dateFrom AND base_debt_amount > 0 THEN base_debt_amount END) b_debit,
		SUM(CASE WHEN doc_date >= :dateFrom AND base_debt_amount < 0 THEN -base_debt_amount END) b_credit,

		SUM(SUM(CASE WHEN doc_date < :dateFrom THEN base_debt_amount END)) OVER(),
        SUM(SUM(CASE WHEN doc_date >= :dateFrom AND base_debt_amount > 0 THEN base_debt_amount END)) OVER(),
        SUM(SUM(CASE WHEN doc_date >= :dateFrom AND base_debt_amount < 0 THEN -base_debt_amount END)) OVER(),
        SUM(SUM(CASE WHEN doc_date < :dateFrom THEN base_debt_amount END)) OVER()
	FROM debt_history
	CROSS JOIN D
	WHERE company_id = :companyId
		AND ((doc_date = D.ob_date AND doc_type = 'DebtOpeningBalance') OR
		     (doc_date > D.ob_date AND doc_date < :dateTo AND (doc_type IS NULL OR doc_type <> 'DebtOpeningBalance')))
		AND (0 = :counterpartyId OR counterparty_id = :counterpartyId)
		AND ('' = :currency OR debt_currency = :currency)
		AND ('' = :type OR type = :type)
	GROUP BY counterparty_id, debt_currency, type, base_debt_currency
)
SELECT R.counterparty_id, R.type,
    R.currency, R.ob, R.debit, R.credit,
	R.b_currency, R.b_ob, R.b_debit, R.b_credit,
	COUNT(*) OVER() AS total,
	R.b_ob_total, R.b_debit_total, R.b_credit_total, R.b_debt_total,
	CP.name, CP.short_name, CP.com_code, CP.vat_code, CP.used_currencies,
	CASE
	    WHEN R.type = 'V' THEN CP.accounts->'V'->>'number'
        WHEN R.type = 'C' THEN CP.accounts->'C'->>'number'
    END AS account_number,
    G.name AS account
FROM R
LEFT JOIN counterparties CP ON CP.id = R.counterparty_id
LEFT JOIN gl_accounts G ON G.company_id = :companyId
	AND G.number = CASE
	                WHEN R.type = 'V' THEN CP.accounts->'V'->>'number'
	                WHEN R.type = 'C' THEN CP.accounts->'C'->>'number'
	               END
	AND (G.archive IS NOT true)
	AND (G.hidden IS NOT true)
WHERE (ob <> 0 OR debit <> 0 OR credit <> 0 OR b_ob <> 0 OR b_debit <> 0 OR b_credit <> 0)
    AND (
        '' = :filter
        OR trim(unaccent(CP.name)) ILIKE :filter
        OR trim(regexp_replace(unaccent(CP.name), '[^[:alnum:]]+', ' ', 'g')) ILIKE :filter
	    OR trim(CP.com_code) ILIKE :filter
	    OR CASE WHEN R.type = 'V' THEN trim(unaccent(CP.accounts->'V'->>'name')) END ILIKE :filter
	    OR CASE WHEN R.type = 'C' THEN trim(unaccent(CP.accounts->'C'->>'name')) END ILIKE :filter
    )
ORDER BY lower(unaccent(CP.name)), CP.com_code, R.counterparty_id, R.type, R.currency, account
