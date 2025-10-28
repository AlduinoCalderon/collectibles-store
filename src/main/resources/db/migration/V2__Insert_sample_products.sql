INSERT INTO products (id, name, description, price, currency, category, is_active, is_deleted) VALUES
('item1', 'Gorra autografiada por Peso Pluma', 'Una gorra autografiada por el famoso Peso Pluma.', 621.34, 'USD', 'Autographed Items', true, false),
('item2', 'Casco autografiado por Rosalía', 'Un casco autografiado por la famosa cantante Rosalía, una verdadera MOTOMAMI!', 734.57, 'USD', 'Autographed Items', true, false),
('item3', 'Chamarra de Bad Bunny', 'Una chamarra de la marca favorita de Bad Bunny, autografiada por el propio artista.', 521.89, 'USD', 'Clothing', true, false),
('item4', 'Guitarra de Fernando Delgadillo', 'Una guitarra acústica de alta calidad utilizada por el famoso cantautor Fernando Delgadillo.', 823.12, 'USD', 'Musical Instruments', true, false),
('item5', 'Jersey firmado por Snoop Dogg', 'Un jersey autografiado por el legendario rapero Snoop Dogg.', 355.67, 'USD', 'Clothing', true, false),
('item6', 'Prenda de Cardi B autografiada', 'Un crop-top usado y autografiado por la famosa rapera Cardi B. en su última visita a México', 674.23, 'USD', 'Clothing', true, false),
('item7', 'Guitarra autografiada por Coldplay', 'Una guitarra eléctrica autografiada por la popular banda británica Coldplay, un día antes de su concierto en Monterrey en 2022.', 458.91, 'USD', 'Musical Instruments', true, false)
ON CONFLICT (id) DO NOTHING;
