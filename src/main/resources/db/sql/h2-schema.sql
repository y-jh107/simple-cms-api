-- members 테이블 생성 (User 엔티티)
CREATE TABLE members (
    id                 BIGINT AUTO_INCREMENT,
    username           VARCHAR(50)  NOT NULL,
    password           VARCHAR(255) NOT NULL,
    created_date       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP,
    CONSTRAINT pk_members PRIMARY KEY (id),
    CONSTRAINT uk_members_username UNIQUE (username)
);

-- user_roles 테이블 생성 (@ElementCollection 반영)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role    VARCHAR(255),
    CONSTRAINT fk_user_roles_on_user FOREIGN KEY (user_id) REFERENCES members (id)
);

-- contents 테이블 생성 (Content 엔티티)
DROP TABLE IF EXISTS contents CASCADE;

CREATE TABLE contents (
    id                 BIGINT AUTO_INCREMENT,
    title              VARCHAR(100) NOT NULL,
    description        TEXT,
    view_count         BIGINT       DEFAULT 0 NOT NULL,
    created_date       TIMESTAMP    NOT NULL,
    last_modified_date TIMESTAMP,
    created_by         VARCHAR(50)  NOT NULL,
    last_modified_by   VARCHAR(50),
    CONSTRAINT pk_contents PRIMARY KEY (id)
);