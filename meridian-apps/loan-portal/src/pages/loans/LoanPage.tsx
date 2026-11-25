import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api } from "../../api/client";

export function LoanPage() {
  const { id } = useParams();
  const [loan, setLoan] = useState<{ outstanding: number; status: string }>();
  useEffect(() => { api.getLoan(id!).then(setLoan); }, [id]);
  if (!loan) return <p>Loading</p>;
  return (
    <div>
      <h1>Your loan {id}</h1>
      <p>Outstanding: {loan.outstanding}</p>
      <p>Status: {loan.status}</p>
      <Link to={`/loans/${id}/pay`}>Make a payment</Link>
    </div>
  );
}
