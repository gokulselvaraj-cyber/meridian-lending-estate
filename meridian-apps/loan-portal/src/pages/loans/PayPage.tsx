import { useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../../api/client";

export function PayPage() {
  const { id } = useParams();
  const [amount, setAmount] = useState(0);
  const [done, setDone] = useState(false);
  return done ? <p>Thank you, your payment is recorded.</p> : (
    <div>
      <h1>Make a payment</h1>
      <input type="number" onChange={(e) => setAmount(Number(e.target.value))} />
      <button onClick={async () => { await api.pay(id!, amount); setDone(true); }}>Pay</button>
    </div>
  );
}
