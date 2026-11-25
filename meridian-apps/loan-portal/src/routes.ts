import { PersonalPage } from "./pages/apply/PersonalPage";
import { PropertyPage } from "./pages/apply/PropertyPage";
import { ReviewPage } from "./pages/apply/ReviewPage";
import { SubmittedPage } from "./pages/apply/SubmittedPage";
import { LoanPage } from "./pages/loans/LoanPage";
import { PayPage } from "./pages/loans/PayPage";

// The application wizard runs in this order: personal -> property -> review -> submitted.
export const routes = [
  { path: "/apply/personal", element: PersonalPage, next: "/apply/property" },
  { path: "/apply/property", element: PropertyPage, next: "/apply/review" },
  { path: "/apply/review", element: ReviewPage, next: "/apply/submitted" },
  { path: "/apply/submitted", element: SubmittedPage },
  { path: "/loans/:id", element: LoanPage },
  { path: "/loans/:id/pay", element: PayPage },
];
