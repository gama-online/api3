WITH D(ob_date) AS (
     SELECT COALESCE((
            SELECT doc.date
            FROM inventory_ob ob
            JOIN documents doc ON doc.id = ob.id
            WHERE doc.company_id = :companyId AND doc.archive IS NOT true AND ob.finished_parts
            AND doc.date >= :startAccounting AND doc.date < :dateFrom
            ORDER BY doc.date DESC
            LIMIT 1), :startAccounting)
), R(part_id, name, sku, forward_sell, part_type, foreign_id,
		packaging,
        ob_quantity, ob_cost, purchase_quantity, purchase_cost, invoice_quantity, invoice_cost,
        transport_quantity, transport_cost, production_quantity, production_cost, inventory_quantity, inventory_cost) AS (
    SELECT part_id, name, sku, forward_sell, P.type AS part_type, P.foreign_id,
    	packaging,
        SUM(CASE WHEN doc_date < :dateFrom AND P.type <> 'N' THEN quantity END) ob_quantity,
		SUM(CASE WHEN doc_date < :dateFrom AND P.type <> 'N' THEN H.cost_total_amount END) ob_cost,
		SUM(CASE WHEN doc_date >= :dateFrom AND inventory_type = 'B' AND ('' IN (:types) OR P.type IN (:types)) THEN quantity END) purchase_quantity,
		SUM(CASE WHEN doc_date >= :dateFrom AND inventory_type = 'B' AND ('' IN (:types) OR P.type IN (:types)) THEN H.cost_total_amount END) purchase_cost,
  		SUM(CASE WHEN doc_date >= :dateFrom AND inventory_type = 'S' AND ('' IN (:types) OR P.type IN (:types)) THEN quantity END) invoice_quantity,
		SUM(CASE WHEN doc_date >= :dateFrom AND inventory_type = 'S' AND ('' IN (:types) OR P.type IN (:types)) THEN H.cost_total_amount END) invoice_cost,
		SUM(CASE WHEN doc_date >= :dateFrom AND inventory_type = 'T' AND ('' IN (:types) OR P.type IN (:types)) THEN quantity END) transport_quantity,
        SUM(CASE WHEN doc_date >= :dateFrom AND inventory_type = 'T' AND ('' IN (:types) OR P.type IN (:types)) THEN H.cost_total_amount END) transport_cost,
		SUM(CASE WHEN doc_date >= :dateFrom AND inventory_type = 'P' AND ('' IN (:types) OR P.type IN (:types)) THEN quantity END) production_quantity,
        SUM(CASE WHEN doc_date >= :dateFrom AND inventory_type = 'P' AND ('' IN (:types) OR P.type IN (:types)) THEN H.cost_total_amount END) production_cost,
		SUM(CASE WHEN doc_date >= :dateFrom AND inventory_type = 'I' AND ('' IN (:types) OR P.type IN (:types)) THEN quantity END) inventory_quantity,
  		SUM(CASE WHEN doc_date >= :dateFrom AND inventory_type = 'I' AND ('' IN (:types) OR P.type IN (:types)) THEN H.cost_total_amount END) inventory_cost
	FROM inventory_history H
	LEFT JOIN parts P ON P.id = part_id
 	CROSS JOIN D
	WHERE H.company_id = :companyId
		AND ((doc_date = D.ob_date AND inventory_type = 'O') OR
		     (doc_date > D.ob_date AND doc_date < :dateTo AND (inventory_type <> 'O')))
		AND (0 = :partId OR part_id = :partId)
		AND (0 = :warehouseId OR warehouse_id = :warehouseId)
	GROUP BY part_id, name, sku, forward_sell, P.type, P.foreign_id, packaging
)
SELECT part_id, name, sku, forward_sell, part_type, foreign_id,
	CAST(packaging AS text) AS packaging,
    ob_quantity, ob_cost,
    purchase_quantity, purchase_cost,
    invoice_quantity, invoice_cost,
    transport_quantity, transport_cost,
    production_quantity, production_cost,
    inventory_quantity, inventory_cost,
    COALESCE(ob_quantity, 0) + COALESCE(purchase_quantity, 0) + COALESCE(invoice_quantity, 0)  +
    	COALESCE(transport_quantity, 0) + COALESCE(production_quantity, 0) + COALESCE(inventory_quantity, 0) AS total_quantity,
    COALESCE(ob_cost, 0) + COALESCE(purchase_cost, 0) + COALESCE(invoice_cost, 0) +
    	COALESCE(transport_cost, 0) + COALESCE(production_cost, 0) + COALESCE(inventory_cost, 0) AS total_cost,
    COUNT(*) OVER() AS total
FROM R
WHERE (ob_quantity <> 0 OR purchase_quantity <> 0 OR invoice_quantity <> 0 OR transport_quantity <> 0
    OR production_quantity <> 0 OR inventory_quantity <> 0
    OR ob_cost <> 0 OR purchase_cost <> 0 OR invoice_cost <> 0 OR transport_cost <> 0
    OR production_cost <> 0 OR inventory_cost <> 0)
    AND (
        '' = :filter
        OR trim(unaccent(name)) ILIKE :filter
        OR trim(regexp_replace(unaccent(name), '[^[:alnum:]]+', ' ', 'g')) ILIKE :filter
	    OR trim(sku) ILIKE :filter
    )
ORDER BY lower(unaccent(name)), lower(unaccent(sku)), part_id