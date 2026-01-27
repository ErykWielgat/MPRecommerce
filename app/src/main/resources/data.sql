INSERT IGNORE INTO categories (id, name, description) VALUES (1, 'Elektronika', 'Sprzęt komputerowy i RTV');
INSERT IGNORE INTO categories (id, name, description) VALUES (2, 'Książki', 'Literatura i podręczniki');
INSERT IGNORE INTO categories (id, name, description) VALUES (3, 'Dom i Ogród', 'Rzeczy do domu');

INSERT IGNORE INTO products (id, name, description, price, stock, image_url, category_id)
VALUES (201, 'Laptop Gamingowy', 'Szybki laptop do gier', 4500.00, 5, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRSq5ZB8hleQFi1_f15OEeCLEr-Uw2seScPTQ&s', 1);

INSERT IGNORE INTO products (id, name, description, price, stock, image_url, category_id)
VALUES (202, 'Smartfon X', 'Telefon z dobrym aparatem', 2999.99, 10, 'https://m.media-amazon.com/images/I/61pTiSkbvzL.jpg', 1);

INSERT IGNORE INTO products (id, name, description, price, stock, image_url, category_id)
VALUES (203, 'Władca Pierścieni', 'Klasyka fantasy', 49.90, 20, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSDChhRTce3mTCC-tDwwgnYxHjfndByaIB0WA&s', 2);

INSERT IGNORE INTO products (id, name, description, price, stock, image_url, category_id)
VALUES (204, 'Kosiarka spalinowa', 'Do dużej trawy', 1200.00, 3, 'https://grupa-narzedziowa.pl/18486-large_default/husqvarna-lc-353v-kompozytowa-kosiarka-spalinowa-z-regulowanym-napedem-i-koszem-hs-166a.jpg', 3);