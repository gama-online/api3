SELECT
  pid,
  now() - pg_stat_activity.query_start AS duration,
  query,
  state
FROM pg_stat_activity
WHERE (now() - pg_stat_activity.query_start) > interval '5 minutes';

-- SELECT pg_cancel_backend(3502339);
-- SELECT pg_terminate_backend(__pid__);
