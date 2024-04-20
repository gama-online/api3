WITH D AS (
    SELECT
    	CAST(
    		date_trunc(:timeInterval, CAST(date_trunc(:timeInterval, CAST(:dateFrom AS date)) AS date) - 1)
        AS date) AS dt,
    	account_id,
    	base_amount_currency AS base_currency,
        SUM(base_amount_amount) AS base_amount,
        null AS base_debit_amount,
        null AS base_credit_amount
    FROM money_history
    WHERE company_id = :companyId
        AND account_type = 'B' -- BANK
        AND (account_id = :accountId OR 0 = :accountId)
        AND date_trunc(:timeInterval, doc_date) < date_trunc(:timeInterval, CAST(:dateFrom AS date))
    GROUP BY account_id, base_amount_currency
    UNION
    SELECT CAST(date_trunc(:timeInterval, doc_date) AS date) AS dt, account_id, base_amount_currency,
        SUM(base_amount_amount) base_amount,
        SUM(CASE WHEN base_amount_amount > 0 THEN base_amount_amount ELSE 0 END) AS base_debit_amount,
        SUM(CASE WHEN base_amount_amount < 0 THEN base_amount_amount ELSE 0 END) AS base_credit_amount
    FROM money_history
    WHERE company_id = :companyId
        AND account_type = 'B' -- BANK
        AND (account_id = :accountId OR 0 = :accountId)
        AND date_trunc(:timeInterval, doc_date) >= date_trunc(:timeInterval, CAST(:dateFrom AS date))
        AND date_trunc(:timeInterval, doc_date) <= date_trunc(:timeInterval, CAST(:dateTo AS date))
    GROUP BY account_id, base_amount_currency, dt
),
E AS (
	SELECT D.account_id, D.dt, D.base_currency,
		D.base_amount, D.base_debit_amount, D.base_credit_amount,
		SUM(D.base_amount)
			OVER (PARTITION BY D.account_id ORDER BY D.dt ASC ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS base_balance_amount
	FROM D
)
SELECT A.account AS account_name, dt, E.account_id, base_currency,
	base_amount, base_debit_amount, base_credit_amount,
    base_balance_amount
FROM E
LEFT JOIN bank_accounts A ON A.id = E.account_id
ORDER BY account_name, E.account_id, dt
