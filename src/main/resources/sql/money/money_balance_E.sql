SELECT R.account_id, R.currency, R.ob, R.debit, R.credit, R.b_currency, R.b_ob, R.b_debit, R.b_credit,
	CASE WHEN ACC.acc_number IS NOT NULL THEN ACC.acc_number ELSE ACN.acc_number END AS account_number,
	G.name AS account_name,
	E.name, E.office, E.department, E.used_currencies
FROM (
    SELECT account_id, exchange_currency AS currency,
        SUM(CASE WHEN doc_date < :dateFrom THEN amount_amount END) ob,
        SUM(CASE WHEN doc_date >= :dateFrom AND amount_amount > 0 THEN amount_amount END) debit,
        SUM(CASE WHEN doc_date >= :dateFrom AND amount_amount < 0 THEN -amount_amount END) credit,
        base_amount_currency AS b_currency,
        SUM(CASE WHEN doc_date < :dateFrom THEN base_amount_amount END) b_ob,
        SUM(CASE WHEN doc_date >= :dateFrom AND base_amount_amount > 0 THEN base_amount_amount END) b_debit,
        SUM(CASE WHEN doc_date >= :dateFrom AND base_amount_amount < 0 THEN -base_amount_amount END) b_credit
    FROM money_history
    WHERE company_id = :companyId
        AND account_type = 'E' -- EMPLOYEE
        AND doc_date < :dateTo
        AND (0 = :accountId OR account_id = :accountId)
        AND ('' = :currency OR exchange_currency = :currency)
    GROUP BY account_id, exchange_currency, base_amount_currency
) R
LEFT JOIN
  (SELECT id, D->'account'->>'name' acc_name, D->'account'->>'number' acc_number, D->>'currency' currency
		FROM employee
		LEFT JOIN jsonb_array_elements(money_account_accounts) AS D ON TRUE
		WHERE company_id = :companyId
	) AS ACC
ON R.account_id = ACC.id AND R.currency = ACC.currency
LEFT JOIN
  (SELECT id, D->'account'->>'name' acc_name, D->'account'->>'number' acc_number, D->>'currency' currency
		FROM employee
		LEFT JOIN jsonb_array_elements(money_account_accounts) AS D ON TRUE
		WHERE company_id = :companyId
	) AS ACN
ON R.account_id = ACN.id AND (ACN.currency IS NULL OR ACN.currency = '')
LEFT JOIN gl_accounts G ON G.company_id = :companyId
	AND G.number = CASE WHEN ACC.acc_number IS NOT NULL THEN ACC.acc_number ELSE ACN.acc_number END
	AND (NOT G.archive OR G.archive IS NULL)
LEFT JOIN employee E ON E.id = R.account_id
WHERE (R.ob IS NOT null AND R.ob <> 0)
  OR (R.debit IS NOT null AND R.debit <> 0)
  OR (R.credit IS NOT null AND R.credit <> 0)
  OR (R.b_ob IS NOT null AND R.b_ob <> 0)
  OR (R.b_debit IS NOT null AND R.b_debit <> 0)
  OR (R.b_credit IS NOT null AND R.b_credit <> 0)
ORDER BY E.name, E.office, E.department, R.account_id, R.currency


