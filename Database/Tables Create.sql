DROP SCHEMA hl7v2_inbound;

CREATE SCHEMA hl7v2_inbound;

USE hl7v2_inbound;

CREATE TABLE `imperial` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'unique identifier of the HL7v2 Message',
  `date_received` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'the date the message was received',
  `message_wrapper` text COMMENT 'the information contained in the message wrapper',
  `hl7_message` text COMMENT 'the HL7v2 payload of the message',
  `payload_id` varchar(45) NOT NULL COMMENT 'The unique identifier sent by the publishing system',
  `processed` boolean not null default FALSE COMMENT 'whether the message has been sent to the messaging API',
  PRIMARY KEY (`id`),
  UNIQUE KEY `payload_id_UNIQUE` (`payload_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

CREATE INDEX ix_imperial_date_received_processed
  ON imperial
  (date_received, processed);