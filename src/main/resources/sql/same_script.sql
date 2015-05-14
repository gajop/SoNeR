BEGIN;
drop table if exists peopleuri_same;
create table peopleuri_same (firstPersonId INTEGER, secondPersonId INTEGER);
INSERT INTO peopleuri_same
SELECT DISTINCT
  first_person_uri.personid, 
  second_person_uri.personid
FROM 
  peopleuri_modified first_person_uri, 
  peopleuri_modified second_person_uri
WHERE 
  first_person_uri.localurl = second_person_uri.localurl AND
  first_person_uri.personid != second_person_uri.personid AND
  first_person_uri.validUrl AND 
  second_person_uri.validUrl;
END;