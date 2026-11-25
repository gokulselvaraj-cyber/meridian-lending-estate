import { useNavigate } from "react-router-dom";
import { api } from "../../api/client";

export function ReviewPage() {
  const nav = useNavigate();
  const borrower = JSON.parse(sessionStorage.getItem("borrower") ?? "{}");
  const property = JSON.parse(sessionStorage.getItem("property") ?? "{}");

  async function submit() {
    const res = await api.submitApplication({
      customerId: borrower.customerId,
      requestedAmount: property.amount,
      propertyValue: property.value,
      termMonths: 300,
      street: borrower.street,
      city: borrower.city,
      postcode: borrower.postcode,
    });
    sessionStorage.setItem("applicationId", res.applicationId);
    nav("/apply/submitted");
  }

  return (
    <div>
      <h1>Check and submit</h1>
      <p>{borrower.fullName}, borrowing {property.amount} against {property.value}</p>
      <button onClick={submit}>Submit application</button>
    </div>
  );
}
