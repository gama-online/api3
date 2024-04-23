DO
   $$
   DECLARE rec RECORD;
   BEGIN
       -- delete all test schemas
        FOR rec IN
        SELECT DISTINCT schemaname FROM (
			SELECT schemaname FROM pg_catalog.pg_tables WHERE schemaname LIKE 'user_%'
			UNION SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'user_%'
			UNION SELECT nspname FROM pg_catalog.pg_namespace WHERE nspname LIKE 'user_%'
		) A
           LOOP
             EXECUTE 'DROP SCHEMA IF EXISTS ' || rec.schemaname || ' CASCADE';
			 COMMIT;
           END LOOP;
		-- delete all test users
		FOR rec IN
		SELECT DISTINCT usename FROM pg_catalog.pg_user WHERE usename LIKE 'user_%'
		LOOP
			 EXECUTE 'DROP USER IF EXISTS ' || rec.usename;
			 COMMIT;
        END LOOP;
   END;
   $$ LANGUAGE plpgsql;
