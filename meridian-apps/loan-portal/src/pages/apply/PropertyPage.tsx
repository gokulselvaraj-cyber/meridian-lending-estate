import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../../api/client";

export function PropertyPage() {
  const nav = useNavigate();
  const [amount, setAmount] = useState(0);
  const [value, setValue] = useState(0);
  const [rate, setRate] = useState<number>();

  async function getQuote() {
    const b = JSON.parse(sessionStorage.getItem("borrower") ?? "{}");
    const q = await api.quote(b.statedScore, amount, value);
    setRate(q.rate);
    sessionStorage.setItem("property", JSON.stringify({ amount, value, rate: q.rate }));
  }

  return (
    <div>
      <h1>The property</h1>
      <input type="number" placeholder="Amount to borrow" onChange={(e) => setAmount(Number(e.target.value))} />
      <input type="number" placeholder="Property value" onChange={(e) => setValue(Number(e.target.value))} />
      <button onClick={getQuote}>Get a quote</button>
      {rate !== undefined && <p>Your indicative rate: {rate}%</p>}
      <button disabled={rate === undefined} onClick={() => nav("/apply/review")}>Next</button>
    </div>
  );
}
