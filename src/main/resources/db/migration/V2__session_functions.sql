-------- set_session function -------------------
create function set_session_variables_v2(p_tenant_id uuid, p_user_id uuid, p_passphrase text) returns uuid
    security definer
    language plpgsql
as
$$
DECLARE
    -- UUID generator provided by pgcrypto
    session_id UUID := gen_random_uuid();
BEGIN

    -- verify the passphrase
    PERFORM
        1
    FROM key_vault
    WHERE key_pass = crypt(p_passphrase, key_pass);

    -- either there's no key in the table, or the passphrase does not match
    IF NOT FOUND
    THEN
        RAISE EXCEPTION 'matching key not found';
    END IF;

    -- store the context into the sessions table
    INSERT INTO t_session(c_id, c_tenant_id, c_user_id)
    VALUES (session_id, p_tenant_id, p_user_id);

    -- store the ID in a session variable
    PERFORM set_config('signed_vault.current_session_id', CAST(session_id AS TEXT), FALSE);

    -- but also return the value (because of convenience)
    RETURN session_id;

END;
$$;

alter function set_session_variables_v2(uuid, uuid, text) owner to signavio_admin;

---------- get_session function -------------------
create function get_session() returns t_session
    stable
  security definer
  language plpgsql
as
$$
DECLARE
    v_session    t_session;
    v_session_id UUID := current_setting('signed_vault.current_session_id');
BEGIN

    SELECT * INTO v_session
    FROM t_session
    WHERE c_id = v_session_id;

    -- no matching session ID found
    IF NOT FOUND
    THEN
        RAISE EXCEPTION 'invalid session ID';
    END IF;

    -- also check that the value is not expired (24 hours)
    IF now() > v_session.c_created_at + INTERVAL '1 day'
    THEN
        RAISE EXCEPTION 'session expired';
    END IF;

  -- signature seems OK, return the username
    RETURN v_session;

END;
$$;

alter function get_session() owner to signavio_admin;
