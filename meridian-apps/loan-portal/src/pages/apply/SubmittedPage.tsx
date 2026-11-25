export function SubmittedPage() {
  const id = sessionStorage.getItem("applicationId");
  return (
    <div>
      <h1>Application received</h1>
      <p>Your reference is {id}. We will email you with our decision.</p>
    </div>
  );
}
