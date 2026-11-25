"""Consumes loan-core's events and sends the matching customer message."""
import json

from confluent_kafka import Consumer

from notify import config, dunning, welcome


def consume_forever() -> None:
    c = Consumer({"bootstrap.servers": config.KAFKA_BOOTSTRAP, "group.id": "notify-svc",
                  "auto.offset.reset": "earliest"})
    c.subscribe([config.TOPIC_LOAN_DISBURSED, config.TOPIC_PAYMENT_MISSED])
    while True:
        msg = c.poll(1.0)
        if msg is None or msg.error():
            continue
        event = json.loads(msg.value())
        if msg.topic() == config.TOPIC_LOAN_DISBURSED:
            welcome.send_welcome(event["customer_id"], event["loan_id"], event["amount"])
        elif msg.topic() == config.TOPIC_PAYMENT_MISSED:
            dunning.start(event["loan_id"], event["customer_id"], event["installment_no"],
                          event["amount_due"], event["due_date"])
        c.commit(msg)
