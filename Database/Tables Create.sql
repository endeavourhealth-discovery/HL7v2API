DROP SCHEMA if exists hl7v2_inbound;
CREATE SCHEMA hl7v2_inbound;

USE hl7v2_inbound;

CREATE TABLE `acute` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'unique identifier of the HL7v2 Message',
  `date_received` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'the date the message was received',
  `message_wrapper` text COMMENT 'the information contained in the message wrapper',
  `hl7_message` text COMMENT 'the HL7v2 payload of the message',
  `payload_id` varchar(45) NOT NULL COMMENT 'The unique identifier sent by the publishing system',
  `send_to_mq`  varchar(1) default 'N' COMMENT 'whether the message has been sent to the messaging API',
  `source_system` varchar(45) DEFAULT NULL COMMENT 'This Represents the Source System of the Message',
  `message_id` varchar(45) DEFAULT NULL COMMENT 'Information about the Message ID',
  PRIMARY KEY (`id`)
)
ROW_FORMAT=COMPRESSED
KEY_BLOCK_SIZE=8;

-- unclear what this index is neded for
CREATE INDEX ix_acute_date_received_send_to_mq
  ON acute
  (date_received, send_to_mq);
  
-- required for message sender to find messages to send
CREATE INDEX ix_sent_id ON acute (send_to_mq, id);

-- for dashboard on API receipt
CREATE INDEX ix_source_received ON acute (source_system, date_received);

-- for dashboard on message sender queue
CREATE INDEX ix_source_sent_received ON acute (source_system, send_to_mq, date_received);



CREATE table last_message (
  source_system varchar(45),
  date_received datetime,
  message_id varchar(45),
  id bigint,
  PRIMARY KEY (source_system)
);

-- trigger updates the last_message table as messages are inserted into the acute table
DELIMITER $$
CREATE TRIGGER after_acute_insert
  AFTER INSERT ON acute
  FOR EACH ROW
  BEGIN
    INSERT INTO last_message (source_system, date_received, message_id, id)
    VALUES (NEW.source_system, NEW.date_received, NEW.message_id, NEW.id)
    ON DUPLICATE KEY UPDATE
      date_received = NEW.date_received,
      message_id = NEW.message_id,
      id = NEW.id
    ;

  END$$
DELIMITER ;