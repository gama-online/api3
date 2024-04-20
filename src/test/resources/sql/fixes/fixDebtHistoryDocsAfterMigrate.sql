UPDATE debt_history Debt
SET doc_id = Doc.id, doc_db = 'P'
FROM documents Doc
WHERE (Debt.doc_db IS NULL OR Debt.doc_db = '')
	AND Doc.foreign_id = Debt.doc_id AND Doc.company_id = Debt.company_id
