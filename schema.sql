-- Create users table
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(30) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    failed_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    last_login TIMESTAMP
);

-- Create sessions table
CREATE TABLE sessions (
    session_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(user_id),
    token VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true
);

-- Create emails table
CREATE TABLE emails (
    email_id SERIAL PRIMARY KEY,
    from_user VARCHAR(30) REFERENCES users(username),
    to_user VARCHAR(30) REFERENCES users(username),
    subject VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    sent_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('SENT', 'DRAFT', 'DELETED')),
    is_read BOOLEAN DEFAULT false
);

-- Create indexes
CREATE INDEX idx_username_lower ON users (LOWER(username));
CREATE INDEX idx_sessions_token ON sessions(token);
CREATE INDEX idx_emails_from_user ON emails(from_user);
CREATE INDEX idx_emails_to_user ON emails(to_user);
CREATE INDEX idx_emails_sent_date ON emails(sent_date);