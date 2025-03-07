INSERT INTO payments (id, booking_id, session_id, session_url, expired_time, amount, status)
VALUES
    (2,
     2,
     'session-12345',
     'https://payment.example.com/session_1',
     1700000000, 120.00,
     'PENDING'),
    (3,
     3,
     'session-67890',
     'https://payment.example.com/session_2',
     1700005000,
     200.00,
     'PAID');
