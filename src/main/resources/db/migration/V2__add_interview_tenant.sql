ALTER TABLE interviews ADD COLUMN IF NOT EXISTS company_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_interview_company ON interviews(company_id);
