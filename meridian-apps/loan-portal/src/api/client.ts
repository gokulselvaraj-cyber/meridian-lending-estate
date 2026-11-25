// The portal's calls to loan-core. All go through the API gateway at /api/v1.
const BASE = "/api/v1";

async function call<T>(method: string, path: string, body?: unknown): Promise<T> {
  const res = await fetch(BASE + path, {
    method,
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!res.ok) {
    throw new Error(`${method} ${path} failed: ${res.status}`);
  }
  return res.json() as Promise<T>;
}

export const api = {
  quote: (statedScore: number, amount: number, propertyValue: number) =>
    call<{ rate: number }>("POST", "/quotes", { statedScore, amount, propertyValue }),
  submitApplication: (application: unknown) => call<{ applicationId: string }>("POST", "/applications", application),
  getLoan: (loanId: string) => call<{ loanId: string; outstanding: number; status: string }>("GET", `/loans/${loanId}`),
  pay: (loanId: string, amount: number) => call<{ paymentId: string }>("POST", `/loans/${loanId}/payments`, { amount }),
};
