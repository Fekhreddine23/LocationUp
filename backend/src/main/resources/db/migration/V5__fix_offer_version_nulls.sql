DO $$
BEGIN
    IF to_regclass('public.offers') IS NOT NULL THEN
        ALTER TABLE offers
        ADD COLUMN IF NOT EXISTS version BIGINT;

        ALTER TABLE offers
        ALTER COLUMN version SET DEFAULT 0;

        UPDATE offers
        SET version = 0
        WHERE version IS NULL;

        ALTER TABLE offers
        ALTER COLUMN version SET NOT NULL;
    END IF;
END
$$;
