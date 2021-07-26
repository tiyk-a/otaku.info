ALTER TABLE item ADD COLUMN fct_chk tinyint(1) not null;
ALTER TABLE del_item ADD COLUMN fct_chk tinyint(1) not null;
ALTER TABLE program ADD COLUMN fct_chk tinyint(1) not null;

UPDATE item SET fct_chk = 1;
UPDATE del_item SET fct_chk = 1;
UPDATE program SET fct_chk = 1;
