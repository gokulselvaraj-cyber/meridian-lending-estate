"""Entry point: load today's statements, then consume loan events forever."""
from notify import consumers, statement_loader


def run() -> None:
    statement_loader.load()
    consumers.consume_forever()


if __name__ == "__main__":
    run()
