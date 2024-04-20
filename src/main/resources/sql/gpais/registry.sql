WITH D(ob_date) AS (
     SELECT COALESCE((
            SELECT doc.date
            FROM inventory_ob ob
            JOIN documents doc ON doc.id = ob.id
            WHERE doc.company_id = :companyId AND doc.archive IS NOT true AND ob.finished_parts
            AND doc.date >= :startAccounting AND doc.date < :dateFrom
            ORDER BY doc.date DESC
            LIMIT 1), :startAccounting)
), R(part_id, name, sku,
		packaging,
        ob_quantity,
        purchase_quantity, import_quantity,
        wholesale_quantity, retail_quantity, export_quantity,
        production_quantity,
        inventory_quantity) AS (
    SELECT part_id, P.name, sku,
    	packaging,
        SUM(CASE WHEN doc_date < :dateFrom THEN quantity END) ob_quantity,
		SUM(CASE WHEN doc_date >= :dateFrom AND inventory_type = 'B' AND COALESCE(C.registration_address_country, C.business_address_country, C.locations->0->>'country', 'LT') = 'LT' THEN quantity END) purchase_quantity,
		SUM(CASE WHEN doc_date >= :dateFrom AND inventory_type = 'B' AND COALESCE(C.registration_address_country, C.business_address_country, C.locations->0->>'country', 'LT') <> 'LT' THEN quantity END) import_quantity,
  		SUM(CASE WHEN doc_date >= :dateFrom AND inventory_type = 'S' AND COALESCE(C.registration_address_country, C.business_address_country, C.locations->0->>'country', 'LT') = 'LT' AND C.taxpayer_type = 'L' THEN quantity END) wholesale_quantity,
  		SUM(CASE WHEN doc_date >= :dateFrom AND inventory_type = 'S' AND COALESCE(C.registration_address_country, C.business_address_country, C.locations->0->>'country', 'LT') = 'LT' AND NOT (C.taxpayer_type = 'L') THEN quantity END) retail_quantity,
  		SUM(CASE WHEN doc_date >= :dateFrom AND inventory_type = 'S' AND COALESCE(C.registration_address_country, C.business_address_country, C.locations->0->>'country', 'LT') <> 'LT' THEN quantity END) export_quantity,
		SUM(CASE WHEN doc_date >= :dateFrom AND inventory_type = 'P' THEN quantity END) production_quantity,
		SUM(CASE WHEN doc_date >= :dateFrom AND inventory_type = 'I' THEN quantity END) inventory_quantity
	FROM inventory_history H
	JOIN parts P ON P.id = part_id
	LEFT JOIN counterparties C ON C.id = H.counterparty_id
	LEFT JOIN documents doc ON doc.id = H.origin_doc_id
 	LEFT JOIN counterparties G ON G.id = doc.counterparty_id
	CROSS JOIN D
	WHERE H.company_id = :companyId
		AND ((doc_date = D.ob_date AND inventory_type = 'O') OR
		     (doc_date > D.ob_date AND doc_date < :dateTo AND (inventory_type <> 'O')))
		AND P.type <> 'N'
		AND P.packaging IS NOT NULL
		AND COALESCE(G.registration_address_country, G.business_address_country, G.locations->0->>'country', 'LT') <> 'LT'
	GROUP BY part_id, P.name, sku, packaging
)
SELECT part_id, name, sku,
	CAST(packaging AS text) AS packaging,
    ob_quantity,
    purchase_quantity, import_quantity,
    wholesale_quantity, retail_quantity, export_quantity,
    production_quantity,
    inventory_quantity,
    COALESCE(ob_quantity, 0) + COALESCE(purchase_quantity, 0) + COALESCE(import_quantity, 0) +
    	COALESCE(wholesale_quantity, 0) + COALESCE(retail_quantity, 0) + COALESCE(export_quantity, 0) +
    	COALESCE(production_quantity, 0) + COALESCE(inventory_quantity, 0) AS remainder_quantity,
    COUNT(*) OVER() AS total
FROM R
WHERE (ob_quantity <> 0 OR purchase_quantity <> 0 OR import_quantity <> 0
	OR wholesale_quantity <> 0 OR retail_quantity <> 0 OR export_quantity <> 0
    OR production_quantity <> 0 OR inventory_quantity <> 0)
ORDER BY lower(unaccent(name)), lower(unaccent(sku)), part_id
