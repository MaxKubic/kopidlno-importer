# Kopidlno RÚIAN XML Importer

Aplikace ve **Spring Boot 3 (Java 21)** určená pro stažení, streamingové zpracování a uložení dat z RÚIAN XML exportu pro obec Kopidlno do databáze **PostgreSQL**.

---

## 🎯 Cíl projektu

Aplikace řeší:
1. Stažení komprimovaného archivu `.zip` z URL (`https://www.smartform.cz/download/kopidlno.xml.zip`).
2. Streamované čtení XML souboru bez ukládání na disk a bez načítání celého DOM stromu do paměti.
3. Extrakci údajů o obci (`vf:Obec`) a jejích částech (`vf:CastObce`).
4. Uložení záznamů s relační vazbou do tabulek `obec` a `cast_obce`.

---

## 🛠 Použité technologie & Architektura

- **Java 21** & **Spring Boot 3**
- **StAX (Streaming API for XML):** Zvoleno pro nízkou paměťovou náročnost ($O(1)$ paměťová složitost vůči velikosti XML). Zpracovává data přímo ze síťového proudu `ZipInputStream`.
- **Spring Data JPA & Hibernate:** ORM mapování pro tabulky `obec` a `cast_obce`.
- **PostgreSQL 16:** Relační SQL databáze.
- **Flyway:** Automatizovaná verzovaná migrace databázového schématu (`V1__init_schema.sql`).
- **Docker & Docker Compose:** Multi-stage build a orchestrace celého prostředí jedním příkazem.

---

## 🗄 Databázové schéma

```sql
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
```

---

## 🚀 Spuštění aplikace (Docker Compose)

Celé prostředí (databáze + automatická migrace schématu + spuštění a import) se sestaví a spustí jedním příkazem:

```bash
docker compose up --build
```

Aplikace běží jako `CommandLineRunner`. Po dokončení importu dat do PostgreSQL se kontejner korektně ukončí (`exit code 0`). Databázový kontejner zůstává dostupný pro kontrolu.

---

## 🔍 Ověření importovaných dat

Po proběhnutí importu lze data zkontrolovat přímo v kontejneru:

### 1. Spuštění databáze na pozadí (pokud neběží):
```bash
docker compose up -d db
```

### 2. Kontrola obce Kopidlno:
```bash
docker exec -it ruian-postgres psql -U postgres -d ruian_db -c "SELECT * FROM obec;"
```
*Výsledek:*
```text
  kod   |  nazev   
--------+----------
 573060 | Kopidlno
(1 row)
```

### 3. Kontrola částí obce:
```bash
docker exec -it ruian-postgres psql -U postgres -d ruian_db -c "SELECT * FROM cast_obce;"
```
*Výsledek:*
```text
  kod  |  nazev   | obec_kod 
-------+----------+----------
 69299 | Kopidlno |   573060
 69302 | Ledkov   |   573060
 97373 | Mlýnec   |   573060
 31801 | Drahoraz |   573060
 31828 | Pševes   |   573060
(5 rows)
```

### 4. Zastavení kontejnerů:
```bash
docker compose down
```