SELECT DISTINCT doc.date, doc.ordinal, doc.number, doc.id
FROM gl_operations op
JOIN double_entries doc ON doc.id = op.parent_id
WHERE doc.company_id = :companyId AND doc.date BETWEEN :dateFrom AND :dateTo
 AND doc.archive IS NOT true AND doc.finished_gl
 AND (op.credit_number = :accountNumber AND jsonb_path_query_array(credit_rc, '$[*].id') @> CAST(CAST(:rcId AS text) AS jsonb)
     OR op.debit_number = :accountNumber AND jsonb_path_query_array(debit_rc, '$[*].id') @> CAST(CAST(:rcId AS text) AS jsonb))
ORDER BY doc.date, doc.ordinal, doc.number, doc.id
