drop table if exists peoplesynonym_modified;
create table peoplesynonym_modified ( personid integer, synonymid integer );
INSERT INTO peoplesynonym_modified
SELECT DISTINCT
  first_person_uri.personid,
  second_person_uri.personid
FROM 
  peoplesynonym,
  peopleuri_modified first_person_uri, 
  peopleuri_modified second_person_uri  

WHERE 
  first_person_uri.localurl = peoplesynonym.localurl AND
   (
    	(first_person_uri.validURL AND first_person_uri.context = peoplesynonym.context) OR 
        (NOT first_person_uri.validURL)
    ) AND
  second_person_uri.localurl = peoplesynonym.synonym;
