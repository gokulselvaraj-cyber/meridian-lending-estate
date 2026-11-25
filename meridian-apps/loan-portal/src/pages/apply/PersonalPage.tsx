import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { preCheck } from "../../rules/eligibility";
import { isValidAddress } from "../../rules/addressValidator";
import type { Borrower } from "../../types/borrower";

export function PersonalPage() {
  const nav = useNavigate();
  const [borrower, setBorrower] = useState<Partial<Borrower>>({});
  const [score, setScore] = useState(0);
  const [error, setError] = useState<string>();

  function next() {
    const check = preCheck(score);
    if (!check.ok) return setError(check.message);
    if (!isValidAddress(borrower.street ?? "", borrower.city ?? "", borrower.postcode ?? "")) {
      return setError("Please check your address.");
    }
    sessionStorage.setItem("borrower", JSON.stringify({ ...borrower, statedScore: score }));
    nav("/apply/property");
  }

  return (
    <form onSubmit={(e) => { e.preventDefault(); next(); }}>
      <h1>About you</h1>
      <input placeholder="Full name" onChange={(e) => setBorrower({ ...borrower, fullName: e.target.value })} />
      <input placeholder="Street" onChange={(e) => setBorrower({ ...borrower, street: e.target.value })} />
      <input placeholder="City" onChange={(e) => setBorrower({ ...borrower, city: e.target.value })} />
      <input placeholder="Postcode" onChange={(e) => setBorrower({ ...borrower, postcode: e.target.value })} />
      <input type="number" placeholder="Your credit score" onChange={(e) => setScore(Number(e.target.value))} />
      {error && <p role="alert">{error}</p>}
      <button type="submit">Next</button>
    </form>
  );
}
