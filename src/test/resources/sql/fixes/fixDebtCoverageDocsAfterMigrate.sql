UPDATE debt_coverage Debt
SET doc_id = Doc.id, doc_db = 'P'
FROM documents Doc
WHERE (Debt.doc_db IS NULL OR Debt.doc_db = '')
	AND Doc.foreign_id = Debt.doc_id AND Doc.company_id = Debt.company_id;



WITH DID AS (
	SELECT DISTINCT Debt.id
	FROM debt_coverage Debt
	CROSS JOIN jsonb_array_elements(docs) AS x
	JOIN documents Doc ON Doc.foreign_id = CAST(x->'doc'->>'id' AS BIGINT)
		AND Doc.company_id = Debt.company_id
		AND x->'doc'->>'db' = ''
), D AS(
	SELECT Debt.id,
		jsonb_agg(
			CASE WHEN x->'doc'->>'db' = '' AND Doc.id IS NOT NULL
				THEN x || jsonb_build_object('doc', jsonb_build_object('db', 'P', 'id', Doc.id))
				ELSE x
			END) AS docs_updated
	FROM debt_coverage Debt
	CROSS JOIN jsonb_array_elements(docs) AS x
	JOIN DID ON DID.id = Debt.id
	LEFT JOIN documents Doc ON x->'doc'->>'db' = '' AND Doc.foreign_id = CAST(x->'doc'->>'id' AS BIGINT) AND Doc.company_id = Debt.company_id
	GROUP BY Debt.id
)
UPDATE debt_coverage Debt SET docs=D.docs_updated
FROM D
WHERE Debt.id = D.id
