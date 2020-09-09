CREATE TABLE processes (
    id SERIAL PRIMARY KEY NOT NULL,
    name TEXT UNIQUE,
    number_of_evaluations INT NOT NULL,
    number_of_seeds INT NOT NULL,
    status TEXT NOT NULL,
    rabbit_id TEXT NOT NULL,
    results TEXT NOT NULL,
    algorithm_sha256 TEXT NOT NULL,
    problem_sha256 TEXT NOT NULL,
    reference_set_sha256 TEXT NOT NULL,
    user_id INT NOT NULL
);

