"""Calls the bank's AI gateway (outside this estate) to draft text."""
import os

import httpx

from notify import config

MODEL = "letter-writer-v2"


def complete(prompt: str, max_tokens: int = 800) -> str:
    res = httpx.post(f"{config.AI_GATEWAY_URL}/v1/complete",
                     headers={"Authorization": f"Bearer {os.environ.get('AI_GATEWAY_TOKEN', '')}"},
                     json={"model": MODEL, "prompt": prompt, "max_tokens": max_tokens},
                     timeout=30)
    res.raise_for_status()
    return res.json()["text"]
