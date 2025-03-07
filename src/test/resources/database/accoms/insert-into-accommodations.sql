INSERT INTO addresses (id, address, is_deleted)
VALUES
    (2,
     'Downtown',
     false),
    (3,
     'Suburbs',
     false);

INSERT INTO accommodations (id, type, location_id, size, daily_rate, availability, is_deleted)
VALUES
    (2,
     'APARTMENT',
     2,
     '1000 sqft',
     120,
     5,
     false),
    (3,
     'HOUSE',
     3,
     '2000 sqft',
     200,
     3,
     false);
