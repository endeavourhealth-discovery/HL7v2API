DROP SCHEMA if exists hl7v2_imperial;

CREATE SCHEMA hl7v2_imperial;

USE hl7v2_imperial;

CREATE TABLE `hl7v2_inbound_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'unique identifier of the HL7v2 Message',
  `date_received` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'the date the message was received',
  `message_wrapper` text COMMENT 'the information contained in the message wrapper',
  `hl7_message` text COMMENT 'the HL7v2 payload of the message',
  `payload_id` varchar(45) NOT NULL COMMENT 'The unique identifier sent by the publishing system',
  `processed` boolean not null default FALSE COMMENT 'whether the message has been sent to the messaging API',
  PRIMARY KEY (`id`),
  UNIQUE KEY `payload_id_UNIQUE` (`payload_id`)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;

CREATE INDEX ix_hl7v2_inbound_data_date_received_processed
  ON hl7v2_inbound_data
  (date_received, processed);