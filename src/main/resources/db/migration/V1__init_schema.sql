CREATE TABLE obec (
    kod INT PRIMARY KEY,
    nazev VARCHAR(255) NOT NULL
);

CREATE TABLE cast_obce (
    kod INT PRIMARY KEY,
    nazev VARCHAR(255) NOT NULL,
    obec_kod INT NOT NULL,
    CONSTRAINT fk_cast_obce_obec FOREIGN KEY (obec_kod) REFERENCES obec (kod) ON DELETE CASCADE
);

CREATE INDEX idx_cast_obce_obec_kod ON cast_obce(obec_kod);