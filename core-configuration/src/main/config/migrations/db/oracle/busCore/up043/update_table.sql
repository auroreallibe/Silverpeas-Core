UPDATE ST_Group
SET
    state = 'VALID',
    stateSaveDate = CURRENT_TIMESTAMP;

ALTER TABLE ST_Group MODIFY state NOT NULL;
ALTER TABLE ST_Group MODIFY stateSaveDate  NOT NULL;