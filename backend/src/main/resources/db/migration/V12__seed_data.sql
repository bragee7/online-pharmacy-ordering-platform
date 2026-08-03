-- Development seed data: categories, medicines, inventory.
-- User accounts are seeded by the Java DataSeeder (needs BCrypt).

INSERT INTO categories (name, description, active, created_at, updated_at) VALUES
('Analgesics & Pain Relief', 'Pain killers and anti-inflammatory medicines', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Antibiotics', 'Prescription antibiotics for bacterial infections', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Cold, Flu & Cough', 'Relief for cold, fever and cough symptoms', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Digestive Health', 'Antacids, acidity and gut health products', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Vitamins & Supplements', 'Daily vitamins and nutritional supplements', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Diabetes Care', 'Medicines for diabetes management', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Cardiovascular', 'Heart and blood pressure medicines', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Allergy & Skin Care', 'Antihistamines and topical creams', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));

INSERT INTO medicines (name, generic_name, brand_name, description, category_id, manufacturer, price, prescription_required, dosage_information, expiry_date, active, created_at, updated_at) VALUES
('Paracetamol 500mg', 'Acetaminophen', 'Dolo 650', 'For fever and mild to moderate pain', (SELECT id FROM categories WHERE name = 'Analgesics & Pain Relief'), 'Micro Labs', 30.50, FALSE, '1 tablet every 4-6 hours as needed', '2027-06-30', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Ibuprofen 400mg', 'Ibuprofen', 'Brufen', 'Anti-inflammatory pain relief', (SELECT id FROM categories WHERE name = 'Analgesics & Pain Relief'), 'Abbott', 45.00, FALSE, '1 tablet 3 times daily after food', '2027-08-31', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Diclofenac Gel 30g', 'Diclofenac diethylamine', 'Voveran Gel', 'Topical gel for joint and muscle pain', (SELECT id FROM categories WHERE name = 'Analgesics & Pain Relief'), 'Novartis', 120.00, FALSE, 'Apply thin layer 3-4 times daily', '2027-03-31', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Tramadol 50mg', 'Tramadol', 'Trama', 'Moderate to severe pain (opioid)', (SELECT id FROM categories WHERE name = 'Analgesics & Pain Relief'), 'Sun Pharma', 89.00, TRUE, '1 tablet every 6-8 hours as prescribed', '2027-05-31', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Amoxicillin 500mg', 'Amoxicillin trihydrate', 'Mox 500', 'Broad-spectrum antibiotic', (SELECT id FROM categories WHERE name = 'Antibiotics'), 'Cipla', 105.00, TRUE, '1 capsule every 8 hours', '2027-09-30', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Azithromycin 500mg', 'Azithromycin', 'Azithral', 'Antibiotic for respiratory infections', (SELECT id FROM categories WHERE name = 'Antibiotics'), 'Alembic', 180.00, TRUE, '1 tablet once daily for 3-5 days', '2027-10-31', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Ciprofloxacin 500mg', 'Ciprofloxacin', 'Ciplox', 'Fluoroquinolone antibiotic', (SELECT id FROM categories WHERE name = 'Antibiotics'), 'Cipla', 130.00, TRUE, '1 tablet every 12 hours', '2027-04-30', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Cefixime 200mg', 'Cefixime', 'Taxim-O', 'Third generation cephalosporin', (SELECT id FROM categories WHERE name = 'Antibiotics'), 'Alkem', 210.00, TRUE, '1 tablet every 12 hours', '2027-07-31', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Cetirizine 10mg', 'Cetirizine hydrochloride', 'Zyrtec', 'Antihistamine for allergy symptoms', (SELECT id FROM categories WHERE name = 'Allergy & Skin Care'), 'UCB', 25.00, FALSE, '1 tablet once daily', '2027-11-30', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Cold & Flu Capsule', 'Paracetamol + Phenylephrine + Cetirizine', 'D-Cold Total', 'Multi-symptom cold and flu relief', (SELECT id FROM categories WHERE name = 'Cold, Flu & Cough'), 'Mankind', 60.00, FALSE, '1 capsule every 6-8 hours', '2027-02-28', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Dextromethorphan Syrup 100ml', 'Dextromethorphan hydrobromide', 'Benadryl Dry', 'Dry cough relief syrup', (SELECT id FROM categories WHERE name = 'Cold, Flu & Cough'), 'Fulford', 95.00, FALSE, '10 ml 3 times daily', '2027-01-31', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Omeprazole 20mg', 'Omeprazole', 'Omez', 'Reduces stomach acid production', (SELECT id FROM categories WHERE name = 'Digestive Health'), 'Dr Reddy''s', 75.00, FALSE, '1 capsule before breakfast', '2027-12-31', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Ranitidine 150mg', 'Ranitidine', 'Rantac', 'Acidity and ulcer relief', (SELECT id FROM categories WHERE name = 'Digestive Health'), 'JB Chemicals', 40.00, FALSE, '1 tablet twice daily', '2027-03-31', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('ORS Powder Sachet', 'Oral rehydration salts', 'Electral', 'Replaces fluids lost in diarrhoea', (SELECT id FROM categories WHERE name = 'Digestive Health'), 'FDC', 15.00, FALSE, '1 sachet per litre of water', '2027-05-31', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Vitamin C 500mg', 'Ascorbic acid', 'Limcee', 'Antioxidant and immunity support', (SELECT id FROM categories WHERE name = 'Vitamins & Supplements'), 'Abbott', 55.00, FALSE, '1 tablet once daily', '2027-06-30', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Multivitamin + Multimineral', 'A, B-complex, C, D, E and minerals', 'Supradyn', 'Daily multivitamin supplement', (SELECT id FROM categories WHERE name = 'Vitamins & Supplements'), 'Bayer', 220.00, FALSE, '1 tablet once daily after food', '2027-09-30', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Calcium + Vitamin D3', 'Calcium carbonate + Cholecalciferol', 'Shelcal', 'Bone health supplement', (SELECT id FROM categories WHERE name = 'Vitamins & Supplements'), 'Torrent', 160.00, FALSE, '1 tablet once daily', '2027-08-31', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Metformin 500mg', 'Metformin hydrochloride', 'Glycomet', 'First-line oral anti-diabetic', (SELECT id FROM categories WHERE name = 'Diabetes Care'), 'USV', 65.00, TRUE, '1 tablet with food twice daily', '2027-10-31', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Glimepiride 1mg', 'Glimepiride', 'Amaryl', 'Sulfonylurea anti-diabetic', (SELECT id FROM categories WHERE name = 'Diabetes Care'), 'Sanofi', 95.00, TRUE, '1 tablet once daily', '2027-04-30', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Insulin Glargine 100IU/ml', 'Insulin glargine', 'Lantus', 'Long-acting basal insulin', (SELECT id FROM categories WHERE name = 'Diabetes Care'), 'Sanofi', 650.00, TRUE, 'Inject as directed by physician', '2027-01-31', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Amlodipine 5mg', 'Amlodipine besylate', 'Amlong', 'Calcium channel blocker for hypertension', (SELECT id FROM categories WHERE name = 'Cardiovascular'), 'Mankind', 48.00, TRUE, '1 tablet once daily', '2027-07-31', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Atorvastatin 10mg', 'Atorvastatin calcium', 'Lipicure', 'Statin for cholesterol control', (SELECT id FROM categories WHERE name = 'Cardiovascular'), 'Cipla', 130.00, TRUE, '1 tablet once daily in the evening', '2027-11-30', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Aspirin 75mg', 'Aspirin (low dose)', 'Ecosprin', 'Blood thinner for cardiac protection', (SELECT id FROM categories WHERE name = 'Cardiovascular'), 'USV', 12.00, FALSE, '1 tablet once daily', '2027-03-31', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
('Clotrimazole Cream 20g', 'Clotrimazole', 'Clopreg', 'Anti-fungal topical cream', (SELECT id FROM categories WHERE name = 'Allergy & Skin Care'), 'Glenmark', 85.00, FALSE, 'Apply twice daily on affected area', '2027-05-31', TRUE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));

-- Inventory seed. Note: Vitamin C and Aspirin are intentionally below reorder
-- level so the low-stock feature is demonstrable out of the box.
INSERT INTO inventory (medicine_id, available_quantity, reserved_quantity, reorder_level, last_updated) VALUES
((SELECT id FROM medicines WHERE name = 'Paracetamol 500mg'), 500, 0, 50, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Ibuprofen 400mg'), 300, 0, 40, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Diclofenac Gel 30g'), 120, 0, 25, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Tramadol 50mg'), 60, 0, 20, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Amoxicillin 500mg'), 100, 0, 20, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Azithromycin 500mg'), 100, 0, 20, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Ciprofloxacin 500mg'), 80, 0, 15, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Cefixime 200mg'), 50, 0, 15, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Cetirizine 10mg'), 400, 0, 60, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Cold & Flu Capsule'), 250, 0, 40, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Dextromethorphan Syrup 100ml'), 200, 0, 30, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Omeprazole 20mg'), 300, 0, 40, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Ranitidine 150mg'), 150, 0, 25, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'ORS Powder Sachet'), 600, 0, 80, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Vitamin C 500mg'), 5, 0, 30, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Multivitamin + Multimineral'), 180, 0, 30, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Calcium + Vitamin D3'), 140, 0, 25, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Metformin 500mg'), 220, 0, 35, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Glimepiride 1mg'), 90, 0, 20, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Insulin Glargine 100IU/ml'), 40, 0, 10, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Amlodipine 5mg'), 160, 0, 30, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Atorvastatin 10mg'), 110, 0, 20, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Aspirin 75mg'), 7, 0, 25, UTC_TIMESTAMP(6)),
((SELECT id FROM medicines WHERE name = 'Clotrimazole Cream 20g'), 90, 0, 15, UTC_TIMESTAMP(6));