drop table if exists peopleknown_modified;
create table peopleknown_modified (personId INTEGER, knownPersonId INTEGER);
INSERT INTO peopleknown_modified 
SELECT DISTINCT
  first_person_uri.personid, 
  second_person_uri.personid
FROM 
  peopleknown,
  peopleuri_modified first_person_uri, 
  peopleuri_modified second_person_uri   
WHERE 
  first_person_uri.localurl = peopleknown.localurl AND
   (
    	  (NOT first_person_uri.validURL AND first_person_uri.context = peopleknown.context) OR 
        (first_person_uri.validURL)
    ) AND
  second_person_uri.localurl = peopleknown.knownpersonurl 
 -- and
 -- (
 -- 	  (second_person_uri.validURL AND second_person_uri.context = peopleknown.context) OR
 --     (NOT second_person_uri.validURL)
 -- ) AND
 -- second_person_uri.context = peopleknown.context
 ;
