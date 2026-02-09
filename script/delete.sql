-- WARNING: This will permanently remove ALL DATA from all user schemas in the database.
-- Make a backup before running (pg_dump)!
-- Usage (psql): psql -U <db_user> -d <database> -f script/delete.sql
DO $$
BEGIN
  -- Safety check: ensure we're connected to the intended database
  IF current_database() <> 'locationvoiture' THEN
    RAISE EXCEPTION 'This script must be run against database "locationvoiture". Current DB: %', current_database();
  END IF;
END$$;

DO $$
DECLARE
  tbls TEXT;
BEGIN
  -- Collect all user tables from schemas excluding system schemas
  SELECT string_agg(format('%I.%I', schemaname, tablename), ', ')
    INTO tbls
  FROM pg_tables
  WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
    AND schemaname NOT LIKE 'pg_%';

  IF tbls IS NULL THEN
    RAISE NOTICE 'No user tables found to truncate.';
  ELSE
    RAISE NOTICE 'Truncating tables: %', tbls;
    -- Truncate all tables, restart identities and cascade to honor FK constraints
    EXECUTE 'TRUNCATE TABLE ' || tbls || ' RESTART IDENTITY CASCADE';
  END IF;
END$$;
