-- Let the DB generate IDs (avoids clashes on restarts)
INSERT INTO customer (name, email, password_hash) VALUES
 ('Alice','alice@example.com','hash1'),
 ('Bob','bob@example.com','hash2');
