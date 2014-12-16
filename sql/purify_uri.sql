BEGIN;
drop table if exists peopleuri_modified;
create table peopleuri_modified (localURL VARCHAR(200), context VARCHAR(200), validURL BOOLEAN, personId SERIAL PRIMARY KEY);
--INSERT INTO peopleuri_modified (localURL, context, validURL) SELECT DISTINCT localURL, context, validURL FROM peopleuri WHERE localURL IS NOT NULL AND context IS NOT NULL;
INSERT INTO peopleuri_modified (localURL, context, validURL) SELECT DISTINCT localURL, MIN(context), TRUE FROM peopleuri WHERE localURL IS NOT NULL AND context IS NOT NULL AND validURL GROUP BY localUrl;
INSERT INTO peopleuri_modified (localURL, context, validURL) SELECT localURL, context, FALSE FROM peopleuri WHERE localURL IS NOT NULL AND context IS NOT NULL AND NOT validURL;
END;