drop table if exists peoplesynonym_synonym;
create table peoplesynonym_synonym AS
SELECT min(personid) as personId, p1.localUrl as localUrl from 
peopleuri_modified p1, peoplesynonym p2 WHERE
p1.localUrl = p2.synonym GROUP BY p1.localUrl;