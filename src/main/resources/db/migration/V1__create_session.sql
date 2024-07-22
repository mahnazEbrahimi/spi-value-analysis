CREATE TABLE t_session
(
    c_id                    UUID PRIMARY KEY,
    c_tenant_id             UUID                     NOT NULL,
    c_created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    c_user_id               UUID
);

ALTER TABLE t_session owner TO signavio_admin;