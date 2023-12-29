UPDATE address_district
SET root_name = regexp_replace(upper(name), '^(HUYỆN |QUẬN |THỊ XÃ |THÀNH PHỐ )', '');