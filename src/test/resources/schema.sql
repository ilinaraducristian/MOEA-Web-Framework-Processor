CREATE TABLE queue_items
(
    id                    INT IDENTITY,
    name                  VARCHAR(255),
    number_of_evaluations INT          NOT NULL,
    number_of_seeds       INT          NOT NULL,
    status                VARCHAR(255),
    rabbit_id             VARCHAR(255),
    results               VARCHAR(255),
    algorithm_md5         VARCHAR(255) NOT NULL,
    problem_md5           VARCHAR(255) NOT NULL,
    reference_set_md5     VARCHAR(255) NOT NULL,
    user_entity_id        VARCHAR(36)  NOT NULL
);