// A quick check on the personal step, before anything is sent to the server.
// The server makes the real decision.
export const MIN_CREDIT_SCORE = 640;

export function preCheck(statedScore: number): { ok: boolean; message?: string } {
  if (statedScore < MIN_CREDIT_SCORE) {
    return { ok: false, message: "We are unlikely to be able to lend with this credit score." };
  }
  return { ok: true };
}
