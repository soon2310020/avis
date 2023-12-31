BEGIN;

CREATE TABLE IF NOT EXISTS SYSTEM_NOTE_230118 AS SELECT * FROM SYSTEM_NOTE;
CREATE TABLE IF NOT EXISTS BROADCAST_NOTIFICATION_230118 AS SELECT * FROM BROADCAST_NOTIFICATION;
CREATE TABLE IF NOT EXISTS NOTIFICATION_230118 AS SELECT * FROM NOTIFICATION;
CREATE TABLE IF NOT EXISTS TOPIC_230118 AS SELECT * FROM TOPIC;

 UPDATE SYSTEM_NOTE set SYSTEM_NOTE_FUNCTION = 'CATEGORY_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'CATEGORY';
 UPDATE SYSTEM_NOTE set SYSTEM_NOTE_FUNCTION = 'COMPANY_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'COMPANY';
 UPDATE SYSTEM_NOTE set SYSTEM_NOTE_FUNCTION = 'COUNTER_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'COUNTER';
 UPDATE SYSTEM_NOTE set SYSTEM_NOTE_FUNCTION = 'PART_DASHBOARD' WHERE SYSTEM_NOTE_FUNCTION = 'PART';
 UPDATE SYSTEM_NOTE set SYSTEM_NOTE_FUNCTION = 'PART_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'PART_SETTINGS';
 UPDATE SYSTEM_NOTE set SYSTEM_NOTE_FUNCTION = 'TERMINAL_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'TERMINAL';
 UPDATE SYSTEM_NOTE set SYSTEM_NOTE_FUNCTION = 'TOOLING_DASHBOARD' WHERE SYSTEM_NOTE_FUNCTION = 'TOOLING';
 UPDATE SYSTEM_NOTE set SYSTEM_NOTE_FUNCTION = 'TOOLING_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'TOOLING_SETTINGS';
 UPDATE SYSTEM_NOTE set SYSTEM_NOTE_FUNCTION = 'EFFICIENCY_ALERT' WHERE SYSTEM_NOTE_FUNCTION = 'UPTIME_ALERT';
 UPDATE SYSTEM_NOTE set SYSTEM_NOTE_FUNCTION = 'DETACHMENT_ALERT' WHERE SYSTEM_NOTE_FUNCTION = 'DETACHMENT';

 UPDATE BROADCAST_NOTIFICATION set SYSTEM_FUNCTION = 'CATEGORY_SETTING' WHERE SYSTEM_FUNCTION = 'CATEGORY';
UPDATE BROADCAST_NOTIFICATION set SYSTEM_FUNCTION = 'COMPANY_SETTING' WHERE SYSTEM_FUNCTION = 'COMPANY';
UPDATE BROADCAST_NOTIFICATION set SYSTEM_FUNCTION = 'COUNTER_SETTING' WHERE SYSTEM_FUNCTION = 'COUNTER';
 UPDATE BROADCAST_NOTIFICATION set SYSTEM_FUNCTION = 'PART_DASHBOARD' WHERE SYSTEM_FUNCTION = 'PART';
 UPDATE BROADCAST_NOTIFICATION set SYSTEM_FUNCTION = 'PART_SETTING' WHERE SYSTEM_FUNCTION = 'PART_SETTINGS';
 UPDATE BROADCAST_NOTIFICATION set SYSTEM_FUNCTION = 'TERMINAL_SETTING' WHERE SYSTEM_FUNCTION = 'TERMINAL';
 UPDATE BROADCAST_NOTIFICATION set SYSTEM_FUNCTION = 'TOOLING_DASHBOARD' WHERE SYSTEM_FUNCTION = 'TOOLING';
 UPDATE BROADCAST_NOTIFICATION set SYSTEM_FUNCTION = 'TOOLING_SETTING' WHERE SYSTEM_FUNCTION = 'TOOLING_SETTINGS';
 UPDATE BROADCAST_NOTIFICATION set SYSTEM_FUNCTION = 'EFFICIENCY_ALERT' WHERE SYSTEM_FUNCTION = 'UPTIME_ALERT';
 UPDATE BROADCAST_NOTIFICATION set SYSTEM_FUNCTION = 'DETACHMENT_ALERT' WHERE SYSTEM_FUNCTION = 'DETACHMENT';

 UPDATE NOTIFICATION set SYSTEM_NOTE_FUNCTION = 'CATEGORY_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'CATEGORY';
UPDATE NOTIFICATION set SYSTEM_NOTE_FUNCTION = 'COMPANY_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'COMPANY';
UPDATE NOTIFICATION set SYSTEM_NOTE_FUNCTION = 'COUNTER_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'COUNTER';
 UPDATE NOTIFICATION set SYSTEM_NOTE_FUNCTION = 'PART_DASHBOARD' WHERE SYSTEM_NOTE_FUNCTION = 'PART';
 UPDATE NOTIFICATION set SYSTEM_NOTE_FUNCTION = 'PART_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'PART_SETTINGS';
 UPDATE NOTIFICATION set SYSTEM_NOTE_FUNCTION = 'TERMINAL_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'TERMINAL';
 UPDATE NOTIFICATION set SYSTEM_NOTE_FUNCTION = 'TOOLING_DASHBOARD' WHERE SYSTEM_NOTE_FUNCTION = 'TOOLING';
 UPDATE NOTIFICATION set SYSTEM_NOTE_FUNCTION = 'TOOLING_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'TOOLING_SETTINGS';
 UPDATE NOTIFICATION set SYSTEM_NOTE_FUNCTION = 'EFFICIENCY_ALERT' WHERE SYSTEM_NOTE_FUNCTION = 'UPTIME_ALERT';
 UPDATE NOTIFICATION set SYSTEM_NOTE_FUNCTION = 'DETACHMENT_ALERT' WHERE SYSTEM_NOTE_FUNCTION = 'DETACHMENT';

 UPDATE TOPIC set SYSTEM_NOTE_FUNCTION = 'CATEGORY_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'CATEGORY';
UPDATE TOPIC set SYSTEM_NOTE_FUNCTION = 'COMPANY_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'COMPANY';
UPDATE TOPIC set SYSTEM_NOTE_FUNCTION = 'COUNTER_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'COUNTER';
 UPDATE TOPIC set SYSTEM_NOTE_FUNCTION = 'PART_DASHBOARD' WHERE SYSTEM_NOTE_FUNCTION = 'PART';
 UPDATE TOPIC set SYSTEM_NOTE_FUNCTION = 'PART_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'PART_SETTINGS';
 UPDATE TOPIC set SYSTEM_NOTE_FUNCTION = 'TERMINAL_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'TERMINAL';
 UPDATE TOPIC set SYSTEM_NOTE_FUNCTION = 'TOOLING_DASHBOARD' WHERE SYSTEM_NOTE_FUNCTION = 'TOOLING';
 UPDATE TOPIC set SYSTEM_NOTE_FUNCTION = 'TOOLING_SETTING' WHERE SYSTEM_NOTE_FUNCTION = 'TOOLING_SETTINGS';
 UPDATE TOPIC set SYSTEM_NOTE_FUNCTION = 'EFFICIENCY_ALERT' WHERE SYSTEM_NOTE_FUNCTION = 'UPTIME_ALERT';
 UPDATE TOPIC set SYSTEM_NOTE_FUNCTION = 'DETACHMENT_ALERT' WHERE SYSTEM_NOTE_FUNCTION = 'DETACHMENT';

COMMIT ;
