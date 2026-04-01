-- Soft FK orphan checks (auth/event)

-- auth_identities orphan
SELECT ai.id
FROM auth_identities ai
LEFT JOIN members m ON m.id = ai.member_id
WHERE ai.deleted_at IS NULL
  AND (m.id IS NULL OR m.deleted_at IS NOT NULL);

-- email_verifications orphan
SELECT ev.id
FROM email_verifications ev
LEFT JOIN members m ON m.id = ev.member_id
WHERE ev.deleted_at IS NULL
  AND (m.id IS NULL OR m.deleted_at IS NOT NULL);

-- refresh_tokens orphan
SELECT rt.id
FROM refresh_tokens rt
LEFT JOIN members m ON m.id = rt.member_id
WHERE rt.deleted_at IS NULL
  AND (m.id IS NULL OR m.deleted_at IS NOT NULL);

-- events orphan
SELECT e.id
FROM events e
LEFT JOIN venues v ON v.id = e.venue_id
WHERE e.deleted_at IS NULL
  AND (v.id IS NULL OR v.deleted_at IS NOT NULL);

-- event_sessions orphan
SELECT es.id
FROM event_sessions es
LEFT JOIN events e ON e.id = es.event_id
WHERE es.deleted_at IS NULL
  AND (e.id IS NULL OR e.deleted_at IS NOT NULL);
