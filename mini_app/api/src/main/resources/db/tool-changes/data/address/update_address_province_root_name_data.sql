UPDATE address_province
SET root_name = regexp_replace(upper(name), '^(TỈNH |THÀNH PHỐ )', '');