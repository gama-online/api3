WITH D AS (
    SELECT CAST(date_trunc(:timeInterval, CAST(:dateFrom AS date)) AS date) - 1 AS dt,
    	base_debt_currency AS base_currency,
        SUM(base_debt_amount) AS base_amount,
        null AS base_debit_amount,
        null AS base_credit_amount
    FROM debt_history
    WHERE company_id = :companyId
        AND date_trunc(:timeInterval, doc_date) < date_trunc(:timeInterval, CAST(:dateFrom AS date))
        AND type = :type
    GROUP BY base_debt_currency
    UNION
    SELECT CAST(date_trunc(:timeInterval, doc_date) AS date) AS dt, base_debt_currency,
		SUM(base_debt_amount) base_amount,
		SUM(CASE WHEN base_debt_amount > 0 THEN base_debt_amount ELSE 0 END) AS base_debit_amount,
		SUM(CASE WHEN base_debt_amount < 0 THEN base_debt_amount ELSE 0 END) AS base_credit_amount
    FROM debt_history
    WHERE company_id = :companyId
        AND date_trunc(:timeInterval, doc_date) >= date_trunc(:timeInterval, CAST(:dateFrom AS date))
        AND date_trunc(:timeInterval, doc_date) <= date_trunc(:timeInterval, CAST(:dateTo AS date))
        AND type = :type
    GROUP BY base_debt_currency, dt
),
E AS (
SELECT D.dt, D.base_currency,
    D.base_amount, D.base_debit_amount, D.base_credit_amount,
    SUM(D.base_amount)
    	OVER (ORDER BY D.dt ASC ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS base_balance_amount
FROM D
)
SELECT dt, base_currency, base_amount, base_debit_amount, base_credit_amount, base_balance_amount
FROM E
WHERE dt >= date_trunc(:timeInterval, CAST(:dateFrom AS date))
ORDER BY dt
