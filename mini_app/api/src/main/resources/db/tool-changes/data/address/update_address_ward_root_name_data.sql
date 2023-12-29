UPDATE address_ward
SET root_name = regexp_replace(upper(name), '^(XÃ |PHƯỜNG |THỊ TRẤN )', '');