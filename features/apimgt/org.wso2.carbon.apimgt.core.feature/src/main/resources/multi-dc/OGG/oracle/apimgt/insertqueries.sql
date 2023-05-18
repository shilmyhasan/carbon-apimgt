INSERT INTO IDN_BASE_TABLE values ('WSO2 Identity Server')
/
INSERT INTO CM_PURPOSE (NAME, DESCRIPTION, PURPOSE_GROUP, GROUP_TYPE, TENANT_ID) VALUES ('DEFAULT', 'For core functionalities of the product', 'DEFAULT', 'SP', '-1234')
/
INSERT INTO CM_PURPOSE_CATEGORY (NAME, DESCRIPTION, TENANT_ID) VALUES ('DEFAULT','For core functionalities of the product', '-1234')
/
INSERT INTO AM_ALERT_TYPES (ALERT_TYPE_NAME, STAKE_HOLDER) VALUES ('AbnormalResponseTime', 'publisher')
/
INSERT INTO AM_ALERT_TYPES (ALERT_TYPE_NAME, STAKE_HOLDER) VALUES ('AbnormalBackendTime', 'publisher')
/
INSERT INTO AM_ALERT_TYPES (ALERT_TYPE_NAME, STAKE_HOLDER) VALUES ('AbnormalRequestsPerMin', 'subscriber')
/
INSERT INTO AM_ALERT_TYPES (ALERT_TYPE_NAME, STAKE_HOLDER) VALUES ('AbnormalRequestPattern', 'subscriber')
/
INSERT INTO AM_ALERT_TYPES (ALERT_TYPE_NAME, STAKE_HOLDER) VALUES ('UnusualIPAccess', 'subscriber')
/
INSERT INTO AM_ALERT_TYPES (ALERT_TYPE_NAME, STAKE_HOLDER) VALUES ('FrequentTierLimitHitting', 'subscriber')
/
INSERT INTO AM_ALERT_TYPES (ALERT_TYPE_NAME, STAKE_HOLDER) VALUES ('ApiHealthMonitor', 'publisher')
/
INSERT ALL INTO IDN_CONFIG_TYPE (ID, NAME, DESCRIPTION) VALUES
('9ab0ef95-13e9-4ed5-afaf-d29bed62f7bd', 'IDP_TEMPLATE', 'Template type to uniquely identify IDP templates')
INTO IDN_CONFIG_TYPE (ID, NAME, DESCRIPTION) VALUES
('3c4ac3d0-5903-4e3d-aaca-38df65b33bfd', 'APPLICATION_TEMPLATE', 'Template type to uniquely identify Application templates')
INTO IDN_CONFIG_TYPE (ID, NAME, DESCRIPTION) VALUES
('8ec6dbf1-218a-49bf-bc34-0d2db52d151c', 'CORS_CONFIGURATION', 'A resource type to keep the tenant CORS configurations')
SELECT 1 FROM dual
/